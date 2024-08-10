package ru.easydonate.easypayments.command.help;

import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.config.localized.Messages;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class HelpMessageFactory {

    private final Messages messages;
    private final List<HelpMessageLine> lines;

    private String argumentKeyFormat;
    private String descriptionKeyFormat;
    private String permissionFormat;

    private Supplier<String> headerStub;
    private Supplier<String> lineFormat;
    private Supplier<String> footerStub;

    private HelpMessageFactory(@NotNull Messages messages) {
        this.messages = messages;
        this.lines = new ArrayList<>();
    }

    public static @NotNull HelpMessageFactory newFactory(@NotNull Messages messages) {
        return new HelpMessageFactory(messages);
    }

    public @NotNull HelpMessage constructMessage() {
        return HelpMessage.builder()
                .withHeaderStub(headerStub)
                .withLineFormat(lineFormat)
                .withFooterStub(footerStub)
                .withLines(lines)
                .create();
    }

    public @NotNull HelpMessageFactory addLine(@NotNull HelpMessageLine line) {
        lines.add(line);
        return this;
    }

    public @NotNull HelpMessageLine newLine() {
        return new HelpMessageLine(this);
    }

    public @NotNull HelpMessageFactory withArgumentKeyFormat(@NotNull String argumentKeyFormat) {
        this.argumentKeyFormat = argumentKeyFormat;
        return this;
    }

    public @NotNull HelpMessageFactory withDescriptionKeyFormat(@NotNull String descriptionKeyFormat) {
        this.descriptionKeyFormat = descriptionKeyFormat;
        return this;
    }

    public @NotNull HelpMessageFactory withPermissionFormat(@NotNull String permissionFormat) {
        this.permissionFormat = permissionFormat;
        return this;
    }

    public @NotNull HelpMessageFactory withHeader(@NotNull String header) {
        this.headerStub = () -> header;
        return this;
    }

    public @NotNull HelpMessageFactory withHeaderFrom(@NotNull String key) {
        this.headerStub = () -> messages.get(key);
        return this;
    }

    public @NotNull HelpMessageFactory withLineFormat(@NotNull String lineFormat) {
        this.lineFormat = () -> lineFormat;
        return this;
    }

    public @NotNull HelpMessageFactory withLineFormatFrom(@NotNull String key) {
        this.lineFormat = () -> messages.get(key);
        return this;
    }

    public @NotNull HelpMessageFactory withFooter(@NotNull String footer) {
        this.footerStub = () -> footer;
        return this;
    }

    public @NotNull HelpMessageFactory withFooterFrom(@NotNull String key) {
        this.footerStub = () -> messages.get(key);
        return this;
    }

    @NotNull Messages getMessages() {
        return messages;
    }

    @NotNull Supplier<String> getArgument(@NotNull String key) {
        String path = argumentKeyFormat != null ? String.format(argumentKeyFormat, key) : key;
        return () -> messages.getOrDefault(path, path);
    }

    @NotNull Supplier<String> getDescription(@NotNull String key) {
        String path = descriptionKeyFormat != null ? String.format(descriptionKeyFormat, key) : key;
        return () -> messages.getOrDefault(path, path);
    }

    @NotNull String getPermission(@NotNull String key) {
        return permissionFormat != null ? String.format(permissionFormat, key) : key;
    }

}
