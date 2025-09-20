package ru.easydonate.easypayments.core.easydonate4j.extension.client;

import org.jetbrains.annotations.NotNull;
import ru.easydonate.easydonate4j.api.v3.client.EasyDonateClient;
import ru.easydonate.easydonate4j.exception.HttpRequestException;
import ru.easydonate.easydonate4j.exception.HttpResponseException;
import ru.easydonate.easydonate4j.http.client.jdk.legacy.JDKLegacyHttpClientService;
import ru.easydonate.easydonate4j.json.serialization.GsonSerializationService;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.EventUpdateReports;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.PluginStateModel;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.PluginVersionModel;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.client.LongPollClient;

import java.util.Map;

public interface EasyPaymentsClient extends EasyDonateClient {

    static @NotNull EasyPaymentsClient create(@NotNull String accessKey, @NotNull String userAgent, int serverId) {
        // SDK modules registration
        JDKLegacyHttpClientService.registerIfNotRegisteredYet();
        GsonSerializationService.registerIfNotRegisteredYet();

        return new SimpleEasyPaymentsClient(accessKey, userAgent, serverId);
    }

    @NotNull LongPollClient getLongPollClient();

    @NotNull PluginVersionModel getPluginVersion(@NotNull String currentVersion) throws HttpRequestException, HttpResponseException;

    @NotNull PluginStateModel getPluginState() throws HttpRequestException, HttpResponseException;

    boolean uploadKnownPlayers(@NotNull Map<String, Boolean> knownPlayers) throws HttpRequestException, HttpResponseException;

    boolean uploadReports(@NotNull EventUpdateReports updateReports) throws HttpRequestException, HttpResponseException;

}
