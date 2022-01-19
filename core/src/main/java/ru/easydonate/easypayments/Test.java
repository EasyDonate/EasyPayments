package ru.easydonate.easypayments;

import lombok.SneakyThrows;
import ru.easydonate.easypayments.easydonate4j.longpoll.client.LongPollClient;
import ru.easydonate.easypayments.easydonate4j.longpoll.data.model.EventUpdate;
import ru.easydonate.easypayments.easydonate4j.longpoll.data.model.EventUpdates;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Test {

    private static final ExecutorService ASYNC_EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        String accessKey = "faf36db39393dcbeaa700dee00f04ea5";
        int serverId = 12597;

        LongPollClient client = LongPollClient.create(accessKey, serverId, "EasyPayments 2.0.0");
        client.getUpdatesList().whenComplete(Test::onUpdatesFound).join();
    }

    @SneakyThrows
    private static void onUpdatesFound(EventUpdates eventUpdates, Throwable throwable) {
        if(throwable != null) {
            throwable.printStackTrace();
            return;
        }

        int amountOfUpdates = eventUpdates.size();
        long amountOfEvents = eventUpdates.stream()
                .map(EventUpdate::getEventObjects)
                .mapToLong(List::size)
                .sum();

        System.out.printf("Found %d new event(s) in %d update(s)!%n", amountOfEvents, amountOfUpdates);
    }

}
