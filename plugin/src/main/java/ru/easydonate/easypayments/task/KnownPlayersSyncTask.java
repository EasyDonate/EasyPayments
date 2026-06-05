package ru.easydonate.easypayments.task;

import org.jetbrains.annotations.NotNull;
import ru.easydonate.easydonate4j.exception.HttpRequestException;
import ru.easydonate.easydonate4j.exception.HttpResponseException;
import ru.easydonate.easypayments.EasyPaymentsPlugin;
import ru.easydonate.easypayments.core.easydonate4j.extension.client.EasyPaymentsClient;
import ru.easydonate.easypayments.core.util.ThrowableCauseFinder;
import ru.easydonate.easypayments.service.KnownPlayersService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class KnownPlayersSyncTask extends AbstractPluginTask {

    private static final Pattern RESPONSE_CODE_EXCEPTION = Pattern.compile("Server returned HTTP response code: (?<code>\\d+) for URL: (?<url>.+)");

    private static final long TASK_DELAY = 100L;    // 5 seconds
    private static final long TASK_PERIOD = 600L;   // 30 seconds

    private static final int UPLOAD_ATTEMPTS = 5;
    private static final int HTTP_CONFLICT = 409;
    private static final long CONFLICT_NOTICE_COOLDOWN_MILLIS = 300000L;

    private final @NotNull KnownPlayersService knownPlayersService;
    private final @NotNull EasyPaymentsClient easyPaymentsClient;
    private final @NotNull AtomicBoolean uploading;
    private final @NotNull Map<String, Boolean> pendingPlayers;
    private final @NotNull Object pendingPlayersLock;
    private volatile long lastConflictNoticeMillis;

    public KnownPlayersSyncTask(
            @NotNull EasyPaymentsPlugin plugin,
            @NotNull KnownPlayersService knownPlayersService,
            @NotNull EasyPaymentsClient easyPaymentsClient
    ) {
        super(plugin, TASK_DELAY);
        this.knownPlayersService = knownPlayersService;
        this.easyPaymentsClient = easyPaymentsClient;
        this.uploading = new AtomicBoolean(false);
        this.pendingPlayers = new HashMap<>();
        this.pendingPlayersLock = new Object();
        this.lastConflictNoticeMillis = 0L;
    }

    @Override
    protected long getPeriod() {
        return TASK_PERIOD;
    }

    @Override
    public void run() {
        if (!isWorking())
            return;

        if (!uploading.compareAndSet(false, true)) {
            plugin.getDebugLogger().warn("[PlayersSync] Previous known players upload is still in progress, skipping this tick...");
            return;
        }

        try {
            Map<String, Boolean> players = collectPlayers();
            if (players.isEmpty())
                return;

            plugin.getDebugLogger().debug("[PlayersSync] Uploading known players ({0}):", players.size());
            plugin.getDebugLogger().debug(players.toString());

            if (!uploadPlayers(players))
                keepPlayersForNextSync(players);
        } finally {
            uploading.set(false);
        }
    }

    private @NotNull Map<String, Boolean> collectPlayers() {
        Map<String, Boolean> players = new HashMap<>();

        synchronized (pendingPlayersLock) {
            players.putAll(pendingPlayers);
            pendingPlayers.clear();
        }

        // joined players since last drain (dynamic) + current online players (always 'true')
        knownPlayersService.drainJoinedPlayers(players::put);
        plugin.getServer().getOnlinePlayers().forEach(player -> players.put(player.getName(), true));
        return players;
    }

    private boolean uploadPlayers(@NotNull Map<String, Boolean> players) {
        try {
            for (int attempt = 0; attempt < UPLOAD_ATTEMPTS; attempt++) {
                try {
                    if (easyPaymentsClient.uploadKnownPlayers(players)) {
                        if (attempt > 0) {
                            plugin.getLogger().warning(String.format(
                                    "Known players have been uploaded on attempt #%d! Ensure that your Internet connection is stable.",
                                    attempt + 1
                            ));
                        }

                        return true;
                    }

                    plugin.getLogger().severe("An unknown error occured while trying to upload known players!");
                    plugin.getLogger().severe("Please, contact with the platform support:");
                    plugin.getLogger().severe(EasyPaymentsPlugin.SUPPORT_URL);
                    return false;
                } catch (HttpRequestException | HttpResponseException ex) {
                    int httpCode = resolveHttpCode(ex);
                    if (httpCode == HTTP_CONFLICT) {
                        handleConflictResponse();
                        return false;
                    }

                    if (attempt == UPLOAD_ATTEMPTS - 1)
                        throw ex;

                    plugin.getDebugLogger().warn("Couldn't upload known players! ({0} of {1})", attempt + 1, UPLOAD_ATTEMPTS);
                    if (httpCode > 0)
                        plugin.getDebugLogger().warn("[PlayersSync] EasyDonate API returned HTTP {0}", httpCode);
                    plugin.getDebugLogger().warn(ex);

                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        } catch (Exception ex) {
            plugin.getDebugLogger().error("Couldn't upload known players!");
            plugin.getDebugLogger().error(ex);

            error("Couldn't upload known players! Please, check your Internet connection!");
            error("This omission may issue some problems for your customers :(");
        }

        return false;
    }

    private void keepPlayersForNextSync(@NotNull Map<String, Boolean> players) {
        synchronized (pendingPlayersLock) {
            pendingPlayers.putAll(players);
        }

        plugin.getDebugLogger().warn("[PlayersSync] Known players batch ({0}) has been kept for the next sync attempt.", players.size());
    }

    private void handleConflictResponse() {
        plugin.getDebugLogger().error("[PlayersSync] EasyDonate API returned HTTP 409 for known players upload");
        plugin.getDebugLogger().error("[PlayersSync] Current server ID: {0}", plugin.getServerId());
        plugin.getDebugLogger().error("[PlayersSync] This usually means that the same server-id is used by another running EasyPayments instance.");

        long currentMillis = System.currentTimeMillis();
        if (currentMillis - lastConflictNoticeMillis < CONFLICT_NOTICE_COOLDOWN_MILLIS)
            return;

        this.lastConflictNoticeMillis = currentMillis;

        error("Known players sync conflict (409)!");
        error("Please, make sure that server-id #%d isn't used by another running server.", plugin.getServerId());
        error("If you have moved the server, shut down the old instance or refresh the shop key in EasyDonate control panel.");
    }

    private int resolveHttpCode(@NotNull Exception ex) {
        Throwable lastCause = ThrowableCauseFinder.findLastCause(ex);
        if (!(lastCause instanceof IOException))
            return -1;

        String message = lastCause.getMessage();
        if (message == null || message.isEmpty())
            return -1;

        Matcher matcher = RESPONSE_CODE_EXCEPTION.matcher(message);
        if (!matcher.matches())
            return -1;

        try {
            return Integer.parseInt(matcher.group("code"));
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

}
