package ru.easydonate.easypayments.utility;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public final class ThreadLocker {

    public static void lockUninterruptive(long duration, @NotNull TimeUnit timeUnit) {
        lockUninterruptive(timeUnit.toMillis(duration));
    }

    public static void lockUninterruptive(long timeInMillis) {
        try {
            Thread.sleep(timeInMillis);
        } catch (InterruptedException ignored) {
        }
    }

}
