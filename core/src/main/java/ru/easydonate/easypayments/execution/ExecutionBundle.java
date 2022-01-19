package ru.easydonate.easypayments.execution;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.CommandResponsingReport;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@Getter
@RequiredArgsConstructor
public class ExecutionBundle implements Comparable<ExecutionBundle> {

    private final int id;
    private final String command;
    private final Plugin plugin;
    private final InterceptorFactory interceptorFactory;
    private String response;

    public CompletableFuture<ExecutionBundle> executeAsync() {
        return CompletableFuture.supplyAsync(this::executeAndWait);
    }

    public ExecutionBundle executeAndWait() {
        AtomicReference<FeedbackInterceptor> interceptor = new AtomicReference<>();

        plugin.getServer()
                .getScheduler()
                .scheduleSyncDelayedTask(plugin, () -> {
                    interceptor.set(interceptorFactory.createFeedbackInterceptor());
                    Bukkit.dispatchCommand((CommandSender) interceptor.get(), command);
                });

        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {}

        List<String> messages = interceptor.get() != null ? interceptor.get().getFeedbackMessages() : null;
        this.response = messages != null ? String.join("\n", messages) : "";
        return this;
    }

    public void saveTo(CommandResponsingReport report) {
        report.addCommandResponse(response);
    }

    @Override
    public int compareTo(ExecutionBundle other) {
        return Integer.compare(id, other.id);
    }

}
