package ru.easydonate.easypayments.core.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

@UtilityClass
public class ThreadLocker {

    public void lockUninterruptive(long duration, @NotNull TimeUnit timeUnit) {
        lockUninterruptive(timeUnit.toMillis(duration));
    }

    public void lockUninterruptive(long timeInMillis) {
        doUninterruptive(() -> Thread.sleep(timeInMillis));
    }

    public void doUninterruptive(Action action) {
        try {
            action.perform();
        } catch (InterruptedException ignored) {
        }
    }

    @FunctionalInterface
    public interface Action {

        void perform() throws InterruptedException;

    }

}
