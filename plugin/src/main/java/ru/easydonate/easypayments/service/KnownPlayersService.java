package ru.easydonate.easypayments.service;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class KnownPlayersService {

    private final @NotNull Map<String, WeakReference<Player>> joinedSinceLastDrain;
    private final @NotNull Lock joinedSinceLastDrainLock;

    public KnownPlayersService() {
        this.joinedSinceLastDrain = new HashMap<>();
        this.joinedSinceLastDrainLock = new ReentrantLock();
    }

    public @NotNull Map<String, Boolean> drainJoinedPlayers() {
        Map<String, WeakReference<Player>> drained;

        try {
            joinedSinceLastDrainLock.lock();

            if (joinedSinceLastDrain.isEmpty())
                return Collections.emptyMap();

            drained = new HashMap<>(joinedSinceLastDrain);
            joinedSinceLastDrain.clear();
        } finally {
            joinedSinceLastDrainLock.unlock();
        }

        Map<String, Boolean> joinedPlayers = new HashMap<>();
        drained.forEach((playerName, playerRef) -> {
            boolean isOnline = false;
            if (playerRef != null) {
                Player player = playerRef.get();
                if (player != null) {
                    isOnline = player.isOnline();
                }
            }

            joinedPlayers.put(playerName, isOnline);
        });

        return joinedPlayers;
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
