package ru.easydonate.easypayments.setup.session;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.setup.InteractiveSetupProvider;
import ru.easydonate.easypayments.setup.InteractiveSetupStep;
import ru.easydonate.easypayments.setup.ShortAnswer;

import java.util.*;

public abstract class AbstractSetupSession implements InteractiveSetupSession {

    protected final InteractiveSetupProvider setupProvider;
    protected final Map<String, Object> persistentDataContainer;
    protected InteractiveSetupStep currentStep;

    private boolean awaitingShortAnswer;

    public AbstractSetupSession(@NotNull InteractiveSetupProvider setupProvider) {
        this.setupProvider = setupProvider;
        this.persistentDataContainer = new HashMap<>();
        this.currentStep = InteractiveSetupStep.START;
    }

    @Override
    public void initialize() {
        setupProvider.currentStepIn(this);
    }

    @Override
    public @NotNull InteractiveSetupStep getCurrentStep() {
        synchronized (this) {
            return currentStep;
        }
    }

    @Override
    public @NotNull InteractiveSetupStep nextStep() {
        synchronized (this) {
            this.currentStep = currentStep.next();
            return currentStep;
        }
    }

    @Override
    public boolean isAwaitingShortAnswer() {
        return awaitingShortAnswer;
    }

    @Override
    public void acceptShortAnswer(@NotNull ShortAnswer answer) {
        this.awaitingShortAnswer = false;
        setupProvider.getCurrentStepFunction(this).acceptShortAnswer(this, answer);
    }

    @Override
    public void awaitShortAnswer() {
        this.awaitingShortAnswer = true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <T> Optional<T> getPersistentData(@NotNull String key) {
        return Optional.ofNullable((T) persistentDataContainer.get(key));
    }

    @Override
    public @NotNull <T> OptionalInt getPersistentIntData(@NotNull String key) {
        Integer value = (Integer) persistentDataContainer.get(key);
        return value != null ? OptionalInt.of(value) : OptionalInt.empty();
    }

    @Override
    public @NotNull <T> OptionalLong getPersistentLongData(@NotNull String key) {
        Long value = (Long) persistentDataContainer.get(key);
        return value != null ? OptionalLong.of(value) : OptionalLong.empty();
    }

    @Override
    public @NotNull <T> OptionalDouble getPersistentDoubleData(@NotNull String key) {
        Double value = (Double) persistentDataContainer.get(key);
        return value != null ? OptionalDouble.of(value) : OptionalDouble.empty();
    }

    @Override
    public synchronized void savePersistentData(@NotNull String key, @Nullable Object data) {
        persistentDataContainer.put(key, data);
    }

    @Override
    public @NotNull Optional<String> getAccessKey() {
        return getPersistentData(ACCESS_KEY_PERSISTENT_KEY);
    }

    @Override
    public void setAccessKey(@NotNull String accessKey) {
        savePersistentData(ACCESS_KEY_PERSISTENT_KEY, accessKey);
    }

    @Override
    public @NotNull OptionalInt getServerId() {
        return getPersistentIntData(SERVER_ID_PERSISTENT_KEY);
    }

    @Override
    public void setServerId(int serverId) {
        savePersistentData(SERVER_ID_PERSISTENT_KEY, serverId);
    }

}
