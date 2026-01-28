package ru.easydonate.easypayments.platform.spigot.internals.interceptor;

import net.minecraft.server.v1_13_R1.MinecraftServer;
import net.minecraft.server.v1_13_R1.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_13_R1.CraftServer;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.interceptor.FeedbackInterceptor;
import ru.easydonate.easypayments.core.interceptor.InterceptorFactoryBase;
import ru.easydonate.easypayments.core.platform.provider.PlatformProviderBase;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class PlatformInterceptorFactory extends InterceptorFactoryBase {

    private final WorldServer worldServer;

    public PlatformInterceptorFactory(
            @NotNull PlatformProviderBase provider,
            @NotNull String executorName,
            int permissionLevel
    ) {
        super(provider, executorName, permissionLevel);
        this.worldServer = getWorldServer();
    }

    @Override
    public @NotNull FeedbackInterceptor createFeedbackInterceptor() {
        InterceptedCommandListener commandListener = new InterceptedCommandListener(executorName);
        return new InterceptedProxiedSender(
                new InterceptedCommandListenerWrapper(commandListener, worldServer, executorName, permissionLevel),
                commandListener
        );
    }

    private WorldServer getWorldServer() {
        MinecraftServer minecraftServer = getMinecraftServer();

        Class<?> MinecraftServer = minecraftServer.getClass();
        String packageName = MinecraftServer.getPackage().getName();

        try {
            // [1.16 R1]: #getWorldServer(ResourceKey)
            Class<?> ResourceKey = Class.forName(packageName + ".ResourceKey");

            Class<?> World = Class.forName(packageName + ".World");
            Field World$OVERWORLD = World.getField("OVERWORLD");
            Object overworld = World$OVERWORLD.get(null);

            Method MinecraftServer$getWorldServer = MinecraftServer.getMethod("getWorldServer", ResourceKey);
            return (WorldServer) MinecraftServer$getWorldServer.invoke(minecraftServer, overworld);
        } catch (Exception ignored1_16_R1) {
            try {
                // [1.13 R2]: #getWorldServer(DimensionManager)
                Class<?> DimensionManager = Class.forName(packageName + ".DimensionManager");
                Field DimensionManager$OVERWORLD = DimensionManager.getField("OVERWORLD");
                Object overworld = DimensionManager$OVERWORLD.get(null);

                Method MinecraftServer$getWorldServer = MinecraftServer.getMethod("getWorldServer", DimensionManager);
                return (WorldServer) MinecraftServer$getWorldServer.invoke(minecraftServer, overworld);
            } catch (Exception ignored1_13_R2) {
                try {
                    // [1.13 R1]: #getWorldServer(int)
                    Method MinecraftServer$getWorldServer = MinecraftServer.getMethod("getWorldServer", int.class);
                    return (WorldServer) MinecraftServer$getWorldServer.invoke(minecraftServer, 0);
                } catch (Exception ex) {
                    throw new RuntimeException("Couldn't resolve method MinecraftServer#getWorldServer!");
                }
            }
        }
    }

    private MinecraftServer getMinecraftServer() {
        CraftServer craftServer = (CraftServer) Bukkit.getServer();

        try {
            // [1.14 R1]: #getServer() returns DedicatedServer instead of MinecraftServer
            Class<?> CraftServer = craftServer.getClass();
            Method CraftServer$getServer = CraftServer.getMethod("getServer");
            return (MinecraftServer) CraftServer$getServer.invoke(craftServer);
        } catch (Exception ex) {
            throw new RuntimeException("Couldn't resolve method CraftServer#getServer!");
        }
    }

}
