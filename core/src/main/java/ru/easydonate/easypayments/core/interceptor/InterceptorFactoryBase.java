package ru.easydonate.easypayments.core.interceptor;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.platform.provider.PlatformProviderBase;

import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
public abstract class InterceptorFactoryBase implements InterceptorFactory {

    protected final PlatformProviderBase provider;
    protected final String executorName;

    @Override
    public @NotNull CompletableFuture<FeedbackInterceptor> createFeedbackInterceptorAsync() {
        return CompletableFuture.supplyAsync(this::createFeedbackInterceptor, provider.getSyncExecutor());
    }

}
