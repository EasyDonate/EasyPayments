package ru.easydonate.easypayments.execution;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easydonate4j.exception.HttpRequestException;
import ru.easydonate.easydonate4j.exception.HttpResponseException;
import ru.easydonate.easydonate4j.exception.JsonSerializationException;
import ru.easydonate.easypayments.EasyPaymentsPlugin;
import ru.easydonate.easypayments.config.Configuration;
import ru.easydonate.easypayments.database.DatabaseManager;
import ru.easydonate.easypayments.database.model.Customer;
import ru.easydonate.easypayments.database.model.Payment;
import ru.easydonate.easypayments.easydonate4j.EventType;
import ru.easydonate.easypayments.easydonate4j.extension.client.EasyPaymentsClient;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.EventReportObject;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.EventUpdateReport;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.EventUpdateReports;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.object.*;
import ru.easydonate.easypayments.easydonate4j.longpoll.data.model.EventObject;
import ru.easydonate.easypayments.easydonate4j.longpoll.data.model.EventUpdate;
import ru.easydonate.easypayments.easydonate4j.longpoll.data.model.EventUpdates;
import ru.easydonate.easypayments.easydonate4j.longpoll.data.model.object.NewPaymentEvent;
import ru.easydonate.easypayments.easydonate4j.longpoll.data.model.object.NewRewardEvent;
import ru.easydonate.easypayments.easydonate4j.longpoll.data.model.object.NewWithdrawEvent;
import ru.easydonate.easypayments.easydonate4j.longpoll.data.model.object.RepeatPaymentEvent;
import ru.easydonate.easypayments.exception.CommandExecutionException;
import ru.easydonate.easypayments.execution.interceptor.FeedbackInterceptor;
import ru.easydonate.easypayments.execution.interceptor.InterceptorFactory;
import ru.easydonate.easypayments.execution.processor.object.*;
import ru.easydonate.easypayments.execution.processor.update.EventUpdateProcessor;
import ru.easydonate.easypayments.execution.processor.update.SimplePaymentEventProcessor;
import ru.easydonate.easypayments.shopcart.ShopCartStorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Getter
public final class ExecutionController {

    private final Plugin plugin;
    private final Configuration config;
    private final DatabaseManager databaseManager;
    private final ShopCartStorage shopCartStorage;
    private final InterceptorFactory interceptorFactory;

    private final ExecutorService asyncExecutorService;
    private final Map<EventType, EventObjectProcessor<? extends EventObject, ? extends EventReportObject>> eventObjectProcessors;
    private final Map<EventType, EventUpdateProcessor<? extends EventObject, ? extends EventReportObject>> eventUpdateProcessors;

    public ExecutionController(
            @NotNull Plugin plugin,
            @NotNull Configuration config,
            @NotNull DatabaseManager databaseManager,
            @NotNull ShopCartStorage shopCartStorage,
            @NotNull InterceptorFactory interceptorFactory
    ) {
        this.plugin = plugin;
        this.config = config;
        this.databaseManager = databaseManager;
        this.shopCartStorage = shopCartStorage;
        this.interceptorFactory = interceptorFactory;

        this.asyncExecutorService = Executors.newCachedThreadPool();
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

    public int getServerId() {
        return config.getInt("server-id", 0);
    }

    public int getFeedbackAwaitTime() {
        return Math.max(config.getInt("feedback-await-time", 0), 1000);
    }

    public boolean shouldAddToCart(@NotNull OfflinePlayer customer) {
        return !customer.isOnline() && config.getBoolean("use-shop-cart", true);
    }

    public void refreshCustomer(@NotNull Customer customer) {
        databaseManager.refreshCustomer(customer).join();
    }

    @SneakyThrows(JsonSerializationException.class)
    public void uploadReports(@NotNull EasyPaymentsClient client, @Nullable EventUpdateReports reports) throws HttpRequestException, HttpResponseException {
        if(reports == null || reports.isEmpty())
            return;

        if(!client.uploadReports(reports)) {
            plugin.getLogger().severe("An unknown error occured while trying to upload reports!");
            plugin.getLogger().severe("Please, contact with the platform support:");
            plugin.getLogger().severe("https://vk.me/easydonateru");
            return;
        }

        // TODO remove that later
        System.out.println("Uploaded reports: " + reports.toPrettyString());

        Map<Integer, Payment> payments = databaseManager.getAllUnreportedPayments(getServerId())
                .join()
                .stream()
                .collect(Collectors.toMap(Payment::getId, p -> p));

        reports.stream()
                .map(EventUpdateReport::getReportObjects)
                .flatMap(List::stream)
                .filter(object -> object instanceof NewPaymentReport)
                .map(object -> (NewPaymentReport) object)
                .map(NewPaymentReport::getPaymentId)
                .map(payments::get)
                .filter(Objects::nonNull)
                .filter(Payment::markAsReported)
                .map(databaseManager::savePayment)
                .parallel()
                .forEach(CompletableFuture::join);
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
        if(processor == null)
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
        if(processor == null)
            throw new IllegalArgumentException(String.format(
                    "There are no event object processor present for event type '%s'!",
                    eventType
            ));

        return CompletableFuture.supplyAsync(() -> processor.processObject(eventObject), asyncExecutorService);
    }

    public @NotNull CompletableFuture<CommandReport> processObjectCommand(@NotNull String command) {
        return interceptorFactory.createFeedbackInterceptorAsync()
                .thenApply(interceptor -> executeCommandAndWait(interceptor, command))
                .thenApply(FeedbackInterceptor::getFeedbackMessages)
                .thenApply(feedback -> CommandReport.create(command, feedback))
                .exceptionally(this::handleExceptionalReport);
    }

    public @NotNull CompletableFuture<IndexedWrapper<CommandReport>> processObjectCommandIndexed(@NotNull String command, int index) {
        IndexedWrapper<CommandReport> wrapper = new IndexedWrapper<>(index);
        return processObjectCommand(command).thenApply(wrapper::setObject);
    }

    private @NotNull CommandReport handleExceptionalReport(@NotNull Throwable cause) {
        if(cause instanceof CommandExecutionException) {
            CommandExecutionException exception = (CommandExecutionException) cause;
            String command = exception.getCommand();
            String response = exception.toString();

            plugin.getLogger().severe(response);
            if(EasyPaymentsPlugin.isDebugEnabled())
                exception.getCause().printStackTrace();

            return CommandReport.create(command, response);
        }

        throw new IllegalStateException("An unexpected exception has been thrown!", cause);
    }

    private @NotNull FeedbackInterceptor executeCommandAndWait(
            @NotNull FeedbackInterceptor interceptor,
            @NotNull String command
    ) throws CommandExecutionException {
        try {
            Bukkit.dispatchCommand((CommandSender) interceptor, command);
        } catch (Throwable throwable) {
            throw new CommandExecutionException(command, throwable);
        }

        try {
            Thread.sleep(getFeedbackAwaitTime());
        } catch (InterruptedException ignored) {
        }

        return interceptor;
    }

}
