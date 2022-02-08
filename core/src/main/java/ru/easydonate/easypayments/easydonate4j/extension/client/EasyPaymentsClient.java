package ru.easydonate.easypayments.easydonate4j.extension.client;

import org.jetbrains.annotations.NotNull;
import ru.easydonate.easydonate4j.api.v3.client.EasyDonateClient;
import ru.easydonate.easydonate4j.exception.HttpRequestException;
import ru.easydonate.easydonate4j.exception.HttpResponseException;
import ru.easydonate.easydonate4j.http.client.jdk.legacy.JDKLegacyHttpClientService;
import ru.easydonate.easydonate4j.json.serialization.GsonSerializationService;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.EventUpdateReports;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.VersionResponse;
import ru.easydonate.easypayments.easydonate4j.longpoll.client.LongPollClient;

public interface EasyPaymentsClient extends EasyDonateClient {

    static @NotNull EasyPaymentsClient create(@NotNull String accessKey, @NotNull String userAgent, int serverId) {
        // SDK modules registration
        JDKLegacyHttpClientService.registerIfNotRegisteredYet();
        GsonSerializationService.registerIfNotRegisteredYet();

        return new SimpleEasyPaymentsClient(accessKey, userAgent, serverId);
    }

    @NotNull LongPollClient getLongPollClient();

    @NotNull VersionResponse checkForUpdates(@NotNull String version) throws HttpRequestException, HttpResponseException;

    boolean uploadReports(@NotNull EventUpdateReports updateReports) throws HttpRequestException, HttpResponseException;

}
