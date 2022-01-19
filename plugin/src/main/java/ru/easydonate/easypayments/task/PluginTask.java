package ru.easydonate.easypayments.task;

public interface PluginTask {

    boolean isWorking();

    void start();

    void restart();

    void shutdown();

}
