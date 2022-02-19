package ru.easydonate.easypayments.setup;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.EasyPaymentsPlugin;
import ru.easydonate.easypayments.config.Configuration;
import ru.easydonate.easypayments.config.Messages;
import ru.easydonate.easypayments.exception.UnsupportedCallerException;
import ru.easydonate.easypayments.setup.session.InteractiveSetupSession;
import ru.easydonate.easypayments.setup.step.function.*;
import ru.easydonate.easypayments.utility.StringMasker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class InteractiveSetupProvider {

    private final EasyPaymentsPlugin plugin;
    private final Configuration config;
    private final Messages messages;

    private final Map<CommandSender, InteractiveSetupSession> sessions;
    private final Map<InteractiveSetupStep, SetupStepFunction> stepFunctions;

    public InteractiveSetupProvider(
            @NotNull EasyPaymentsPlugin plugin,
            @NotNull Configuration config,
            @NotNull Messages messages
    ) {
        this.plugin = plugin;
        this.config = config;
        this.messages = messages;

        this.sessions = new HashMap<>();
        this.stepFunctions = new HashMap<>();

        this.stepFunctions.put(InteractiveSetupStep.START, new StartStepFunction(this));
        this.stepFunctions.put(InteractiveSetupStep.SET_ACCESS_KEY, new AccessKeyStepFunction(this));
        this.stepFunctions.put(InteractiveSetupStep.SET_SERVER_ID, new ServerIdStepFunction(this));
        this.stepFunctions.put(InteractiveSetupStep.FINISH, new FinishStepFunction(this));
    }

    public @NotNull Configuration getConfig() {
        return config;
    }

    public @NotNull Messages getMessages() {
        return messages;
    }

    public @NotNull ShortAnswer defineShortAnswer(@NotNull String rawAnswer) {
        if(rawAnswer == null || rawAnswer.isEmpty())
            return ShortAnswer.UNDEFINED;

        String answer = rawAnswer.trim();
        if(answer.isEmpty())
            return ShortAnswer.UNDEFINED;

        List<String> agreeAnswers = config.getStringList("short-answers.agree");
        if(!agreeAnswers.isEmpty() && agreeAnswers.stream().anyMatch(answer::equalsIgnoreCase))
            return ShortAnswer.YES;

        List<String> refuseAnswers = config.getStringList("short-answers.refuse");
        if(!refuseAnswers.isEmpty() && refuseAnswers.stream().anyMatch(answer::equalsIgnoreCase))
            return ShortAnswer.NO;

        return ShortAnswer.UNDEFINED;
    }

    public synchronized void currentStepIn(@NotNull InteractiveSetupSession session) {
        getCurrentStepFunction(session).onStepIn(session);
    }

    public synchronized void currentStepOut(@NotNull InteractiveSetupSession session) {
        getCurrentStepFunction(session).onStepOut(session);
    }

    public synchronized void nextSetupStep(@NotNull InteractiveSetupSession session) {
        synchronized (stepFunctions) {
            currentStepOut(session);
            InteractiveSetupStep step = session.nextStep();

            if(step.isFinished()) {
                sessions.remove(session.asBukkitSender());

                session.getAccessKey().ifPresent(key -> {
                    config.updateExistingFile(EasyPaymentsPlugin.CONFIG_ACCESS_KEY_REGEX, key);
                    String maskedKey = StringMasker.maskAccessKey(key);
                    messages.getAndSend(session::sendMessage, "setup.success.access-key", "%access_key%", maskedKey);
                });

                session.getServerId().ifPresent(serverId -> {
                    config.updateExistingFile(EasyPaymentsPlugin.CONFIG_SERVER_ID_REGEX, serverId);
                    messages.getAndSend(session::sendMessage, "setup.success.server-id", "%server_id%", serverId);
                });

                try {
                    plugin.reload();
                } catch (Exception ignored) {
                }
            }

            currentStepIn(session);
        }
    }

    public @NotNull SetupStepFunction getCurrentStepFunction(@NotNull InteractiveSetupSession session) {
        return stepFunctions.get(session.getCurrentStep());
    }

    public boolean handleChatMessage(@NotNull CommandSender sender, @Nullable String message) {
        return getSession(sender).map(session -> handleChatMessage(session, message)).orElse(false);
    }

    public boolean handleChatMessage(@NotNull InteractiveSetupSession session, @Nullable String message) {
        if(message == null || message.isEmpty())
            return false;

        List<String> exitPhrases = config.getStringList("exit-phrases");
        if(!exitPhrases.isEmpty() && exitPhrases.stream().anyMatch(message::equalsIgnoreCase)) {
            messages.getAndSend(session::sendMessage, "setup.exit");
            closeSession(session.asBukkitSender());
            return true;
        }

        if(session.isAwaitingShortAnswer()) {
            ShortAnswer shortAnswer = defineShortAnswer(message);
            if(shortAnswer.isUndefined()) {
                messages.getAndSend(session::sendMessage, "setup.failed.wrong-short-answer");
            } else {
                session.acceptShortAnswer(shortAnswer);
            }
            return true;
        }

        SetupStepFunction stepFunction = getCurrentStepFunction(session);

        boolean validated = stepFunction.validateInput(session, message);
        if(validated) {
            stepFunction.applyInputValue(session, message);
            nextSetupStep(session);
        } else {
            stepFunction.onValidationFail(session);
        }
        return true;
    }

    public void closeAll() {
        synchronized (sessions) {
            sessions.clear();
        }
    }

    public @NotNull Optional<InteractiveSetupSession> closeSession(@NotNull CommandSender sender) {
        synchronized (sessions) {
            return Optional.ofNullable(sessions.remove(sender));
        }
    }

    public @NotNull Optional<InteractiveSetupSession> getSession(@NotNull CommandSender sender) {
        synchronized (sessions) {
            return Optional.ofNullable(sessions.get(sender));
        }
    }

    public boolean hasSession(@NotNull CommandSender sender) {
        synchronized (sessions) {
            return sessions.containsKey(sender);
        }
    }

    public @NotNull InteractiveSetupSession openSession(@NotNull CommandSender sender, boolean returnExisting) throws UnsupportedCallerException {
        Optional<InteractiveSetupSession> existingSession = getSession(sender);
        if(existingSession.isPresent() && returnExisting)
            return existingSession.get();

        synchronized (sessions) {
            InteractiveSetupSession session = InteractiveSetupSession.create(this, sender);
            sessions.put(sender, session);
            return session;
        }
    }

}
