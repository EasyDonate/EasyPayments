package ru.easydonate.easypayments.task;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easydonate4j.api.v3.exception.ApiResponseFailureException;
import ru.easydonate.easydonate4j.exception.HttpRequestException;
import ru.easydonate.easydonate4j.exception.HttpResponseException;
import ru.easydonate.easypayments.EasyPaymentsPlugin;
import ru.easydonate.easypayments.easydonate4j.extension.client.EasyPaymentsClient;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.EventUpdateReports;
import ru.easydonate.easypayments.easydonate4j.longpoll.data.model.EventUpdates;
import ru.easydonate.easypayments.execution.ExecutionController;

public final class PaymentsQueryTask extends AbstractPluginTask {

    private static final long TASK_PERIOD = 100L;

    private final ExecutionController executionController;
    private final EasyPaymentsClient easyPaymentsClient;

    public PaymentsQueryTask(
            @NotNull Plugin plugin,
            @NotNull ExecutionController executionController,
            @NotNull EasyPaymentsClient easyPaymentsClient
    ) {
        super(plugin, 100L);

        this.executionController = executionController;
        this.easyPaymentsClient = easyPaymentsClient;
    }

    @Override
    protected long getPeriod() {
        return TASK_PERIOD;
    }

    @Override
    public void run() {
        // do that synchronously to prevent any conflicts with other tasks
        synchronized (executionController.getDatabaseManager()) {
            try {
                EventUpdates updates = easyPaymentsClient.getLongPollClient().getUpdatesListSync();
                EventUpdateReports reports = executionController.processEventUpdates(updates).join();
                executionController.uploadReports(easyPaymentsClient, reports);
            } catch (ApiResponseFailureException ex) {
                // redirect API errors to warning channel
                if(EasyPaymentsPlugin.logQueryTaskErrors() && EasyPaymentsPlugin.isDebugEnabled()) {
                    warning(ex.getMessage());
                }
            } catch (HttpRequestException | HttpResponseException ex) {
                // redirect any other errors to error channel
                if(EasyPaymentsPlugin.logQueryTaskErrors()) {
                    error(ex.getMessage());
                    if(EasyPaymentsPlugin.isDebugEnabled()) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        updateActivityState();
    }

}
