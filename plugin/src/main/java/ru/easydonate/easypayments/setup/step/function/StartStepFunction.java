package ru.easydonate.easypayments.setup.step.function;

import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.setup.InteractiveSetupProvider;
import ru.easydonate.easypayments.setup.session.InteractiveSetupSession;

public final class StartStepFunction extends SetupStepFunction {

    public StartStepFunction(@NotNull InteractiveSetupProvider setupProvider) {
        super(setupProvider);
    }

    @Override
    public void onStepIn(@NotNull InteractiveSetupSession session) {
        sendMessage(session, "setup.interactive.start");
        setupProvider.nextSetupStep(session);
    }

}
