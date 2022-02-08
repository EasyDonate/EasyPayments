package ru.easydonate.easypayments.easydonate4j.longpoll.client;

import org.jetbrains.annotations.NotNull;
import ru.easydonate.easydonate4j.api.v3.exception.ApiResponseFailureException;
import ru.easydonate.easydonate4j.api.v3.response.ApiResponse;
import ru.easydonate.easydonate4j.api.v3.response.ErrorResponse;
import ru.easydonate.easydonate4j.api.v3.response.ResponseContentDeserializer;
import ru.easydonate.easydonate4j.exception.HttpRequestException;
import ru.easydonate.easydonate4j.exception.HttpResponseException;
import ru.easydonate.easydonate4j.http.Headers;
import ru.easydonate.easydonate4j.http.client.HttpClient;
import ru.easydonate.easydonate4j.http.client.jdk.legacy.JDKLegacyHttpClientService;
import ru.easydonate.easydonate4j.http.request.EasyHttpRequest;
import ru.easydonate.easydonate4j.http.response.EasyHttpResponse;
import ru.easydonate.easydonate4j.json.serialization.GsonSerializationService;
import ru.easydonate.easydonate4j.json.serialization.implementation.registry.JsonModelsGroup;
import ru.easydonate.easydonate4j.module.ModuleRegistrator;
import ru.easydonate.easypayments.easydonate4j.longpoll.data.model.EventUpdates;
import ru.easydonate.easypayments.easydonate4j.longpoll.response.GetUpdatesListResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public final class LongPollClient {

    public static final String API_ENDPOINT = "https://ep.easydonate.ru";
    public static final String HEADER_SHOP_KEY = "X-Shop-Key";
    public static final String HEADER_SERVER_ID = "X-Server-Id";
    public static final long READ_TIMEOUT = 60000;

    private final Headers defaultHeaders;
    private final HttpClient httpClient;
    private final ExecutorService asyncExecutorService;

    private LongPollClient(@NotNull String accessKey, int serverId, @NotNull String userAgent) {
        this.defaultHeaders = new Headers()
                .set(HEADER_SHOP_KEY, accessKey)
                .set(HEADER_SERVER_ID, serverId);

        this.httpClient = ModuleRegistrator.httpClientService().buildClient()
                .setResponseTimeout(READ_TIMEOUT)
                .setReadTimeout(READ_TIMEOUT)
                .setUserAgent(userAgent)
                .create();

        this.asyncExecutorService = Executors.newCachedThreadPool();
    }

    public static @NotNull LongPollClient create(@NotNull String accessKey, int serverId, @NotNull String userAgent) {
        GsonSerializationService.registerIfNotRegisteredYet();
        JDKLegacyHttpClientService.registerIfNotRegisteredYet();

        ModuleRegistrator.jsonSerializationService().registerImplementationAliasesGroup(JsonModelsGroup.API_V3_MODELS);
        return new LongPollClient(accessKey, serverId, userAgent);
    }

    public @NotNull Headers getDefaultHeaders() {
        return defaultHeaders;
    }

    public void shutdown() {
        if(asyncExecutorService != null)
            asyncExecutorService.shutdown();
    }

    public @NotNull CompletableFuture<EventUpdates> getUpdatesList() {
        return requestAsync(EasyHttpRequest.builder(httpClient, HttpClient.Method.GET), GetUpdatesListResponse.class);
    }

    public @NotNull EventUpdates getUpdatesListSync() throws HttpRequestException, HttpResponseException {
        try {
            return request(EasyHttpRequest.builder(httpClient, HttpClient.Method.GET), GetUpdatesListResponse.class);
        } catch (ApiResponseFailureException responseFailureException) {
            ErrorResponse errorResponse = responseFailureException.getErrorResponse();
            if(errorResponse.getErrorCode() == 0) {
                return EventUpdates.EMPTY;
            } else {
                throw responseFailureException;
            }
        }
    }

    public @NotNull EasyHttpResponse makeCustomRequest(
            @NotNull HttpClient.Method method,
            @NotNull Consumer<EasyHttpRequest.Builder> builderPatcher
    ) throws HttpRequestException {
        EasyHttpRequest.Builder builder = EasyHttpRequest.builder(httpClient, method)
                .setApiEndpoint(API_ENDPOINT)
                .setHeaders(defaultHeaders);

        builderPatcher.accept(builder);

        EasyHttpRequest request = builder.build();
        return request.execute();
    }

    private <R extends ApiResponse<T>, T> @NotNull CompletableFuture<T> requestAsync(
            @NotNull EasyHttpRequest.Builder builder,
            @NotNull Class<R> responseObjectType
    ) {
        CompletableFuture<T> future = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            try {
                T result = request(builder, responseObjectType);
                future.complete(result);
            } catch (HttpRequestException | HttpResponseException ex) {
                future.completeExceptionally(ex);
            }
        }, asyncExecutorService);

        return future;
    }

    private <R extends ApiResponse<T>, T> @NotNull T request(
            @NotNull EasyHttpRequest.Builder builder,
            @NotNull Class<R> responseObjectType
    ) throws HttpRequestException, HttpResponseException {
        EasyHttpRequest request = builder
                .setApiEndpoint(API_ENDPOINT)
                .setHeaders(defaultHeaders)
                .build();

        EasyHttpResponse response = request.execute();
        return ResponseContentDeserializer.deserializeResponseContent(responseObjectType, response);
    }

}
