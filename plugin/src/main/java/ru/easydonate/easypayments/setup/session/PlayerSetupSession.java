package ru.easydonate.easypayments.setup.session;

import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.setup.InteractiveSetupProvider;

@Getter
public final class PlayerSetupSession extends AbstractSetupSession {

    private final Player player;

    public PlayerSetupSession(@NotNull InteractiveSetupProvider setupProvider, @NotNull Player player) {
        super(setupProvider);
        this.player = player;
    }

    @Override
    public @NotNull CommandSender asBukkitSender() {
        return player;
    }

    @Override
    public @NotNull String getDisplayName() {
        return player.getName();
    }

    @Override
    public void sendMessage(@NotNull String message) {
        if(message != null && !message.isEmpty())
            player.sendMessage(message);
    }

}
