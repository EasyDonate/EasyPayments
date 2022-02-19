package ru.easydonate.easypayments.setup.session;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.exception.UnsupportedCallerException;
import ru.easydonate.easypayments.setup.InteractiveSetupProvider;
import ru.easydonate.easypayments.setup.InteractiveSetupStep;
import ru.easydonate.easypayments.setup.ShortAnswer;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

public interface InteractiveSetupSession {

    String ACCESS_KEY_PERSISTENT_KEY = "access-key";
    String SERVER_ID_PERSISTENT_KEY = "server-id";

    static @NotNull InteractiveSetupSession create(
            @NotNull InteractiveSetupProvider setupProvider,
            @NotNull CommandSender sender
    ) throws UnsupportedCallerException {
        if(sender instanceof Player) {
            return new PlayerSetupSession(setupProvider, (Player) sender);
        } else if(sender instanceof ConsoleCommandSender) {
            return new ConsoleSetupSession(setupProvider);
        } else {
            throw new UnsupportedCallerException();
        }
    }

    void initialize();

    @NotNull CommandSender asBukkitSender();

    @NotNull String getDisplayName();

    @NotNull InteractiveSetupStep getCurrentStep();

    @NotNull InteractiveSetupStep nextStep();

    void awaitShortAnswer();

    boolean isAwaitingShortAnswer();

    void acceptShortAnswer(@NotNull ShortAnswer shortAnswer);

    void sendMessage(@NotNull String message);

    <T> @NotNull Optional<T> getPersistentData(@NotNull String key);

    <T> @NotNull OptionalInt getPersistentIntData(@NotNull String key);

    <T> @NotNull OptionalLong getPersistentLongData(@NotNull String key);

    <T> @NotNull OptionalDouble getPersistentDoubleData(@NotNull String key);

    void savePersistentData(@NotNull String key, @Nullable Object data);

    @NotNull Optional<String> getAccessKey();

    void setAccessKey(@NotNull String accessKey);

    @NotNull OptionalInt getServerId();

    void setServerId(int serverId);

}
