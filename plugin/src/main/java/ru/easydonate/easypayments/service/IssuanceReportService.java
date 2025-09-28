package ru.easydonate.easypayments.service;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easydonate4j.exception.HttpRequestException;
import ru.easydonate.easydonate4j.exception.HttpResponseException;
import ru.easydonate.easydonate4j.exception.JsonSerializationException;
import ru.easydonate.easypayments.EasyPaymentsPlugin;
import ru.easydonate.easypayments.core.EasyPayments;
import ru.easydonate.easypayments.core.easydonate4j.EventType;
import ru.easydonate.easypayments.core.easydonate4j.extension.client.EasyPaymentsClient;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.EventUpdateReport;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.EventUpdateReports;
import ru.easydonate.easypayments.database.model.Payment;

import java.util.List;
import java.util.function.Predicate;

@AllArgsConstructor
public final class IssuanceReportService {

    private final EasyPayments plugin;
    private final PersistanceService persistanceService;
    private final EasyPaymentsClient easyPaymentsClient;

    public void uploadReports(@NotNull List<EventUpdateReport<?>> reports) throws HttpRequestException, HttpResponseException {
        EventUpdateReports collection = new EventUpdateReports(reports);
        uploadReports(collection);
    }

    @SneakyThrows(JsonSerializationException.class)
    public void uploadReports(@NotNull EventUpdateReports reports) throws HttpRequestException, HttpResponseException {
        if (reports.isEmpty())
            return;

        plugin.getDebugLogger().debug("[Issuance] Uploading reports:");
        plugin.getDebugLogger().debug(reports.toPrettyString().split("\n"));

        if (easyPaymentsClient == null || !easyPaymentsClient.uploadReports(reports)) {
            plugin.getLogger().severe("An unknown error occured while trying to upload reports!");
            plugin.getLogger().severe("Please, contact with the platform support:");
            plugin.getLogger().severe(EasyPaymentsPlugin.SUPPORT_URL);
            return;
        }

        plugin.getDebugLogger().debug("[Issuance] Reports have been uploaded");
    }

    public void uploadReportsAndPersistStates(
            @NotNull List<EventUpdateReport<?>> reports,
            @Nullable Predicate<Payment> collectedStateResolver
    ) throws HttpRequestException, HttpResponseException {
        EventUpdateReports collection = new EventUpdateReports(reports);
        uploadReportsAndPersistStates(collection, collectedStateResolver);
    }

    public void uploadReportsAndPersistStates(
            @NotNull EventUpdateReports reports,
            @Nullable Predicate<Payment> collectedStateResolver
    ) throws HttpRequestException, HttpResponseException {
        if (reports.isEmpty())
            return;

        uploadReports(reports);

        if (reports.containsReportWithType(EventType.NEW_PAYMENT)) {
            persistanceService.persistPaymentsReportedState(reports, collectedStateResolver);
        } else {
            plugin.getDebugLogger().debug("[Issuance] There are no 'new_payment' events, skipping persistance update...");
        }
    }

}
