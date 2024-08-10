package ru.easydonate.easypayments.listener;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.setup.InteractiveSetupProvider;
import ru.easydonate.easypayments.core.util.Reflection;

import java.lang.reflect.Method;

public final class CommandPreProcessListener implements Listener {

    private static final Method setCancelled = Reflection.getMethod(ServerCommandEvent.class, "setCancelled", boolean.class);

    private final InteractiveSetupProvider setupProvider;

    public CommandPreProcessListener(@NotNull Plugin plugin, @NotNull InteractiveSetupProvider setupProvider) {
        this.setupProvider = setupProvider;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAsyncPlayerChat(@NotNull AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        Player player = event.getPlayer();

        if(setupProvider.handleChatMessage(player, message))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onServerCommand(@NotNull ServerCommandEvent event) {
        String message = event.getCommand();
        CommandSender sender = event.getSender();

        if(setupProvider.handleChatMessage(sender, message)) {
            event.setCommand(null);
            Reflection.invokeVoidMethod(setCancelled, event, true);
        }
    }

}
