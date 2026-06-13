package ru.easydonate.easypayments.core.interceptor;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.platform.provider.PlatformProviderBase;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
public abstract class InterceptorFactoryBase implements InterceptorFactory {

    public static final @NotNull SocketAddress LOOPBACK_ADDRESS = new InetSocketAddress(InetAddress.getLoopbackAddress(), 0);

    protected final @NotNull PlatformProviderBase provider;
    protected final @NotNull String executorName;
    protected final @NotNull boolean runningFolia;

    public InterceptorFactoryBase(@NotNull PlatformProviderBase provider, @NotNull String executorName) {
        this(provider, executorName, false);
    }

    @Override
    public @NotNull CompletableFuture<FeedbackInterceptor> createFeedbackInterceptorAsync() {
        return CompletableFuture.supplyAsync(this::createFeedbackInterceptor, provider.getSyncExecutor());
    }

}
