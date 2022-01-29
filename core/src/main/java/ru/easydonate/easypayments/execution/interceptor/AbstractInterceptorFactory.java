package ru.easydonate.easypayments.execution.interceptor;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.nms.provider.AbstractVersionedFeaturesProvider;

import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
public abstract class AbstractInterceptorFactory implements InterceptorFactory {

    protected final AbstractVersionedFeaturesProvider provider;
    protected final String executorName;
    protected final int permissionLevel;

    @Override
    public @NotNull CompletableFuture<FeedbackInterceptor> createFeedbackInterceptorAsync() {
        return CompletableFuture.supplyAsync(this::createFeedbackInterceptor, provider.getBukkitSyncExecutor());
    }

}
