package ru.soknight.easypayments.sdk;

import org.bukkit.plugin.Plugin;
import ru.soknight.easypayments.sdk.data.model.ProcessPayment;
import ru.soknight.easypayments.sdk.data.model.ProcessPaymentReports;
import ru.soknight.easypayments.sdk.data.model.VersionResponse;
import ru.soknight.easypayments.sdk.exception.ApiException;
import ru.soknight.easypayments.sdk.exception.ErrorResponseException;
import ru.soknight.easypayments.sdk.exception.UnsuccessfulResponseException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface EasyPaymentsSDK {

    static EasyPaymentsSDK create(Plugin plugin, String accessKey, int serverId) {
        return new SimpleEasyPaymentsSDK(plugin, accessKey, serverId);
    }

    List<ProcessPayment> getProcessPayments() throws ErrorResponseException, IOException, ApiException;

    Map<Integer, Boolean> reportProcessPayments(ProcessPaymentReports reports) throws ErrorResponseException, IOException, ApiException, UnsuccessfulResponseException;

    VersionResponse checkForUpdates(String moduleId) throws  ErrorResponseException, IOException, ApiException;

}
