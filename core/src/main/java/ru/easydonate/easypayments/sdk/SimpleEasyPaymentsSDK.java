package ru.easydonate.easypayments.sdk;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.bukkit.plugin.Plugin;
import ru.easydonate.easypayments.EasyPaymentsPlugin;
import ru.easydonate.easypayments.sdk.data.model.ProcessPayment;
import ru.easydonate.easypayments.sdk.data.model.ProcessPaymentReports;
import ru.easydonate.easypayments.sdk.data.model.VersionResponse;
import ru.easydonate.easypayments.sdk.exception.ApiException;
import ru.easydonate.easypayments.sdk.exception.ErrorResponseException;
import ru.easydonate.easypayments.sdk.exception.UnsuccessfulResponseException;
import ru.easydonate.easypayments.sdk.http.HttpRequest;
import ru.easydonate.easypayments.sdk.response.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class SimpleEasyPaymentsSDK implements EasyPaymentsSDK {

    private static final Gson GSON;

    private static final String GET_PROCESS_PAYMENTS_URL;
    private static final String REPORT_PROCESS_PAYMENTS_URL;
    private static final String CHECK_FOR_UPDATES_URL;

    private final Plugin plugin;
    private final String accessKey;
    private final int serverId;

    static {
        GSON = new GsonBuilder().create();

        GET_PROCESS_PAYMENTS_URL = "https://easydonate.ru/api/shop/{key}/getProcessPayments?server_id={server_id}";
        REPORT_PROCESS_PAYMENTS_URL = "https://easydonate.ru/api/shop/{key}/reportProcessPayments?server_id={server_id}";
        CHECK_FOR_UPDATES_URL = "https://easydonate.ru/api/getPluginUpdates?version=%s&edition=je&type=%s";
    }

    @Override
    public List<ProcessPayment> getProcessPayments() throws ErrorResponseException, IOException, ApiException {
        return executeGet(GET_PROCESS_PAYMENTS_URL, ProcessPaymentsResponse.class);
    }

    @Override
    public Map<Integer, Boolean> reportProcessPayments(ProcessPaymentReports reports) throws ErrorResponseException, IOException, ApiException, UnsuccessfulResponseException {
        if(reports == null || reports.isEmpty())
            Collections.emptyMap();

        return executePost(REPORT_PROCESS_PAYMENTS_URL, reports, ReportProcessPaymentsResponse.class);
    }

    @Override
    public VersionResponse checkForUpdates(String moduleId) throws ErrorResponseException, IOException, ApiException {
        String version = plugin.getDescription().getVersion();
        return executeGet(String.format(CHECK_FOR_UPDATES_URL, version, moduleId), VersionCheckResponse.class);
    }

    private <T> T executeGet(String url, Class<? extends AbstractResponse<T>> responseType) throws ApiException, ErrorResponseException, IOException {
        url = url.replace("{key}", accessKey).replace("{server_id}", String.valueOf(serverId));

        String rawResponse = HttpRequest.get(url);
        if(EasyPaymentsPlugin.isDebugEnabled())
            plugin.getLogger().info(rawResponse);

        try {
            AbstractResponse<T> response = GSON.fromJson(rawResponse, responseType);
            if(response != null && response.isSuccess())
                return response.getResponseObject();
        } catch (JsonSyntaxException ignored) {}

        try {
            ErrorResponse errorResponse = GSON.fromJson(rawResponse, ErrorResponse.class);
            if(errorResponse == null)
                errorResponse = ErrorResponse.internal("'response' is null!");
            throw new ApiException(errorResponse, rawResponse);
        } catch (JsonSyntaxException ex) {
            throw new ErrorResponseException(ex);
        }
    }

    private <T> T executePost(String url, Object content, Class<? extends AbstractResponse<T>> responseType)
            throws ApiException, IOException, ErrorResponseException, UnsuccessfulResponseException
    {
        url = url.replace("{key}", accessKey).replace("{server_id}", String.valueOf(serverId));

        String jsonContent = GSON.toJson(content);
        String rawResponse = HttpRequest.post(url, jsonContent);
        if(EasyPaymentsPlugin.isDebugEnabled())
            plugin.getLogger().info(rawResponse);

        try {
            AbstractResponse<T> response = GSON.fromJson(rawResponse, responseType);
            if(response != null) {
                if(response.isSuccess())
                    return response.getResponseObject();
                else if(!response.requiresStringResponse())
                    throw new UnsuccessfulResponseException(response, rawResponse);
            }
        } catch (JsonSyntaxException ignored) {}

        try {
            ErrorResponse errorResponse = GSON.fromJson(rawResponse, ErrorResponse.class);
            if(errorResponse == null)
                errorResponse = ErrorResponse.internal("'response' is null!");
            throw new ApiException(errorResponse, rawResponse);
        } catch (JsonSyntaxException ex) {
            throw new ErrorResponseException(ex);
        }
    }
    
}
