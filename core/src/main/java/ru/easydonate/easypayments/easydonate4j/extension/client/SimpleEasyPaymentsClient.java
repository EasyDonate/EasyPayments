package ru.easydonate.easypayments.easydonate4j.extension.client;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easydonate4j.Constants;
import ru.easydonate.easydonate4j.api.v3.client.SimpleEasyDonateClient;
import ru.easydonate.easydonate4j.exception.HttpRequestException;
import ru.easydonate.easydonate4j.exception.JsonSerializationException;
import ru.easydonate.easydonate4j.http.client.HttpClient;
import ru.easydonate.easydonate4j.http.response.EasyHttpResponse;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.EventUpdateReports;
import ru.easydonate.easypayments.easydonate4j.longpoll.client.LongPollClient;

@Getter
final class SimpleEasyPaymentsClient extends SimpleEasyDonateClient implements EasyPaymentsClient {

    private final LongPollClient longPollClient;

    SimpleEasyPaymentsClient(@NotNull String accessKey, @NotNull String userAgent, int serverId) {
        super(accessKey, userAgent, Constants.CONNECT_TIMEOUT, Constants.RESPONSE_TIMEOUT, Constants.READ_TIMEOUT, Constants.WRITE_TIMEOUT);
        this.longPollClient = LongPollClient.create(accessKey, serverId, userAgent);
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
