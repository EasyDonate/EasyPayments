package ru.easydonate.easypayments.easydonate4j.extension.client;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easydonate4j.Constants;
import ru.easydonate.easydonate4j.api.v3.client.SimpleEasyDonateClient;
import ru.easydonate.easydonate4j.exception.HttpRequestException;
import ru.easydonate.easydonate4j.exception.HttpResponseException;
import ru.easydonate.easydonate4j.exception.JsonSerializationException;
import ru.easydonate.easydonate4j.http.QueryParams;
import ru.easydonate.easydonate4j.http.client.HttpClient;
import ru.easydonate.easydonate4j.http.request.EasyHttpRequest;
import ru.easydonate.easydonate4j.http.response.EasyHttpResponse;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.EventUpdateReports;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.VersionResponse;
import ru.easydonate.easypayments.easydonate4j.extension.response.VersionCheckResponse;
import ru.easydonate.easypayments.easydonate4j.longpoll.client.LongPollClient;

@Getter
final class SimpleEasyPaymentsClient extends SimpleEasyDonateClient implements EasyPaymentsClient {

    private static final String CHECK_FOR_UPDATES_URL = "https://ep.easydonate.ru/updates?version=%s&edition=je";

    private final LongPollClient longPollClient;

    SimpleEasyPaymentsClient(@NotNull String accessKey, @NotNull String userAgent, int serverId) {
        super(accessKey, userAgent, Constants.CONNECT_TIMEOUT, Constants.RESPONSE_TIMEOUT, Constants.READ_TIMEOUT, Constants.WRITE_TIMEOUT);
        this.longPollClient = LongPollClient.create(accessKey, serverId, userAgent);
    }

    @Override
    public @NotNull VersionResponse checkForUpdates(@NotNull String version) throws HttpRequestException, HttpResponseException {
        QueryParams queryParams = new QueryParams()
                .set("version", version)
                .set("edition", "je");

        EasyHttpRequest httpRequest = createRequest(HttpClient.Method.GET)
                .setHeaders(defaultHeaders)
                .setQueryParams(queryParams)
                .setUrl(CHECK_FOR_UPDATES_URL, version)
                .build();

        return request(VersionCheckResponse.class, httpRequest);
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
