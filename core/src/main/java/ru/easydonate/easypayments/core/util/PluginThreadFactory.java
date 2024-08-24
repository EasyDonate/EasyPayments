package ru.easydonate.easypayments.core.util;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class PluginThreadFactory implements ThreadFactory {

    private final String baseThreadName;
    private final AtomicInteger indexer;

    public PluginThreadFactory(@NotNull String baseThreadName) {
        this.baseThreadName = baseThreadName;
        this.indexer = new AtomicInteger();
    }

    @Override
    public @NotNull Thread newThread(@NotNull Runnable task) {
        return new Thread(task, String.format("EasyPayments %s #%d", baseThreadName, indexer.incrementAndGet()));
    }

}
