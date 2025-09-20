package ru.easydonate.easypayments;

import com.j256.ormlite.logger.Level;
import com.j256.ormlite.logger.Logger;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.command.easypayments.CommandEasyPayments;
import ru.easydonate.easypayments.command.exception.ExecutionException;
import ru.easydonate.easypayments.command.exception.InitializationException;
import ru.easydonate.easypayments.command.shopcart.CommandShopCart;
import ru.easydonate.easypayments.core.Constants;
import ru.easydonate.easypayments.core.EasyPayments;
import ru.easydonate.easypayments.core.config.Configuration;
import ru.easydonate.easypayments.core.config.localized.Messages;
import ru.easydonate.easypayments.core.config.template.TemplateConfiguration;
import ru.easydonate.easypayments.core.easydonate4j.extension.client.EasyPaymentsClient;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.PluginStateModel;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.PluginVersionModel;
import ru.easydonate.easypayments.core.exception.ConfigurationValidationException;
import ru.easydonate.easypayments.core.formatting.RelativeTimeFormatter;
import ru.easydonate.easypayments.core.interceptor.InterceptorFactory;
import ru.easydonate.easypayments.core.logging.DebugLogger;
import ru.easydonate.easypayments.core.platform.UnsupportedPlatformException;
import ru.easydonate.easypayments.core.platform.provider.PlatformProvider;
import ru.easydonate.easypayments.core.platform.provider.PlatformProviderBase;
import ru.easydonate.easypayments.core.platform.provider.PlatformResolver;
import ru.easydonate.easypayments.database.Database;
import ru.easydonate.easypayments.database.DatabaseManager;
import ru.easydonate.easypayments.database.model.Customer;
import ru.easydonate.easypayments.database.model.Payment;
import ru.easydonate.easypayments.database.model.Purchase;
import ru.easydonate.easypayments.database.persister.LocalDateTimePersister;
import ru.easydonate.easypayments.exception.*;
import ru.easydonate.easypayments.listener.CommandPreProcessListener;
import ru.easydonate.easypayments.listener.PlayerJoinQuitListener;
import ru.easydonate.easypayments.service.*;
import ru.easydonate.easypayments.service.execution.ExecutionService;
import ru.easydonate.easypayments.setup.InteractiveSetupProvider;
import ru.easydonate.easypayments.shopcart.ShopCartConfig;
import ru.easydonate.easypayments.shopcart.ShopCartStorage;
import ru.easydonate.easypayments.task.KnownPlayersSyncTask;
import ru.easydonate.easypayments.task.PaymentsQueryTask;
import ru.easydonate.easypayments.task.PluginTask;
import ru.easydonate.easypayments.task.ReportCacheWorker;

import java.util.Calendar;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import static ru.easydonate.easypayments.core.util.AnsiColorizer.colorize;

public class EasyPaymentsPlugin extends JavaPlugin implements EasyPayments {

    public static final String COMMAND_EXECUTOR_NAME = "@EasyPayments";
    public static final String TROUBLESHOOTING_PAGE_URL = "https://easypayments.easydonate.ru";
    public static final String SUPPORT_URL = "https://vk.me/easydonate";
    public static final String USER_AGENT_FORMAT = "EasyPayments %s";

    public static final int ACCESS_KEY_LENGTH = 32;
    public static final Pattern ACCESS_KEY_REGEX = Pattern.compile("[a-f\\d]{32}");
    public static final String CONFIG_KEY_ACCESS_KEY = "key";
    public static final String CONFIG_KEY_SERVER_ID = "server-id";
    public static final int STATE_QUERY_ATTEMPTS = 5;

    private static EasyPaymentsPlugin instance;

    @Getter
    private final DebugLogger debugLogger;
    private final String userAgent;
    private volatile boolean pluginEnabled;
    private volatile boolean pluginConfigured;

    private final Configuration config;
    @Getter
    private final Messages messages;
    @Getter
    private final ShopCartConfig shopCartConfig;

    private DatabaseManager databaseManager;
    @Getter
    private PersistanceService persistanceService;

    @Getter
    private PlatformProvider platformProvider;
    private EasyPaymentsClient easyPaymentsClient;
    private InteractiveSetupProvider setupProvider;
    @Getter
    private ShopCartStorage shopCartStorage;

    @Getter private ExecutionService executionService;
    @Getter private LongPollEventDispatcher lpEventDispatcher;
    @Getter private IssuanceReportService issuanceReportService;
    @Getter private IssuancePerformService issuancePerformService;
    @Getter private KnownPlayersService knownPlayersService;

