package ru.easydonate.easypayments.service;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class KnownPlayersService {

    private final @NotNull Set<String> joinedSinceLastDrain;
    private final @NotNull Lock joinedSinceLastDrainLock;

    public KnownPlayersService() {
        this.joinedSinceLastDrain = new HashSet<>();
        this.joinedSinceLastDrainLock = new ReentrantLock();
    }

    public @NotNull Set<String> drainJoinedPlayers() {
        try {
            joinedSinceLastDrainLock.lock();

            if (joinedSinceLastDrain.isEmpty())
                return Collections.emptySet();

            Set<String> drained = new HashSet<>(joinedSinceLastDrain);
            joinedSinceLastDrain.clear();
            return drained;
        } finally {
            joinedSinceLastDrainLock.unlock();
        }
    }

    public void rememberJoinedPlayer(@NotNull String playerName) {
        try {
            joinedSinceLastDrainLock.lock();
            this.joinedSinceLastDrain.add(playerName);
        } finally {
            joinedSinceLastDrainLock.unlock();
        }
    }

}
