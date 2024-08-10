package ru.easydonate.easypayments.setup.step.function;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.core.config.Configuration;
import ru.easydonate.easypayments.setup.InteractiveSetupProvider;
import ru.easydonate.easypayments.setup.ShortAnswer;
import ru.easydonate.easypayments.setup.session.InteractiveSetupSession;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class SetupStepFunction {

    protected final InteractiveSetupProvider setupProvider;

    public void onStepIn(@NotNull InteractiveSetupSession session) {
        // method overriding isn't always required
    }

    public void onStepOut(@NotNull InteractiveSetupSession session) {
        // method overriding isn't always required
    }

    public boolean validateInput(@NotNull InteractiveSetupSession session, @NotNull String rawInput) {
        // method overriding isn't always required
        return true;
    }

    public void onValidationFail(@NotNull InteractiveSetupSession session) {
        // method overriding isn't always required
    }

    public void applyInputValue(@NotNull InteractiveSetupSession session, @NotNull String rawInput) {
        // method overriding isn't always required
    }

    public void acceptShortAnswer(@NotNull InteractiveSetupSession session, @NotNull ShortAnswer answer) {
        // method overriding isn't always required
    }

    protected @NotNull Configuration config() {
        return setupProvider.getConfig();
    }

    protected void sendMessage(@NotNull InteractiveSetupSession session, @NotNull String key, @Nullable Object... replaces) {
        setupProvider.getMessages().getAndSend(session::sendMessage, key, replaces);
    }

}