    @Getter
    private RelativeTimeFormatter relativeTimeFormatter;
    private PluginStateModel pluginStateModel;
    private PluginVersionModel pluginVersionModel;

    private PluginTask knownPlayersSyncTask;
    private PluginTask paymentsQueryTask;
    private PluginTask reportCacheWorker;

    @Getter private String accessKey;
    @Getter private int serverId;
    @Getter private int permissionLevel;

    static {
        // Disable useless ORMLite logging
        Logger.setGlobalLogLevel(Level.ERROR);
    }

    public EasyPaymentsPlugin() {
        this.debugLogger = new DebugLogger(this);
        this.userAgent = String.format(USER_AGENT_FORMAT, getDescription().getVersion());
        this.pluginEnabled = true;

        this.config = new TemplateConfiguration(this, "config.yml");
        this.config.registerKeyAliases("shop-cart.enabled", "use-shop-cart");
        this.config.setValidator(this::validateConfiguration);
        this.messages = new Messages(this, config);
        this.shopCartConfig = new ShopCartConfig(config, debugLogger);

        this.pluginStateModel = PluginStateModel.DEFAULT;
    }

    @Override
    public void onEnable() {
        instance = this;
        debugLogger.info("--- STATE: ENABLING ---");

        // resolving the platform implementation
        if (!resolvePlatformImplementation())
            return;

        try {
            // loading the plugin configurations
            loadConfigurations();
            this.pluginEnabled = true;
        } catch (ConfigurationValidationException ex) {
            debugLogger.error("Configuration validation failed");
            debugLogger.error(ex);
            changeEnabledState(false);
            reportException(ex);
        }

        // API client initialization
        initializeApiClient();
        CompletableFuture<PluginStateModel> future = deferRemoteStateQuery();

        try {
            // loading storage
            loadStorage();
        } catch (StorageLoadException ex) {
            debugLogger.error("Storage loading failed");
            debugLogger.error(ex);
            changeEnabledState(false);
        }

        // interactive setup provider initialization
        debugLogger.debug("Initializing interactive setup...");
        this.setupProvider = new InteractiveSetupProvider(this, config, messages);

        // shop carts storage initialization
        debugLogger.debug("Initializing shop cart storage...");
        this.shopCartStorage = new ShopCartStorage(this);

        // relative time formatter initialization
        this.relativeTimeFormatter = new RelativeTimeFormatter(messages);

        // services initialization
        loadServices();

        // commands executors initialization
        registerCommands();

        // event listeners initialization
        registerListeners();

        awaitRemoteStateQuery(future);

        if (pluginEnabled()) {
            // starting tasks
            launchTasks();

            info(" ");
            info(" &eEasyPayments &ris an official payment processing implementation.");
            info(" &6© EasyDonate 2020-%d &r- All rights reserved.", Calendar.getInstance().get(Calendar.YEAR));
            info(" ");
        }

        platformProvider.getScheduler().runAsyncNow(this, this::checkForUpdates);
        debugLogger.info("--- STATE: ENABLED ---");
    }

    @Override
    public void onDisable() {
        debugLogger.info("--- STATE: DISABLING ---");

        // closing internal tasks
        closeTasks();

        // shutting down services
        shutdownServices();

        // shutting down the API client
        shutdownApiClient();

        // closing storage
        closeStorage();

        debugLogger.info("--- STATE: DISABLED ---");
        debugLogger.shutdown();
    }

    public synchronized void reload() throws ConfigurationValidationException, StorageLoadException {
        debugLogger.info("--- STATE: RELOADING ---");

        // shutting down
        closeTasks();
        shutdownServices();
        shutdownApiClient();
        closeStorage();

        changeEnabledState(false);

        // loading all again
        loadConfigurations();
        initializeApiClient();
        CompletableFuture<PluginStateModel> future = deferRemoteStateQuery();
        loadStorage();
        loadServices();
        awaitRemoteStateQuery(future);
        launchTasks();

        changeEnabledState(true);
        debugLogger.info("--- STATE: RELOADED ---");
    }

    private synchronized void loadConfigurations() throws ConfigurationValidationException {
        debugLogger.debug("Loading configurations...");
        ConfigurationValidationException exception = null;

        try {
            config.reload();
            this.pluginConfigured = true;
        } catch (ConfigurationValidationException ex) {
            exception = ex;
            this.pluginConfigured = false;
        }

        messages.reload();
        shopCartConfig.reload();

        if (exception != null)
            throw exception;
    }

