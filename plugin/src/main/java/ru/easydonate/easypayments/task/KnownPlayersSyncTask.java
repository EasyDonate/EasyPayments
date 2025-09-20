package ru.easydonate.easypayments.task;

import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.EasyPaymentsPlugin;
import ru.easydonate.easypayments.core.easydonate4j.extension.client.EasyPaymentsClient;
import ru.easydonate.easypayments.service.KnownPlayersService;

import java.util.Map;

public final class KnownPlayersSyncTask extends AbstractPluginTask {

    private static final long TASK_DELAY = 100L;    // 5 seconds
    private static final long TASK_PERIOD = 600L;   // 30 seconds

    private static final int UPLOAD_ATTEMPTS = 5;

    private final @NotNull KnownPlayersService knownPlayersService;
    private final @NotNull EasyPaymentsClient easyPaymentsClient;

    public KnownPlayersSyncTask(
            @NotNull EasyPaymentsPlugin plugin,
            @NotNull KnownPlayersService knownPlayersService,
            @NotNull EasyPaymentsClient easyPaymentsClient
    ) {
        super(plugin, TASK_DELAY);
        this.knownPlayersService = knownPlayersService;
        this.easyPaymentsClient = easyPaymentsClient;
    }

    @Override
    protected long getPeriod() {
        return TASK_PERIOD;
    }

    @Override
    public void run() {
        if (!isWorking())
            return;

        Map<String, Boolean> joinedPlayers = knownPlayersService.drainJoinedPlayers();
        if (joinedPlayers.isEmpty())
            return;

        plugin.getDebugLogger().debug("[PlayersSync] Uploading known players ({0}):", joinedPlayers.size());
        plugin.getDebugLogger().debug(joinedPlayers.toString());

        try {
            for (int attempt = 0; attempt < UPLOAD_ATTEMPTS; attempt++) {
                try {
                    if (!easyPaymentsClient.uploadKnownPlayers(joinedPlayers)) {
                        plugin.getLogger().severe("An unknown error occured while trying to upload reports!");
                        plugin.getLogger().severe("Please, contact with the platform support:");
                        plugin.getLogger().severe(EasyPaymentsPlugin.SUPPORT_URL);
                        return;
                    }

                    if (attempt > 0) {
                        plugin.getLogger().warning(String.format(
                                "Known players have been uploaded on attempt #%d! Ensure that your Internet connection is stable.",
                                attempt + 1
                        ));
                    }
                } catch (Exception ex) {
                    if (attempt == UPLOAD_ATTEMPTS - 1)
                        throw ex;

                    plugin.getDebugLogger().warn("Couldn't upload known players! ({0} of {1})", attempt + 1, UPLOAD_ATTEMPTS);
                    plugin.getDebugLogger().warn(ex);
                }
            }
        } catch (Exception ex) {
            plugin.getDebugLogger().error("Couldn't upload known players!");
            plugin.getDebugLogger().error(ex);

            error("Couldn't upload known players! Please, check your Internet connection!");
            error("This omission may issue some problems for your customers :(");
        }
    }

}
