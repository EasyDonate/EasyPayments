package ru.easydonate.easypayments.core.interceptor;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public interface InterceptorFactory {

    @NotNull FeedbackInterceptor createFeedbackInterceptor();

    @NotNull CompletableFuture<FeedbackInterceptor> createFeedbackInterceptorAsync();

}
