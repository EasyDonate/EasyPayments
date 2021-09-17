package ru.soknight.easypayments.task;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.plugin.Plugin;
import ru.soknight.easypayments.EasyPaymentsPlugin;
import ru.soknight.easypayments.cache.ReportCache;
import ru.soknight.easypayments.execution.ExecutionBundle;
import ru.soknight.easypayments.execution.InterceptorFactory;
import ru.soknight.easypayments.sdk.EasyPaymentsSDK;
import ru.soknight.easypayments.sdk.data.model.ProcessPayment;
import ru.soknight.easypayments.sdk.data.model.ProcessPaymentReport;
import ru.soknight.easypayments.sdk.data.model.ProcessPaymentReports;
import ru.soknight.easypayments.sdk.exception.ApiException;
import ru.soknight.easypayments.sdk.exception.ErrorResponseException;
import ru.soknight.easypayments.sdk.exception.UnsuccessfulResponseException;
import ru.soknight.easypayments.sdk.response.AbstractResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public final class PaymentsQueryTask extends AbstractPluginTask {

    private final EasyPaymentsSDK sdk;
    private final InterceptorFactory interceptorFactory;
    private final ReportCache reportCache;

    public PaymentsQueryTask(Plugin plugin, EasyPaymentsSDK sdk, InterceptorFactory interceptorFactory, ReportCache reportCache) {
        super(plugin, 100L);

        this.sdk = sdk;
        this.interceptorFactory = interceptorFactory;
        this.reportCache = reportCache;
    }

    @Override
    protected long getPeriod() {
        int period = plugin.getConfig().getInt("request-frequency", 1);
        return period >= 1 ? period * 1200L : 1200L; // >= 1 min.
    }

    @Override
    public void run() {
        synchronized (sdk) {
            List<ProcessPaymentReport> uncompetedReports = null;
            Map<Integer, Boolean> processedReports = null;

            try {
                List<ProcessPayment> processPayments = sdk.getProcessPayments();
                if(processPayments == null || processPayments.isEmpty())
                    return;

                processPayments.removeIf(reportCache::isCached);
                if(processPayments.isEmpty())
                    return;

                ProcessPaymentReports reports = ProcessPaymentReports.empty();
                processPayments.parallelStream().forEach(payment -> handlePayment(payment, reports));
                if(reports.isEmpty())
                    return;

                uncompetedReports = reports.getReports();
                reportCache.addToCache(uncompetedReports);

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                if(EasyPaymentsPlugin.isDebugEnabled()) {
                    plugin.getLogger().info("--- [ Process payments reports ] ---");
                    plugin.getLogger().info(gson.toJson(reports));
                }

                processedReports = sdk.reportProcessPayments(reports);
            } catch (ApiException ex) {
                if(EasyPaymentsPlugin.logQueryTaskErrors() && EasyPaymentsPlugin.isDebugEnabled())
                    warning("API response: %s", ex.getMessage());
            } catch (UnsuccessfulResponseException ex) {
                AbstractResponse<Map<Integer, Boolean>> response = ex.getResponse();
                processedReports = response.getResponseObject();
            } catch (ErrorResponseException ex) {
                if(EasyPaymentsPlugin.logQueryTaskErrors()) {
                    error("HTTP request failed!");
                    error("Details: " + ex.getMessage());
                }
            } catch (IOException ex) {
                if(EasyPaymentsPlugin.logQueryTaskErrors()) {
                    error("Cannot connect to the API server!");
                    error("Details: " + ex.getMessage());
                    if(EasyPaymentsPlugin.isDebugEnabled())
                        ex.printStackTrace();
                }
            }

            if(processedReports != null && uncompetedReports != null) {
                Map<Integer, Boolean> processed = processedReports;
                List<ProcessPaymentReport> completed = new ArrayList<>(uncompetedReports);
                uncompetedReports.removeIf(report -> processed.getOrDefault(report.getPaymentId(), false));
                completed.removeAll(uncompetedReports);
                handleUncompletedReports(uncompetedReports);
                reportCache.unloadReports(completed);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void handlePayment(ProcessPayment payment, ProcessPaymentReports reports) {
        List<String> commands = payment.getCommands();
        ProcessPaymentReport report = ProcessPaymentReport.create(payment);
        report.setPayload(payment.getPayload());
        reports.addReport(report);

        if(commands == null || commands.isEmpty())
            return;

        AtomicInteger counter = new AtomicInteger();
        CompletableFuture<ExecutionBundle>[] futures = commands.stream()
                .map(cmd -> new ExecutionBundle(counter.getAndIncrement(), cmd, plugin, interceptorFactory))
                .map(ExecutionBundle::executeAsync)
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();

        Arrays.stream(futures)
                .map(CompletableFuture::join)
                .forEach(exec -> exec.saveTo(report));
    }

    private void handleUncompletedReports(List<ProcessPaymentReport> reports) {
        if(reports == null || reports.isEmpty())
            return;

        if(EasyPaymentsPlugin.logQueryTaskErrors()) {
            error("Failed to send %d report(s) to the server!", reports.size());
            error("This report(s) has been cached, the cache worker");
            error("will retry to upload that again every 5 minutes.");
        }

        reportCache.addToCache(reports);
    }

    private void warning(String format, Object... args) {
        plugin.getLogger().warning(String.format(format, args));
    }

    private void error(String format, Object... args) {
        plugin.getLogger().severe(String.format(format, args));
    }

}
