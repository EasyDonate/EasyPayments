package ru.easydonate.easypayments.execution;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easydonate4j.exception.HttpRequestException;
import ru.easydonate.easydonate4j.exception.HttpResponseException;
import ru.easydonate.easydonate4j.exception.JsonSerializationException;
import ru.easydonate.easypayments.EasyPaymentsPlugin;
import ru.easydonate.easypayments.core.Constants;
import ru.easydonate.easypayments.core.config.Configuration;
import ru.easydonate.easypayments.core.config.localized.Messages;
import ru.easydonate.easypayments.core.easydonate4j.EventType;
import ru.easydonate.easypayments.core.easydonate4j.extension.client.EasyPaymentsClient;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.EventReportObject;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.EventUpdateReport;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.EventUpdateReports;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.object.*;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.EventObject;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.EventUpdate;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.EventUpdates;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.object.NewPaymentEvent;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.object.NewRewardEvent;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.object.NewWithdrawEvent;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.object.RepeatPaymentEvent;
import ru.easydonate.easypayments.core.interceptor.FeedbackInterceptor;
import ru.easydonate.easypayments.core.interceptor.InterceptorFactory;
import ru.easydonate.easypayments.core.util.ThreadLocker;
import ru.easydonate.easypayments.database.model.Customer;
import ru.easydonate.easypayments.database.model.Payment;
import ru.easydonate.easypayments.database.model.Purchase;
import ru.easydonate.easypayments.exception.CommandExecutionException;
import ru.easydonate.easypayments.execution.processor.object.*;
import ru.easydonate.easypayments.execution.processor.update.EventUpdateProcessor;
import ru.easydonate.easypayments.execution.processor.update.SimplePaymentEventProcessor;
import ru.easydonate.easypayments.shopcart.ShopCartStorage;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Getter
public final class ExecutionController {

    private final EasyPaymentsPlugin plugin;
    private final Configuration config;
    private final Messages messages;
    private final EasyPaymentsClient easyPaymentsClient;
    private final ShopCartStorage shopCartStorage;
    private final InterceptorFactory interceptorFactory;

    private final ExecutorService asyncExecutorService;
    private final ExecutorService commandsExecutorService;

    private final Map<EventType, EventObjectProcessor<? extends EventObject, ? extends EventReportObject>> eventObjectProcessors;
    private final Map<EventType, EventUpdateProcessor<? extends EventObject, ? extends EventReportObject>> eventUpdateProcessors;

    public ExecutionController(
            @NotNull EasyPaymentsPlugin plugin,
            @NotNull Configuration config,
            @NotNull Messages messages,
            @NotNull EasyPaymentsClient easyPaymentsClient,
            @NotNull ShopCartStorage shopCartStorage,
            @NotNull InterceptorFactory interceptorFactory
    ) {
        this.plugin = plugin;
        this.config = config;
        this.messages = messages;
        this.easyPaymentsClient = easyPaymentsClient;
        this.shopCartStorage = shopCartStorage;
        this.interceptorFactory = interceptorFactory;

        this.asyncExecutorService = Executors.newCachedThreadPool(new ExecutionControllerThreadFactory("Async Execution Worker"));
        this.commandsExecutorService = createAsyncExecutorService();

        this.eventObjectProcessors = new HashMap<>();
        this.eventUpdateProcessors = new HashMap<>();

        // --- event object processors
        this.eventObjectProcessors.put(EventType.NEW_PAYMENT, new NewPaymentObjectProcessor(this));
        this.eventObjectProcessors.put(EventType.REPEAT_PAYMENT, new RepeatPaymentObjectProcessor(this));
        this.eventObjectProcessors.put(EventType.NEW_WITHDRAW, new NewWithdrawObjectProcessor(this));
        this.eventObjectProcessors.put(EventType.NEW_REWARD, new NewRewardObjectProcessor(this));

        // --- event update processors
        this.eventUpdateProcessors.put(EventType.NEW_PAYMENT, new SimplePaymentEventProcessor<NewPaymentEvent, NewPaymentReport>(this));
        this.eventUpdateProcessors.put(EventType.REPEAT_PAYMENT, new SimplePaymentEventProcessor<RepeatPaymentEvent, RepeatPaymentReport>(this));
        this.eventUpdateProcessors.put(EventType.NEW_WITHDRAW, new SimplePaymentEventProcessor<NewWithdrawEvent, NewWithdrawReport>(this));
        this.eventUpdateProcessors.put(EventType.NEW_REWARD, new SimplePaymentEventProcessor<NewRewardEvent, NewRewardReport>(this));
    }

    private @NotNull ExecutorService createAsyncExecutorService() {
        int threadPoolSize = config.getInt("execution-thread-pool-size", -1);
        ThreadFactory threadFactory = new ExecutionControllerThreadFactory("Command Execution Worker");

        return threadPoolSize > 0
                ? Executors.newFixedThreadPool(threadPoolSize, threadFactory)
                : Executors.newCachedThreadPool(threadFactory);
    }