    private synchronized void loadStorage() throws StorageLoadException {
        debugLogger.debug("Loading storage...");
        this.databaseManager = null;

        try {
            // database initialization
            Database database = new Database(this, config)
                    .registerTables(Customer.class, Payment.class, Purchase.class)
                    .registerPersister(LocalDateTimePersister.getSingleton())
                    .complete();

            this.databaseManager = new DatabaseManager(this, config, database);
            this.persistanceService = new PersistanceService(this, databaseManager);
        } catch (CredentialsParseException ex) {
            reportException(ex, "Couldn't parse a database connection credentials:");
            throw new StorageLoadException(ex);
        } catch (DriverNotFoundException | DriverLoadException ex) {
            reportException(ex, "Couldn't load a database connection driver:");
            throw new StorageLoadException(ex);
        } catch (Exception ex) {
            reportException(ex, "An error has occurred when this plugin tried to establish the database connection:");
            disablePlugin();
            throw new StorageLoadException(ex);
        }
    }

    private synchronized void closeStorage() {
        if (databaseManager != null) {
            debugLogger.debug("Shutting down storage...");
            databaseManager.shutdown();
        }
    }

    private synchronized void loadServices() {
        debugLogger.debug("Loading execution service...");
        InterceptorFactory interceptorFactory = platformProvider.getInterceptorFactory();
        this.executionService = new ExecutionService(this, config, interceptorFactory);

        debugLogger.debug("Loading issuance report service...");
        this.issuanceReportService = new IssuanceReportService(this, persistanceService, easyPaymentsClient);

        debugLogger.debug("Loading issuance perform service...");
        this.issuancePerformService = new IssuancePerformService(this, issuanceReportService, executionService, persistanceService);

        debugLogger.debug("Loading LongPoll event dispatcher...");
        this.lpEventDispatcher = new LongPollEventDispatcher(this, executionService);

        debugLogger.debug("Loading known players service...");
        this.knownPlayersService = new KnownPlayersService();
    }

    private synchronized void shutdownServices() {
        if (lpEventDispatcher != null) {
            debugLogger.debug("Shutting down LongPoll event dispatcher...");
            lpEventDispatcher.shutdown();
        }

        if (executionService != null) {
            debugLogger.debug("Shutting down execution service...");
            executionService.shutdown();
        }
    }

    private synchronized void validateConfiguration(@NotNull Configuration config) throws ConfigurationValidationException {
        debugLogger.debug("[Validation] Validating configuration '{0}'...", config.getName());

        // validating the shop key
        this.accessKey = config.getString("key");
        if (accessKey == null || accessKey.isEmpty()) {
            debugLogger.error("[Validation] Bad access key: '{0}'", accessKey);
            throw new ConfigurationValidationException("Please, specify your unique shop key in the config.yml!");
        }

        // validating the shop key format
        this.accessKey = accessKey.toLowerCase();
        if (accessKey.length() != ACCESS_KEY_LENGTH || !ACCESS_KEY_REGEX.matcher(accessKey).matches()) {
            debugLogger.error("[Validation] Bad access key: '{0}'", accessKey);
            throw new ConfigurationValidationException("Please, specify a VALID shop key (32 hex chars) in the config.yml!");
        }

        // validating the server ID
        this.serverId = config.getInt("server-id", 0);
        if (serverId < 1) {
            debugLogger.error("[Validation] Bad server ID: '{0}'", serverId);
            throw new ConfigurationValidationException("Please, specify your valid server ID in the config.yml!");
        }

        debugLogger.debug("[Validation] Validation passed");

        // update permission level of any further command executors
        this.permissionLevel = config.getIntWithBounds("permission-level", 0, 4, 4);

        // updating permission level in existing platform provider instance
        if (platformProvider != null) {
            debugLogger.debug("Updating interceptor factory with username '{0}' and permission level '{1}'", COMMAND_EXECUTOR_NAME, permissionLevel);
            ((PlatformProviderBase) platformProvider).updateInterceptorFactory(COMMAND_EXECUTOR_NAME, permissionLevel);
        }

        // clean old log files
        int logFileTTL = config.getInt("log-file-time-to-life");
        debugLogger.cleanLogsDir(logFileTTL);
    }

