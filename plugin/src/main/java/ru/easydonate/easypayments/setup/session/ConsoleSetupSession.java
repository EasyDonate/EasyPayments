package ru.easydonate.easypayments.setup.session;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.setup.InteractiveSetupProvider;

public final class ConsoleSetupSession extends AbstractSetupSession {

    public static final String DISPLAY_NAME = "#console";

    public ConsoleSetupSession(@NotNull InteractiveSetupProvider setupProvider) {
        super(setupProvider);
    }

    @Override
    public @NotNull CommandSender asBukkitSender() {
        return Bukkit.getConsoleSender();
    }

    @Override
    public @NotNull String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public void sendMessage(@NotNull String message) {
        if (message != null && !message.isEmpty())
            Bukkit.getConsoleSender().sendMessage(message.split("\n"));
    }

}
