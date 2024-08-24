package ru.easydonate.easypayments.service.execution;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.Constants;
import ru.easydonate.easypayments.core.EasyPayments;
import ru.easydonate.easypayments.core.config.Configuration;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.object.CommandReport;
import ru.easydonate.easypayments.core.interceptor.FeedbackInterceptor;
import ru.easydonate.easypayments.core.interceptor.InterceptorFactory;
import ru.easydonate.easypayments.core.util.PluginThreadFactory;
import ru.easydonate.easypayments.core.util.ThreadLocker;
import ru.easydonate.easypayments.exception.CommandExecutionException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Getter
public final class ExecutionService {

    private final EasyPayments plugin;
    private final Configuration config;
    private final InterceptorFactory interceptorFactory;

    private final ExecutorService commandsExecutorService;

    public ExecutionService(
            @NotNull EasyPayments plugin,
            @NotNull Configuration config,
            @NotNull InterceptorFactory interceptorFactory
    ) {
        this.plugin = plugin;
        this.config = config;
        this.interceptorFactory = interceptorFactory;

        this.commandsExecutorService = createAsyncExecutorService();
    }

    public void shutdown() {
        if (commandsExecutorService != null) {
            commandsExecutorService.shutdown();
        }
    }

    public int getFeedbackAwaitTimeMillis() {
        return config.getIntWithBounds(
                "feedback-await-time",
                Constants.MIN_FEEDBACK_AWAIT_TIME,
                Constants.MAX_FEEDBACK_AWAIT_TIME,
                Constants.DEFAULT_FEEDBACK_AWAIT_TIME
        );
    }

    public @NotNull List<CommandReport> processCommandsKeepSequence(@NotNull List<String> commands) {
        if (commands == null || commands.isEmpty())
            return Collections.emptyList();

        AtomicInteger indexer = new AtomicInteger();
        CompletableFuture<?>[] futures = commands.stream()
                .map(command -> processCommandIndexed(command, indexer.getAndIncrement()))
                .toArray(CompletableFuture[]::new);

        long start = System.currentTimeMillis();
        CompletableFuture.allOf(futures).join();
        long time = System.currentTimeMillis() - start;

        // awaiting commands feedback
        int awaitTimeMillis = getFeedbackAwaitTimeMillis();
        if (awaitTimeMillis > time)
            ThreadLocker.lockUninterruptive(awaitTimeMillis - time);

        return Arrays.stream(futures)
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .sequential()
                .map(object -> (ExecutionWrapper) object)
                .filter(wrapper -> wrapper.getObject() != null)
                .sorted()
                .map(ExecutionWrapper::createReport)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public @NotNull CompletableFuture<ExecutionWrapper> processCommandIndexed(@NotNull String command, int index) {
        ExecutionWrapper wrapper = new ExecutionWrapper(index, command);
        return processCommand(command).thenApply(wrapper::setObject);
    }

    public @NotNull CompletableFuture<FeedbackInterceptor> processCommand(@NotNull String command) {
        return interceptorFactory.createFeedbackInterceptorAsync()
                .thenCompose(interceptor -> executeCommand(interceptor, command))
                .exceptionally(this::handleExceptionalExecution);
    }

    private @NotNull CompletableFuture<FeedbackInterceptor> executeCommand(
            @NotNull FeedbackInterceptor interceptor,
            @NotNull String command
    ) throws CommandExecutionException {
        try {
            Bukkit.dispatchCommand(interceptor.getCommandSender(), command);
            return CompletableFuture.supplyAsync(() -> interceptor, commandsExecutorService);
        } catch (Throwable throwable) {
            throw new CommandExecutionException(command, interceptor, throwable);
        }
    }

    private @NotNull FeedbackInterceptor handleExceptionalExecution(@NotNull Throwable cause) {
        if (cause instanceof CompletionException) {
            cause = cause.getCause();

            if (cause instanceof CommandExecutionException) {
                CommandExecutionException exception = (CommandExecutionException) cause;
                cause = exception.getCause();

                String command = exception.getCommand();
                FeedbackInterceptor executor = exception.getExecutor();
                String response = exception.getMessage();
                boolean stackTracePrintRequired = false;

                plugin.getLogger().severe(response);
                executor.getFeedbackMessages().add(response);

                if (cause instanceof CommandException) {
                    CommandException bukkitException = (CommandException) cause;
                    String bukkitMessage = bukkitException.getMessage();

                    cause = bukkitException.getCause();
                    stackTracePrintRequired = true;

                    plugin.getDebugLogger().error(bukkitMessage);
                    executor.getFeedbackMessages().add(bukkitMessage);
                }

                if (stackTracePrintRequired && cause != null) {
                    plugin.getDebugLogger().error(cause);
                }

                return executor;
            }
        } else if (cause instanceof CancellationException || cause instanceof RejectedExecutionException) {
            return null; // ignore these exceptions
        }

        plugin.getLogger().severe("An error occurred while processing a purchase issue command!");
        plugin.getLogger().severe("Tip: check EasyPayments logs for additional information");

        if (cause != null) {
            plugin.getDebugLogger().error("An unexpected exception has been thrown");
            plugin.getDebugLogger().error(cause);
        } else {
            plugin.getDebugLogger().error("An unexpected exception has been thrown (no cause provided)");
        }

        return null;
    }

    private @NotNull ExecutorService createAsyncExecutorService() {
        int threadPoolSize = config.getInt("execution-thread-pool-size", -1);
        ThreadFactory threadFactory = new PluginThreadFactory("Command Execution Worker");

        return threadPoolSize > 0
                ? Executors.newFixedThreadPool(threadPoolSize, threadFactory)
                : Executors.newCachedThreadPool(threadFactory);
    }

}
