package ru.easydonate.easypayments.service;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;

public final class KnownPlayersService {

    private final @NotNull Map<String, WeakReference<Player>> joinedSinceLastDrain;
    private final @NotNull Lock joinedSinceLastDrainLock;

    public KnownPlayersService() {
        this.joinedSinceLastDrain = new HashMap<>();
        this.joinedSinceLastDrainLock = new ReentrantLock();
    }

    public void drainJoinedPlayers(@NotNull BiConsumer<String, Boolean> consumer) {
        Map<String, WeakReference<Player>> threadSafeCopy;

        try {
            joinedSinceLastDrainLock.lock();
            if (joinedSinceLastDrain.isEmpty())
                return;

            threadSafeCopy = new HashMap<>(joinedSinceLastDrain);
            joinedSinceLastDrain.clear();
        } finally {
            joinedSinceLastDrainLock.unlock();
        }

        threadSafeCopy.forEach((playerName, playerRef) -> {
            boolean isOnline = false;
            if (playerRef != null) {
                Player player = playerRef.get();
                if (player != null) {
                    isOnline = player.isOnline();
                }
            }

            consumer.accept(playerName, isOnline);
        });
    }

    public void rememberJoinedPlayer(@NotNull Player player) {
        try {
            joinedSinceLastDrainLock.lock();
            this.joinedSinceLastDrain.put(player.getName(), new WeakReference<>(player));
        } finally {
            joinedSinceLastDrainLock.unlock();
        }
    }

}
