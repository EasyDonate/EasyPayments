package ru.easydonate.easypayments.core.platform.provider;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.EasyPayments;
import ru.easydonate.easypayments.core.interceptor.InterceptorFactory;
import ru.easydonate.easypayments.core.platform.scheduler.PlatformScheduler;

import java.util.UUID;

public interface PlatformProvider {

    @NotNull String getProviderId();

    @NotNull EasyPayments getPlugin();

    @NotNull ImplementationType getImplementationType();

    @NotNull PlatformType getPlatformType();

    @NotNull PlatformScheduler getScheduler();

    @NotNull InterceptorFactory getInterceptorFactory();

    @NotNull UUID resolvePlayerId(@NotNull String name);

    boolean isTaskCancelled(@NotNull BukkitTask asyncTask);

    default @NotNull String getDisplayName() {
        return getPlatformType().getName() + ' ' + getImplementationType().getName();
    }

    @Getter
    @AllArgsConstructor
    enum ImplementationType {

        INTERNALS   ("internals",   "Internals"),
        UNIVERSAL   ("universal",   "Universal"),
        ;

        private final @NotNull String key;
        private final @NotNull String name;

    }

    @Getter
    @AllArgsConstructor
    enum PlatformType {

        SPIGOT  ("spigot",  "Spigot"),
        PAPER   ("paper",   "Paper"),
        FOLIA   ("folia",   "Folia"),
        ;

        private final @NotNull String key;
        private final @NotNull String name;

        public static @NotNull PlatformType with(boolean runningFolia) {
            return runningFolia ? FOLIA : PAPER;
        }

    }

}
