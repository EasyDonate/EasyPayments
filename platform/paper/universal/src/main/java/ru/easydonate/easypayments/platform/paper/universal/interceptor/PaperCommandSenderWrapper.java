package ru.easydonate.easypayments.platform.paper.universal.interceptor;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import ru.easydonate.easypayments.core.interceptor.FeedbackInterceptor;

import java.util.ArrayList;
import java.util.List;

@Getter
final class PaperCommandSenderWrapper implements FeedbackInterceptor {

    private static final PlainTextComponentSerializer COMPONENT_SERIALIZER = PlainTextComponentSerializer.plainText();

    private final CommandSender commandSender;
    private final List<String> feedbackMessages;

    public PaperCommandSenderWrapper() {
        this.commandSender = Bukkit.getServer().createCommandSender(this::addFeedbackMessage);
        this.feedbackMessages = new ArrayList<>();
    }

    private void addFeedbackMessage(Component message) {
        this.feedbackMessages.add(COMPONENT_SERIALIZER.serialize(message));
    }

}
