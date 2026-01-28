package ru.easydonate.easypayments.platform.paper.universal.interceptor;

import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.interceptor.FeedbackInterceptor;
import ru.easydonate.easypayments.core.interceptor.InterceptorFactoryBase;
import ru.easydonate.easypayments.core.platform.provider.PlatformProviderBase;

public final class PlatformInterceptorFactory extends InterceptorFactoryBase {

    public PlatformInterceptorFactory(@NotNull PlatformProviderBase provider, @NotNull String executorName) {
        super(provider, executorName);
    }

    @Override
    public @NotNull FeedbackInterceptor createFeedbackInterceptor() {
        return new PaperCommandSenderWrapper();
    }

}
