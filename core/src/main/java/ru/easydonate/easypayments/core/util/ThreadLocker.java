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
        try {
            Thread.sleep(timeInMillis);
        } catch (InterruptedException ignored) {
        }
    }

}