    public void shutdown() {
        if (commandsExecutorService != null)
            commandsExecutorService.shutdown();

        if (asyncExecutorService != null)
            asyncExecutorService.shutdown();
    }

    public int getServerId() {
        return config.getInt("server-id", 0);
    }

    public int getFeedbackAwaitTimeMillis() {
        return config.getIntWithBounds("feedback-await-time", Constants.MIN_FEEDBACK_AWAIT_TIME, Constants.MAX_FEEDBACK_AWAIT_TIME, Constants.DEFAULT_FEEDBACK_AWAIT_TIME);
    }

    public boolean isShopCartEnabled() {
        return config.getBoolean("use-shop-cart", Constants.DEFAULT_SHOP_CART_STATUS);
    }

    public void refreshCustomer(@NotNull Customer customer) {
        plugin.getStorage().refreshCustomer(customer).join();
    }

    public void refreshPayment(@NotNull Payment payment) {
        plugin.getStorage().refreshPayment(payment).join();
    }

    @SneakyThrows(JsonSerializationException.class)
    public void uploadReports(@Nullable EventUpdateReports reports) throws HttpRequestException, HttpResponseException {
        if (reports == null || reports.isEmpty())
            return;

        plugin.getDebugLogger().debug("[Execution] Uploading reports:");
        plugin.getDebugLogger().debug(reports.toPrettyString().split("\n"));

        if (!easyPaymentsClient.uploadReports(reports)) {
            plugin.getLogger().severe("An unknown error occured while trying to upload reports!");
            plugin.getLogger().severe("Please, contact with the platform support:");
            plugin.getLogger().severe("https://vk.me/easydonateru");
            return;
        }

        plugin.getDebugLogger().debug("[Execution] Reports have been uploaded");

        if (!reports.containsReportWithType(EventType.NEW_PAYMENT)) {
            plugin.getDebugLogger().debug("[Execution] There are no 'new_payment' events, skipping database entries update...");
            return;
        }

        Map<Integer, Payment> payments = plugin.getStorage().getAllUnreportedPayments(getServerId())
                .join()
                .stream()
                .collect(Collectors.toMap(Payment::getId, p -> p));

        plugin.getDebugLogger().debug("[Execution] Unreported payments in the database: {0}", payments.keySet());

        CompletableFuture<?>[] futures = reports.stream()
                .map(EventUpdateReport::getReportObjects)
                .flatMap(List::stream)
                .filter(object -> object instanceof NewPaymentReport)
                .map(object -> (NewPaymentReport) object)
                .map(NewPaymentReport::getPaymentId)
                .map(payments::get)
                .filter(Objects::nonNull)
                .filter(Payment::markAsReported)
                .peek(this::markAsCollectedIfCartDisabled)
                .map(plugin.getStorage()::savePayment)
                .toArray(CompletableFuture<?>[]::new);

        CompletableFuture.allOf(futures).join();

        plugin.getDebugLogger().debug("[Execution] Unreported payments have been updated");
    }

    public void givePurchasesFromCartAndReport(@NotNull Collection<Payment> payments) throws HttpRequestException, HttpResponseException {
        plugin.getDebugLogger().debug("[Execution] Marking payments as collected...");

        payments.stream()
                .filter(Payment::markAsCollected)
                .map(plugin.getStorage()::savePayment)
                .parallel()
                .forEach(CompletableFuture::join);

        plugin.getDebugLogger().debug("[Execution] Constructing the event update report...");

        List<NewPaymentReport> eventReports = payments.parallelStream()
                .map(this::handlePaymentFromCart)
                .collect(Collectors.toList());

        uploadCartReports(eventReports);
    }

    @SneakyThrows(JsonSerializationException.class)
    public void uploadCartReports(List<NewPaymentReport> eventReports) throws HttpRequestException, HttpResponseException {
        if (eventReports.isEmpty())
            return;

        EventUpdateReport<NewPaymentReport> updateReport = new EventUpdateReport<>(EventType.NEW_PAYMENT, eventReports);
        EventUpdateReports updateReports = new EventUpdateReports(updateReport);

        plugin.getDebugLogger().debug("[Execution] Uploading cart reports:");
        plugin.getDebugLogger().debug(updateReports.toPrettyString().split("\n"));

        if (!easyPaymentsClient.uploadReports(updateReports)) {
            plugin.getLogger().severe("An unknown error occured while trying to upload reports!");
            plugin.getLogger().severe("Please, contact with the platform support:");
            plugin.getLogger().severe("https://vk.me/easydonateru");
            return;
        }

        plugin.getDebugLogger().debug("[Execution] Cart reports have been uploaded");
    }

