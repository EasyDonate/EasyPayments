package ru.easydonate.easypayments.execution.interceptor;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface FeedbackInterceptor {

    @NotNull List<String> getFeedbackMessages();

}
