package ru.easydonate.easypayments.command.easypayments;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.EasyPaymentsPlugin;
import ru.easydonate.easypayments.command.CommandExecutor;
import ru.easydonate.easypayments.command.annotation.Command;
import ru.easydonate.easypayments.command.annotation.CommandAliases;
import ru.easydonate.easypayments.command.annotation.Permission;
import ru.easydonate.easypayments.command.exception.ExecutionException;
import ru.easydonate.easypayments.command.exception.InitializationException;
import ru.easydonate.easypayments.core.config.localized.Messages;
import ru.easydonate.easypayments.core.formatting.StringFormatter;

import java.util.List;

@Command("status")
@CommandAliases({"i", "info"})
@Permission("easypayments.command.status")
public final class CommandStatus extends CommandExecutor {

    private final EasyPaymentsPlugin plugin;

    public CommandStatus(
            @NotNull EasyPaymentsPlugin plugin,
            @NotNull Messages messages
    ) throws InitializationException {
        super(messages);
        this.plugin = plugin;
    }

    @Override
    public void executeCommand(@NotNull CommandSender sender, @NotNull List<String> args) throws ExecutionException {
        validateExecution(sender, args);

        boolean isPluginEnabled = EasyPaymentsPlugin.isPluginEnabled();
        boolean isStorageAvailable = EasyPaymentsPlugin.isStorageAvailable();

        String accessKey = StringFormatter.maskAccessKey(plugin.getAccessKey());
        int serverId = plugin.getServerId();
        int permissionLevel = plugin.getPermissionLevel();

        messages.getAndSend(sender, "status.message",
                "%plugin_version%", plugin.getDescription().getVersion(),
                "%plugin_status%", wrapBoolean(isPluginEnabled, "status.working", "status.unconfigured"),
                "%storage_status%", wrapBoolean(isStorageAvailable, "storage.available", "storage.unavailable"),
                "%access_key%", accessKey != null && !accessKey.isEmpty() ? accessKey : getNoValueStub(),
                "%server_id%", serverId > 0 ? ("#" + serverId) : getNoValueStub(),
                "%permission_level%", permissionLevel > 0 ? permissionLevel : getNoValueStub()
        );
    }

    private @NotNull String getNoValueStub() {
        return messages.get("status.no-value-stub");
    }

    private @NotNull String wrapBoolean(boolean value, @NotNull String trueKey, @NotNull String falseKey) {
        return messages.get("status." + (value ? trueKey : falseKey));
    }

}
