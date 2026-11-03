package ru.easydonate.easypayments.platform.paper.unrelocated.v2.interceptor;

import net.minecraft.server.dedicated.DedicatedServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.interceptor.FeedbackInterceptor;
import ru.easydonate.easypayments.core.interceptor.InterceptorFactoryBase;
import ru.easydonate.easypayments.core.platform.provider.PlatformProviderBase;

public final class PlatformInterceptorFactory extends InterceptorFactoryBase {

    public PlatformInterceptorFactory(
            @NotNull PlatformProviderBase provider,
            @NotNull String executorName,
            boolean runningFolia
    ) {
        super(provider, executorName, runningFolia);
    }

    @Override public @NotNull FeedbackInterceptor createFeedbackInterceptor() {
        DedicatedServer server = ((CraftServer) Bukkit.getServer()).getServer();
        InterceptedRconCommandSource rconCommandSource = new InterceptedRconCommandSource(server, executorName);
        InterceptedRconCommandSender rconCommandSender = new InterceptedRconCommandSender(rconCommandSource);

        // created even on Folia, so NMS linkage errors are caught by the platform validation probe
        InterceptedCommandSourceStack commandSourceStack = rconCommandSource.createCommandSourceStack();
        if (runningFolia) return rconCommandSender;

        return new InterceptedProxiedCommandSender(commandSourceStack, rconCommandSender);
    }

}
