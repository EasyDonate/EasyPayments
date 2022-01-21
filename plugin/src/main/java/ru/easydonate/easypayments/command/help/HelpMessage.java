package ru.easydonate.easypayments.command.help;

import lombok.AccessLevel;
import lombok.Builder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.utility.StringSupplier;

import java.util.ArrayList;
import java.util.List;

@Builder(access = AccessLevel.PACKAGE, builderClassName = "Builder", buildMethodName = "create", setterPrefix = "with")
public final class HelpMessage {

    private final StringSupplier headerStub;
    private final StringSupplier lineFormat;
    private final StringSupplier footerStub;

    private final List<HelpMessageLine> lines;

    public @NotNull List<String> getAsMultiLineMessage(@NotNull Permissible receiver) {
        List<String> content = new ArrayList<>();

        if(headerStub != null)
            content.add(headerStub.get());

        if(lineFormat != null && !lines.isEmpty())
            lines.forEach(line -> line.formatAsMessageLine(receiver, lineFormat.get(), content));

        if(footerStub != null)
            content.add(footerStub.get());

        return content;
    }

    public void sendTo(@NotNull CommandSender receiver) {
        List<String> message = getAsMultiLineMessage(receiver);

        if(receiver instanceof Player)
            receiver.sendMessage(String.join("\n", message));
        else
            message.forEach(receiver::sendMessage);
    }

}
