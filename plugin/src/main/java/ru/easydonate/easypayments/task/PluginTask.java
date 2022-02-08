package ru.easydonate.easypayments.task;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public interface PluginTask {

    boolean isActive();

    boolean isWorking();

    void start();

    void restart();

    void shutdown();

    @NotNull CompletableFuture<Void> shutdownAsync();

}
