package ru.easydonate.easypayments.setup;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum InteractiveSetupStep {

    START,
    SET_ACCESS_KEY,
    SET_SERVER_ID,
    FINISH;

    public @NotNull InteractiveSetupStep next() {
        switch (this) {
            case START:
                return InteractiveSetupStep.SET_ACCESS_KEY;
            case SET_ACCESS_KEY:
                return InteractiveSetupStep.SET_SERVER_ID;
            case SET_SERVER_ID:
            case FINISH:
                return InteractiveSetupStep.FINISH;
            default:
                throw new IllegalArgumentException("Unexpected enum constant: " + this);
        }
    }

    public boolean isFinished() {
        return this == FINISH;
    }

}
