package ru.easydonate.easypayments.command.easypayments.setup;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.EasyPaymentsPlugin;
import ru.easydonate.easypayments.command.CommandExecutor;
import ru.easydonate.easypayments.command.annotation.*;
import ru.easydonate.easypayments.command.exception.ExecutionException;
import ru.easydonate.easypayments.command.exception.InitializationException;
import ru.easydonate.easypayments.core.config.Configuration;
import ru.easydonate.easypayments.core.config.localized.Messages;
import ru.easydonate.easypayments.core.formatting.StringFormatter;

import java.util.List;

@Command("access-key")
@CommandAliases({"shop-key", "key"})
@Arguments("access-key")
@MinimalArgsCount(1)
@Permission("easypayments.command.setup")
public final class CommandAccessKey extends CommandExecutor {

    private final Configuration config;

    public CommandAccessKey(@NotNull Configuration config, @NotNull Messages messages) throws InitializationException {
        super(messages);
        this.config = config;
    }

    @Override
    public void executeCommand(@NotNull CommandSender sender, @NotNull List<String> args) throws ExecutionException {
        validateExecution(sender, args);

        String accessKey = args.get(0);
        if (accessKey.length() != EasyPaymentsPlugin.ACCESS_KEY_LENGTH)
            throw new ExecutionException(messages.get("setup.failed.wrong-key-length"));

        if (!EasyPaymentsPlugin.ACCESS_KEY_REGEX.matcher(accessKey).matches())
            throw new ExecutionException(messages.get("setup.failed.wrong-key-regex"));

        config.getOverrides().put(EasyPaymentsPlugin.CONFIG_KEY_ACCESS_KEY, accessKey);
        config.reload();

        String maskedKey = StringFormatter.maskAccessKey(accessKey);
        messages.getAndSend(sender, "setup.success.access-key", "%access_key%", maskedKey);

        if (config.getInt("server-id", 0) <= 0)
            messages.getAndSend(sender, "setup.tips.server-id");
    }

}
