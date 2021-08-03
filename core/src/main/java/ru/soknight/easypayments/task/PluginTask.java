package ru.soknight.easypayments.task;

public interface PluginTask {

    boolean isWorking();

    void start();

    void restart();

    void shutdown();

}
