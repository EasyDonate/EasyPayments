package ru.soknight.easypayments.cache;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.plugin.Plugin;
import ru.soknight.easypayments.sdk.data.model.ProcessPayment;
import ru.soknight.easypayments.sdk.data.model.ProcessPaymentReport;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

public final class ReportCache {

    private final Plugin plugin;
    private final Path cachePath;

    private final Gson gson;
    private final Map<Integer, ProcessPaymentReport> reports;

    public ReportCache(Plugin plugin) {
        this.plugin = plugin;
        this.cachePath = new File(plugin.getDataFolder(), "cache.json").toPath();

        this.gson = new GsonBuilder().create();
        this.reports = new HashMap<>();
    }

    public void loadReports() {
        reports.clear();

        if(!Files.exists(cachePath))
            return;

        try {
            List<String> lines = Files.readAllLines(cachePath, StandardCharsets.UTF_8);
            String content = String.join("\n", lines);

            Reports parsed = gson.fromJson(content, Reports.class);
            if(parsed != null && !parsed.isEmpty())
                parsed.forEach(report -> reports.put(report.getPaymentId(), report));

            if(!reports.isEmpty())
                plugin.getLogger().info(reports.size() + " report(s) has been loaded to the cache.");
        } catch (IOException ignored) {}
    }

    public void addToCache(ProcessPaymentReport report) {
        synchronized (this) {
            reports.put(report.getPaymentId(), report);
            saveReports();
        }
    }

    public void addToCache(Iterable<ProcessPaymentReport> reports) {
        synchronized (this) {
            reports.forEach(report -> this.reports.put(report.getPaymentId(), report));
            saveReports();
        }
    }

    public void clear() {
        synchronized (this) {
            reports.clear();
            deleteFile();
        }
    }

    public ProcessPaymentReport getById(int paymentId) {
        synchronized (this) {
            return reports.get(paymentId);
        }
    }

    public List<ProcessPaymentReport> getCachedReports() {
        synchronized (this) {
            return new ArrayList<>(reports.values());
        }
    }

    public boolean isCached(ProcessPaymentReport report) {
        return isCached(report.getPaymentId());
    }

    public boolean isCached(ProcessPayment payment) {
        return isCached(payment.getPaymentId());
    }

    public boolean isCached(int paymentId) {
        return reports.containsKey(paymentId);
    }

    public ProcessPaymentReport unloadReport(int paymentId) {
        synchronized (this) {
            ProcessPaymentReport removed = reports.remove(paymentId);
            saveReports();
            return removed;
        }
    }

    public void unloadReport(ProcessPaymentReport report) {
        synchronized (this) {
            reports.remove(report.getPaymentId());
            saveReports();
        }
    }

    public void unloadReports(Iterable<ProcessPaymentReport> reports) {
        synchronized (this) {
            reports.forEach(report -> this.reports.remove(report.getPaymentId()));
            saveReports();
        }
    }

    private void saveReports() {
        if(reports.isEmpty()) {
            deleteFile();
            return;
        }

        try {
            String asJson = gson.toJson(reports.values());
            Files.write(
                    cachePath,
                    Collections.singletonList(asJson),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException ignored) {}
    }

    private void deleteFile() {
        try {
            Files.deleteIfExists(cachePath);
        } catch (IOException ignored) {}
    }

    private static final class Reports extends ArrayList<ProcessPaymentReport> {}

}
