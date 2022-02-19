package ru.easydonate.easypayments.setup.step.function;

import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.setup.InteractiveSetupProvider;
import ru.easydonate.easypayments.setup.ShortAnswer;
import ru.easydonate.easypayments.setup.session.InteractiveSetupSession;

public final class ServerIdStepFunction extends SetupStepFunction {

    public ServerIdStepFunction(@NotNull InteractiveSetupProvider setupProvider) {
        super(setupProvider);
    }

    @Override
    public void onStepIn(@NotNull InteractiveSetupSession session) {
        int currentValue = config().getInt("server-id", 0);
        if(currentValue > 0) {
            session.awaitShortAnswer();
            sendMessage(session, "setup.interactive.server-id.already-specified", "%server_id%", currentValue);
            return;
        }

        sendMessage(session, "setup.interactive.server-id.enter-new-value");
    }

    @Override
    public void applyInputValue(@NotNull InteractiveSetupSession session, @NotNull String rawInput) {
        session.setServerId(Integer.parseInt(rawInput));
    }

    @Override
    public void acceptShortAnswer(@NotNull InteractiveSetupSession session, @NotNull ShortAnswer answer) {
        switch (answer) {
            case YES:
                sendMessage(session, "setup.interactive.server-id.enter-new-value");
                break;
            case NO:
                sendMessage(session, "setup.interactive.server-id.used-current-value");
                setupProvider.nextSetupStep(session);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean validateInput(@NotNull InteractiveSetupSession session, @NotNull String rawInput) {
        try {
            int asInt = Integer.parseInt(rawInput);
            if(asInt > 0) {
                return true;
            }
        } catch (NumberFormatException ignored) {
        }

        sendMessage(session, "setup.failed.wrong-server-id");
        return false;
    }

    @Override
    public void onValidationFail(@NotNull InteractiveSetupSession session) {
        sendMessage(session, "setup.interactive.server-id.enter-value-again");
    }

}
