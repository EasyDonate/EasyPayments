package ru.easydonate.easypayments.core.easydonate4j.extension.client;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easydonate4j.Constants;
import ru.easydonate.easydonate4j.api.v3.client.SimpleEasyDonateClient;
import ru.easydonate.easydonate4j.api.v3.response.ResponseContentDeserializer;
import ru.easydonate.easydonate4j.exception.HttpRequestException;
import ru.easydonate.easydonate4j.exception.HttpResponseException;
import ru.easydonate.easydonate4j.exception.JsonSerializationException;
import ru.easydonate.easydonate4j.http.QueryParams;
import ru.easydonate.easydonate4j.http.client.HttpClient;
import ru.easydonate.easydonate4j.http.request.EasyHttpRequest;
import ru.easydonate.easydonate4j.http.response.EasyHttpResponse;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.EventUpdateReports;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.PluginStateModel;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.PluginVersionModel;
import ru.easydonate.easypayments.core.easydonate4j.extension.response.PluginStateResponse;
import ru.easydonate.easypayments.core.easydonate4j.extension.response.PluginVersionResponse;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.client.LongPollClient;

import java.util.Map;

@Getter
final class SimpleEasyPaymentsClient extends SimpleEasyDonateClient implements EasyPaymentsClient {

    private static final String CHECK_FOR_UPDATES_URL = "https://ep.easydonate.ru/updates?version=%s&edition=je";

    private final LongPollClient longPollClient;

    SimpleEasyPaymentsClient(@NotNull String accessKey, @NotNull String userAgent, int serverId) {
        super(accessKey, userAgent, Constants.CONNECT_TIMEOUT, Constants.RESPONSE_TIMEOUT, Constants.READ_TIMEOUT, Constants.WRITE_TIMEOUT);
        this.longPollClient = LongPollClient.create(accessKey, serverId, userAgent);
    }

    @Override
    public @NotNull PluginVersionModel getPluginVersion(@NotNull String currentVersion) throws HttpRequestException, HttpResponseException {
        QueryParams queryParams = new QueryParams()
                .set("version", currentVersion)
                .set("edition", "je");

        EasyHttpRequest httpRequest = createRequest(HttpClient.Method.GET)
                .setHeaders(defaultHeaders)
                .setQueryParams(queryParams)
                .setUrl(CHECK_FOR_UPDATES_URL, currentVersion)
                .build();

        return request(PluginVersionResponse.class, httpRequest);
    }

    @Override
    public @NotNull PluginStateModel getPluginState() throws HttpRequestException, HttpResponseException {
        EasyHttpResponse response = longPollClient.makeCustomRequest(HttpClient.Method.GET, builder -> builder.setApiPath("/state"));
        return ResponseContentDeserializer.deserializeResponseContent(PluginStateResponse.class, response);
    }

    @Override
    public boolean uploadKnownPlayers(@NotNull Map<String, Boolean> knownPlayers) throws HttpRequestException, HttpResponseException {
        if (knownPlayers.isEmpty())
            return false;

        try {
            String jsonBody = jsonSerialization.serialize(knownPlayers);

            EasyHttpResponse response = longPollClient.makeCustomRequest(HttpClient.Method.POST, builder -> builder
                    .setApiPath("/players")
                    .setBody(jsonBody));

            return response.isSuccess();
        } catch (JsonSerializationException ignored) {
            return false;
        }
    }

    @Override
    public boolean uploadReports(@NotNull EventUpdateReports updateReports) throws HttpRequestException {
        try {
            String jsonBody = jsonSerialization.serialize(updateReports);

            EasyHttpResponse response = longPollClient.makeCustomRequest(HttpClient.Method.POST, builder -> builder
                    .setApiPath("/report")
                    .setBody(jsonBody));

            return response.isSuccess();
        } catch (JsonSerializationException ignored) {
            return false;
        }
    }

}