    private @NotNull NewPaymentReport handlePaymentFromCart(@NotNull Payment payment) {
        String customer = payment.getCustomer().getPlayerName();
        NewPaymentReport report = new NewPaymentReport(payment.getId(), false, customer);

        if (payment.hasPurchases()) {
            payment.getPurchases().stream() // may be should do this in parallel stream?
                    .filter(Purchase::hasCommands)
                    .map(purchase -> handlePurchaseFromCart(customer, purchase))
                    .flatMap(List::stream)
                    .forEach(report::addCommandReport);
        }

        return report;
    }

    private @NotNull List<CommandReport> handlePurchaseFromCart(String customer, @NotNull Purchase purchase) {
        List<String> commands = purchase.getCommands();
        if (commands != null && !commands.isEmpty())
            commands = commands.stream()
                    .map(command -> command != null ? command.replace("{user}", customer) : command)
                    .collect(Collectors.toList());

        List<CommandReport> commandReports = processCommandsKeepSequence(commands);
        purchase.collect(commandReports);

        plugin.getStorage().savePurchase(purchase).join();
        return commandReports;
    }

    public @NotNull CompletableFuture<EventUpdateReports> processEventUpdates(@NotNull EventUpdates eventUpdates) {
        return CompletableFuture.supplyAsync(() -> {
            EventUpdateReports reports = eventUpdates.createReports();

            eventUpdates.parallelStream()
                    .map(this::processEventUpdate)
                    .map(CompletableFuture::join)
                    .sequential()
                    .forEach(reports::add);

            return reports;
        }, asyncExecutorService);
    }

    @SuppressWarnings("unchecked")
    public <E extends EventObject, R extends EventReportObject> @NotNull CompletableFuture<EventUpdateReport<R>> processEventUpdate(
            @NotNull EventUpdate<E> eventUpdate
    ) {
        EventType eventType = eventUpdate.getEventType();
        EventUpdateProcessor<E, R> processor = (EventUpdateProcessor<E, R>) eventUpdateProcessors.get(eventType);
        if (processor == null)
            throw new IllegalArgumentException(String.format(
                    "There are no event update processor present for event type '%s'!",
                    eventType
            ));

        return CompletableFuture.supplyAsync(() -> processor.processUpdate(eventUpdate), asyncExecutorService);
    }

    @SuppressWarnings("unchecked")
    public <E extends EventObject, R extends EventReportObject> @NotNull CompletableFuture<R> processEventObject(
            @NotNull EventType eventType,
            @NotNull E eventObject
    ) {
        EventObjectProcessor<E, R> processor = (EventObjectProcessor<E, R>) eventObjectProcessors.get(eventType);
        if (processor == null)
            throw new IllegalArgumentException(String.format(
                    "There are no event object processor present for event type '%s'!",
                    eventType
            ));

        return CompletableFuture.supplyAsync(() -> processor.processObject(eventObject), asyncExecutorService)
                .thenApply(report -> processor.processPluginEvents(eventObject, report));
    }

    public @NotNull CompletableFuture<FeedbackInterceptor> processObjectCommand(@NotNull String command) {
        return interceptorFactory.createFeedbackInterceptorAsync()
                .thenCompose(interceptor -> executeCommand(interceptor, command))
                .exceptionally(this::handleExceptionalExecution);
    }

    public @NotNull CompletableFuture<ExecutionWrapper> processObjectCommandIndexed(@NotNull String command, int index) {
        ExecutionWrapper wrapper = new ExecutionWrapper(index, command);
        return processObjectCommand(command).thenApply(wrapper::setObject);
    }

    public @NotNull List<CommandReport> processCommandsKeepSequence(@NotNull List<String> commands) {
        if (commands == null)
            return null;

        if (commands.isEmpty())
            return Collections.emptyList();

        AtomicInteger indexer = new AtomicInteger();
        CompletableFuture<?>[] futures = commands.stream()
                .map(command -> processObjectCommandIndexed(command, indexer.getAndIncrement()))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();

        // awaiting commands feedback
        ThreadLocker.lockUninterruptive(getFeedbackAwaitTimeMillis());

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

    private void markAsCollectedIfCartDisabled(@NotNull Payment payment) {
        if (!isShopCartEnabled())
            payment.markAsCollected();
    }

    private static final class ExecutionControllerThreadFactory implements ThreadFactory {

        private final String baseThreadName;
        private final AtomicInteger indexer;

        public ExecutionControllerThreadFactory(@NotNull String baseThreadName) {
            this.baseThreadName = baseThreadName;
            this.indexer = new AtomicInteger();
        }

        @Override
        public @NotNull Thread newThread(@NotNull Runnable task) {
            return new Thread(task, String.format("EasyPayments %s #%d", baseThreadName, indexer.incrementAndGet()));
        }

    }

}
