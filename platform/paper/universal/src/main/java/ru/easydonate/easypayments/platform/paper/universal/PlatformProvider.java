package ru.easydonate.easypayments.platform.paper.universal;

import com.mojang.authlib.GameProfile;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.EasyPayments;
import ru.easydonate.easypayments.core.interceptor.InterceptorFactory;
import ru.easydonate.easypayments.core.platform.provider.PlatformProviderBase;
import ru.easydonate.easypayments.core.platform.scheduler.PlatformScheduler;
import ru.easydonate.easypayments.platform.paper.universal.interceptor.PlatformInterceptorFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@Getter
public final class PlatformProvider extends PlatformProviderBase {

    private static final @NotNull MethodHandle CraftServer$getOfflinePlayer;

    private final String name;

    public PlatformProvider(
            @NotNull EasyPayments plugin,
            @NotNull PlatformScheduler scheduler,
            @NotNull String executorName,
            int permissionLevel,
            boolean runningFolia
    ) {
        super(plugin, scheduler, executorName, permissionLevel);
        this.name = runningFolia ? "Folia Universal" : "Paper Universal";
    }

    @Override
    protected @NotNull OfflinePlayer createOfflinePlayer(@NotNull String name) {
        var uuid = plugin.getServer().getPlayerUniqueId(name);
        if (uuid == null)
            uuid = createOfflineUUID(name);

        try {
            var profile = new GameProfile(uuid, name);
            var result = CraftServer$getOfflinePlayer.invoke(plugin.getServer(), profile);
            if (result instanceof OfflinePlayer cast)
                return cast;

            throw new RuntimeException("CraftServer#getOfflinePlayer(GameProfile) returned '%s' - it isn't an OfflinePlayer!".formatted(result));
        } catch (Throwable ex) {
            throw new RuntimeException("Unable to invoke CraftServer#getOfflinePlayer(GameProfile)", ex);
        }
    }

    @Override
    protected @NotNull InterceptorFactory createInterceptorFactory(@NotNull String executorName, int permissionLevel) {
        return new PlatformInterceptorFactory(this, executorName, permissionLevel);
    }

    static {
        try {
            var CraftServer = Bukkit.getServer().getClass();
            var methodType = MethodType.methodType(OfflinePlayer.class, GameProfile.class);
            CraftServer$getOfflinePlayer = MethodHandles.lookup().findVirtual(CraftServer, "getOfflinePlayer", methodType);
        } catch (Exception ex) {
            throw new RuntimeException("Unable to find CraftServer#getOfflinePlayer(GameProfile)", ex);
        }
    }

}
