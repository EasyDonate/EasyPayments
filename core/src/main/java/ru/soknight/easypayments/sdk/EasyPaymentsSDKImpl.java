package ru.soknight.easypayments.sdk;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.bukkit.plugin.Plugin;
import ru.soknight.easypayments.EasyPaymentsPlugin;
import ru.soknight.easypayments.sdk.data.model.ProcessPayment;
import ru.soknight.easypayments.sdk.data.model.ProcessPaymentReports;
import ru.soknight.easypayments.sdk.data.model.VersionResponse;
import ru.soknight.easypayments.sdk.exception.ApiException;
import ru.soknight.easypayments.sdk.exception.ErrorResponseException;
import ru.soknight.easypayments.sdk.exception.UnsuccessfulResponseException;
import ru.soknight.easypayments.sdk.http.HttpRequest;
import ru.soknight.easypayments.sdk.response.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
final class EasyPaymentsSDKImpl implements EasyPaymentsSDK {

    private static final Gson GSON = new GsonBuilder().create();

    private final Plugin plugin;
    private final String accessKey;
    private final int serverId;

    @Override
    public List<ProcessPayment> getProcessPayments() throws ErrorResponseException, IOException, ApiException {
        return executeGet("https://easydonate.ru/api/shop/{key}/getProcessPayments?server_id={server_id}", ProcessPaymentsResponse.class);
    }

    @Override
    public Map<Integer, Boolean> reportProcessPayments(ProcessPaymentReports reports) throws ErrorResponseException, IOException, ApiException, UnsuccessfulResponseException {
        if(reports == null || reports.isEmpty())
            Collections.emptyMap();

        return executePost("https://easydonate.ru/api/shop/{key}/reportProcessPayments?server_id={server_id}", reports, ReportProcessPaymentsResponse.class);
    }

    @Override
    public VersionResponse checkForUpdates(String moduleId) throws ErrorResponseException, IOException, ApiException {
        String url = "https://easydonate.ru/api/getPluginUpdates?version=%s&edition=je&type=%s";
        String version = plugin.getDescription().getVersion();
        return executeGet(String.format(url, version, moduleId), VersionCheckResponse.class);
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
