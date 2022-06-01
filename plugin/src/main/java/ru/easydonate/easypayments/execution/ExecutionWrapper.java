package ru.easydonate.easypayments.execution;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.object.CommandReport;
import ru.easydonate.easypayments.execution.interceptor.FeedbackInterceptor;

import java.util.Objects;

public final class ExecutionWrapper extends IndexedWrapper<FeedbackInterceptor> {

    private final String command;

    public ExecutionWrapper(int index, @NotNull String command) {
        super(index);
        this.command = command;
    }

    public ExecutionWrapper(int index, @Nullable FeedbackInterceptor interceptor, @NotNull String command) {
        super(index, interceptor);
        this.command = command;
    }

    public @Nullable CommandReport createReport() {
        FeedbackInterceptor interceptor = getObject();
        if (interceptor == null)
            return null;

        return CommandReport.create(command, interceptor.getFeedbackMessages());
    }

    @Override
    public @NotNull ExecutionWrapper setObject(@Nullable FeedbackInterceptor object) {
        super.setObject(object);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ExecutionWrapper that = (ExecutionWrapper) o;
        return Objects.equals(command, that.command);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), command);
    }

    @Override
    public @NotNull String toString() {
        return "ExecutionWrapper{" +
                "command='" + command + '\'' +
                ", index=" + index +
                ", interceptor=" + object +
                '}';
    }

}
