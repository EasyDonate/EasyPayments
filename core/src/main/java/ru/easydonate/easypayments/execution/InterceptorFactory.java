package ru.easydonate.easypayments.execution;

import org.jetbrains.annotations.NotNull;

public interface InterceptorFactory {

    @NotNull FeedbackInterceptor createFeedbackInterceptor();

}
