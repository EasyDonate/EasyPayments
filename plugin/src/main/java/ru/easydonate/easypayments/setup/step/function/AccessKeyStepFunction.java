package ru.easydonate.easypayments.setup.step.function;

import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.EasyPaymentsPlugin;
import ru.easydonate.easypayments.core.formatting.StringFormatter;
import ru.easydonate.easypayments.setup.InteractiveSetupProvider;
import ru.easydonate.easypayments.setup.ShortAnswer;
import ru.easydonate.easypayments.setup.session.InteractiveSetupSession;

public final class AccessKeyStepFunction extends SetupStepFunction {

    public AccessKeyStepFunction(@NotNull InteractiveSetupProvider setupProvider) {
        super(setupProvider);
    }

    @Override
    public void onStepIn(@NotNull InteractiveSetupSession session) {
        String currentValue = config().getString("key");
        if (currentValue != null && !currentValue.trim().isEmpty()) {
            session.awaitShortAnswer();
            String maskedKey = StringFormatter.maskAccessKey(currentValue);
            sendMessage(session, "setup.interactive.access-key.already-specified", "%access_key%", maskedKey);
            return;
        }

        sendMessage(session, "setup.interactive.access-key.enter-new-value");
    }

    @Override
    public void applyInputValue(@NotNull InteractiveSetupSession session, @NotNull String rawInput) {
        session.setAccessKey(rawInput);
    }

    @Override
    public void acceptShortAnswer(@NotNull InteractiveSetupSession session, @NotNull ShortAnswer answer) {
        switch (answer) {
            case YES:
                sendMessage(session, "setup.interactive.access-key.enter-new-value");
                break;
            case NO:
                sendMessage(session, "setup.interactive.access-key.used-current-value");
                setupProvider.nextSetupStep(session);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean validateInput(@NotNull InteractiveSetupSession session, @NotNull String rawInput) {
        if (rawInput == null || rawInput.length() != EasyPaymentsPlugin.ACCESS_KEY_LENGTH) {
            sendMessage(session, "setup.failed.wrong-key-length");
            return false;
        }

        if (!EasyPaymentsPlugin.ACCESS_KEY_REGEX.matcher(rawInput.toLowerCase()).matches()) {
            sendMessage(session, "setup.failed.wrong-key-regex");
            return false;
        }

        return true;
    }

    @Override
    public void onValidationFail(@NotNull InteractiveSetupSession session) {
        sendMessage(session, "setup.interactive.access-key.enter-value-again");
    }

}
