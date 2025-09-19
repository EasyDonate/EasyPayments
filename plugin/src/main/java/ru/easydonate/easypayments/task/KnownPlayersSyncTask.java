package ru.easydonate.easypayments.task;

import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.EasyPaymentsPlugin;
import ru.easydonate.easypayments.service.KnownPlayersService;

import java.util.Set;

public final class KnownPlayersSyncTask extends AbstractPluginTask {

    private static final long TASK_DELAY = 100L;    // 5 seconds
    private static final long TASK_PERIOD = 600L;   // 30 seconds

    private final @NotNull KnownPlayersService knownPlayersService;

    public KnownPlayersSyncTask(@NotNull EasyPaymentsPlugin plugin, @NotNull KnownPlayersService knownPlayersService) {
        super(plugin, TASK_DELAY);
        this.knownPlayersService = knownPlayersService;
    }

    @Override
    protected long getPeriod() {
        return TASK_PERIOD;
    }

    @Override
    public void run() {
        if (!isWorking())
            return;

        Set<String> playerNames = knownPlayersService.drainJoinedPlayers();
        if (playerNames.isEmpty())
            return;

        // TODO upload player names
    }

}
