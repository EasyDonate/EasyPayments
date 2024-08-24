package ru.easydonate.easypayments.service;

import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.EasyPaymentsPlugin;
import ru.easydonate.easypayments.core.easydonate4j.EventType;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.EventReportObject;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.EventUpdateReport;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.EventUpdateReports;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.object.NewPaymentReport;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.object.NewRewardReport;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.object.NewWithdrawReport;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.object.RepeatPaymentReport;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.EventObject;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.EventUpdate;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.EventUpdates;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.object.NewPaymentEvent;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.object.NewRewardEvent;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.object.NewWithdrawEvent;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.object.RepeatPaymentEvent;
import ru.easydonate.easypayments.core.util.PluginThreadFactory;
import ru.easydonate.easypayments.service.execution.ExecutionService;
import ru.easydonate.easypayments.service.processor.object.*;
import ru.easydonate.easypayments.service.processor.update.EventUpdateProcessor;
import ru.easydonate.easypayments.service.processor.update.SimplePaymentEventProcessor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class LongPollEventDispatcher {

    private final EasyPaymentsPlugin plugin;
    private final ExecutionService executionService;

    private final ExecutorService asyncExecutorPool;
    private final Map<EventType, EventObjectProcessor<?, ?>> eventObjectProcessors;
    private final Map<EventType, EventUpdateProcessor<?, ?>> eventUpdateProcessors;

    public LongPollEventDispatcher(
            @NotNull EasyPaymentsPlugin plugin,
            @NotNull ExecutionService executionService
    ) {
        this.plugin = plugin;
        this.executionService = executionService;

        this.asyncExecutorPool = Executors.newCachedThreadPool(new PluginThreadFactory("Async Processing Worker"));
        this.eventObjectProcessors = new HashMap<>();
        this.eventUpdateProcessors = new HashMap<>();
        registerDefaults();
    }

    public void shutdown() {
        if (asyncExecutorPool != null) {
            asyncExecutorPool.shutdown();
        }
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
        }, asyncExecutorPool);
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

        return CompletableFuture.supplyAsync(() -> processor.processUpdate(eventUpdate), asyncExecutorPool);
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

        return CompletableFuture.supplyAsync(() -> processor.processObject(eventObject), asyncExecutorPool)
                .thenApply(report -> processor.processPluginEvents(eventObject, report));
    }

    private void registerDefaults() {
        // --- event object processors
        this.eventObjectProcessors.put(EventType.NEW_PAYMENT, new NewPaymentObjectProcessor(plugin));
        this.eventObjectProcessors.put(EventType.REPEAT_PAYMENT, new RepeatPaymentObjectProcessor(executionService));
        this.eventObjectProcessors.put(EventType.NEW_WITHDRAW, new NewWithdrawObjectProcessor(executionService));
        this.eventObjectProcessors.put(EventType.NEW_REWARD, new NewRewardObjectProcessor(executionService));

        // --- event update processors
        this.eventUpdateProcessors.put(EventType.NEW_PAYMENT, new SimplePaymentEventProcessor<NewPaymentEvent, NewPaymentReport>(this));
        this.eventUpdateProcessors.put(EventType.REPEAT_PAYMENT, new SimplePaymentEventProcessor<RepeatPaymentEvent, RepeatPaymentReport>(this));
        this.eventUpdateProcessors.put(EventType.NEW_WITHDRAW, new SimplePaymentEventProcessor<NewWithdrawEvent, NewWithdrawReport>(this));
        this.eventUpdateProcessors.put(EventType.NEW_REWARD, new SimplePaymentEventProcessor<NewRewardEvent, NewRewardReport>(this));
    }

}