    private boolean resolvePlatformImplementation() {
        debugLogger.debug("[Platform] Resolving platform implementation...");

        try {
            PlatformResolver platformResolver = new PlatformResolver(this, debugLogger);
            this.platformProvider = platformResolver.resolve(COMMAND_EXECUTOR_NAME, permissionLevel);
            info("Detected platform: &b%s", platformProvider.getName());
            debugLogger.info("[Platform] Using implementation: {0}", platformProvider.getClass().getName());
            return true;
        } catch (UnsupportedPlatformException ex) {
            debugLogger.error("[Platform] Unsupported platform!");
            debugLogger.error(ex);

            error("Couldn't find a platform implementation for your server software!");

            if (ex.getMessage() != null)
                error("Failure reason: '%s'", ex.getMessage());

            error("----------------------------------------------------------------------");
            error("Currently, EasyPayments plugin is compatible with:");
            error("- Spigot: %s - %s", Constants.MIN_SUPPORTED_SPIGOT, Constants.MAX_SUPPORTED_SPIGOT);
            error("- Paper: %s or newer", Constants.MIN_SUPPORTED_PAPER);
            error("- Folia: %s or newer", Constants.MIN_SUPPORTED_FOLIA);
            error("Proper plugin working on unlisted software isn't guaranteed.");

            getServer().getPluginManager().disablePlugin(this);
            return false;
        }
    }

    private void initializeApiClient() {
        debugLogger.debug("Initializing EasyDonate API client...");
        this.easyPaymentsClient = EasyPaymentsClient.create(accessKey, userAgent, serverId);
    }

    private void shutdownApiClient() {
        if (easyPaymentsClient != null) {
            debugLogger.debug("Shutting down EasyDonate API client...");
            easyPaymentsClient.getLongPollClient().shutdown();
        }
    }

    private void registerCommands() throws InitializationException {
        debugLogger.debug("Registering commands...");
        new CommandEasyPayments(this, config, messages, setupProvider);
        new CommandShopCart(this, messages, shopCartStorage);
    }

    private void registerListeners() {
        debugLogger.debug("Registering event listeners...");
        new PlayerJoinQuitListener(this, messages, shopCartStorage, knownPlayersService);
        new CommandPreProcessListener(this, setupProvider);
    }

    private void launchTasks() {
        debugLogger.debug("[Tasks] Launching plugin tasks...");

        if (isPurchasesIssuanceActive()) {
            debugLogger.debug("[Tasks] - Launching report cache worker task...");
            this.reportCacheWorker = new ReportCacheWorker(this, issuanceReportService, persistanceService);
            this.reportCacheWorker.start();

            debugLogger.debug("[Tasks] - Launching payments query task...");
            this.paymentsQueryTask = new PaymentsQueryTask(this, easyPaymentsClient, lpEventDispatcher, issuanceReportService);
            this.paymentsQueryTask.start();
        }

        if (isPlayersSyncActive()) {
            debugLogger.debug("[Tasks] - Launching known players sync task...");
            this.knownPlayersSyncTask = new KnownPlayersSyncTask(this, knownPlayersService, easyPaymentsClient);
            this.knownPlayersSyncTask.start();
        }
    }

    private void closeTasks() {
        CompletableFuture<Void> knownPlayersSyncTaskFuture = null;
        CompletableFuture<Void> paymentsQueryTaskFuture = null;
        CompletableFuture<Void> reportCacheWorkerFuture = null;

        if (knownPlayersSyncTask != null)
            knownPlayersSyncTaskFuture = knownPlayersSyncTask.shutdownAsync();

        if (paymentsQueryTask != null)
            paymentsQueryTaskFuture = paymentsQueryTask.shutdownAsync();

        if (reportCacheWorker != null)
            reportCacheWorkerFuture = reportCacheWorker.shutdownAsync();

        if (knownPlayersSyncTaskFuture != null || paymentsQueryTaskFuture != null || reportCacheWorkerFuture != null) {
            getLogger().info("Closing internal tasks...");
            debugLogger.info("Closing internal tasks...");

            if (knownPlayersSyncTaskFuture != null) {
                knownPlayersSyncTaskFuture.join();
                this.knownPlayersSyncTask = null;
            }

            if (paymentsQueryTaskFuture != null) {
                paymentsQueryTaskFuture.join();
                this.paymentsQueryTask = null;
            }

            if (reportCacheWorkerFuture != null) {
                reportCacheWorkerFuture.join();
                this.reportCacheWorker = null;
            }
        }
    }

    private void reportException(@NotNull Throwable ex) {
        reportException(ex, null);
    }

    private void reportException(@NotNull Throwable ex, @Nullable String message, @Nullable Object... args) {
        if (message != null)
            error(message, args);

        error(ex instanceof ConfigurationValidationException ? ex.getMessage() : ex.toString());
        error("Need a help? You can learn the documentation here:");
        error("> %s", TROUBLESHOOTING_PAGE_URL);
    }

