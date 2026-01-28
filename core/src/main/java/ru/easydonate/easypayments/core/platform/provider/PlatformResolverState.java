package ru.easydonate.easypayments.core.platform.provider;

import lombok.*;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.config.Configuration;

import java.util.Objects;

@Value
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PlatformResolverState {

    public static final @NotNull String DEFAULT_EXECUTOR_NAME = "@EasyPayments";
    public static final boolean DEFAULT_FORCE_INTERNALS = false;
    public static final int DEFAULT_PERMISSION_LEVEL = 4;

    public static final @NotNull PlatformResolverState DEFAULT = new PlatformResolverState(
            DEFAULT_EXECUTOR_NAME,
            DEFAULT_FORCE_INTERNALS
    );

    @NotNull String executorName;
    boolean forceInternals;

    public static @NotNull PlatformResolverState from(@NotNull Configuration config) {
        return builder()
                .executorName(config.getString("executor-name", DEFAULT_EXECUTOR_NAME))
                .forceInternals(config.getBoolean("force-internals-backed-platform", DEFAULT_FORCE_INTERNALS))
                .build();
    }

    public boolean requiresPlatformResolve(@NotNull PlatformResolverState other) {
        return forceInternals != other.forceInternals;
    }

    public boolean requiresPlatformUpdate(@NotNull PlatformResolverState other) {
        return !Objects.equals(executorName, other.executorName);
    }

}