    private void disablePlugin() {
        error("Disabling plugin...");
        getServer().getPluginManager().disablePlugin(this);
    }

    private @NotNull CompletableFuture<PluginStateModel> deferRemoteStateQuery() {
        CompletableFuture<PluginStateModel> future = new CompletableFuture<>();
        platformProvider.getScheduler().runAsyncNow(this, () -> {
            try {
                PluginStateModel pluginStateModel = PluginStateModel.DEFAULT;
                int receivedOnAttempt = 0;

                for (int attempt = 0; attempt < STATE_QUERY_ATTEMPTS; attempt++) {
                    try {
                        pluginStateModel = easyPaymentsClient.getPluginState();
                        receivedOnAttempt = attempt;
                    } catch (Exception ex) {
                        if (attempt == STATE_QUERY_ATTEMPTS - 1)
                            throw ex;

                        debugLogger.warn("Couldn't query remote state! ({0} of {1})", attempt + 1, STATE_QUERY_ATTEMPTS);
                        debugLogger.warn(ex);
                    }
                }

                if (receivedOnAttempt > 0 && pluginConfigured)
                    getLogger().warning(String.format(
                            "Remote state has been received on attempt #%d! Ensure that your Internet connection is stable.",
                            receivedOnAttempt + 1
                    ));

                debugLogger.debug("[RemoteState] Model: {0}", pluginStateModel);
                future.complete(pluginStateModel);
            } catch (Exception ex) {
                future.completeExceptionally(ex);
            }
        });
        return future;
    }

    private void awaitRemoteStateQuery(@NotNull CompletableFuture<PluginStateModel> future) {
        try {
            this.pluginStateModel = future.join();
        } catch (Exception ex) {
            this.pluginStateModel = PluginStateModel.DEFAULT;

            Throwable cause = ex.getCause();
            if (cause instanceof ExecutionException)
                cause = cause.getCause();

            debugLogger.error("[FATAL] Couldn't query remote state!");
            if (cause != null)
                debugLogger.error(cause);

            if (!pluginConfigured)
                return;

            error("[FATAL] Couldn't query remote state! Please, check your Internet connection!");
            error("[FATAL] Falling back to the default state! The plugin will not issue");
            error("[FATAL] purchases or sync players until server restart or '/ep reload'.");
        }
    }

    private void checkForUpdates() {
        String currentVersion = getDescription().getVersion();

        try {
            PluginVersionModel pluginVersionModel = easyPaymentsClient.getPluginVersion(currentVersion);
            debugLogger.debug("[CheckForUpdates] Model: {0}", pluginVersionModel);

            String downloadUrl = pluginVersionModel.getDownloadUrl();
            String version = pluginVersionModel.getVersion();

            if (downloadUrl != null && version != null) {
                this.pluginVersionModel = pluginVersionModel;
                debugLogger.info("[CheckForUpdates] Found new version {0}, current is {1}", version, currentVersion);

                info(" ");
                info(" &rA new version of &eEasyPayments &ravailable!");
                info(" &rYour version: &b%s&r, available version: &a%s", currentVersion, version);
                info(" &rDownload: &6%s", downloadUrl);
                info(" ");
            }
        } catch (Exception ignored) {}
    }

    private void info(@NotNull String message, @Nullable Object... args) {
        getLogger().info(colorize(String.format(message, args)));
    }

    private void error(@NotNull String message, @Nullable Object... args) {
        getLogger().severe(colorize(String.format(message, args)));
    }

    public @NotNull DatabaseManager getStorage() {
        if (!isPluginEnabled() || databaseManager == null)
            throw new PluginUnavailableException();

        return databaseManager;
    }

    public @NotNull Optional<PluginVersionModel> getPluginVersionModel() {
        return Optional.ofNullable(pluginVersionModel);
    }

    @Override
    public boolean isPlayersSyncActive() {
        return pluginEnabled() && pluginStateModel.isPlayersSyncActive();
    }

    @Override
    public boolean isPurchasesIssuanceActive() {
        return pluginEnabled() && pluginStateModel.isPurchaseIssuanceActive();
    }

    private boolean pluginEnabled() {
        return pluginEnabled;
    }

    private void changeEnabledState(boolean value) {
        this.pluginEnabled = value;
    }

    public static @NotNull String getVersion() {
        return instance.getDescription().getVersion();
    }

    public static boolean isPluginEnabled() {
        return instance.pluginEnabled();
    }

    public static boolean isStorageAvailable() {
        return isPluginEnabled() && instance.databaseManager != null;
    }

}