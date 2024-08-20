package ru.easydonate.easypayments;

import com.j256.ormlite.logger.Level;
import com.j256.ormlite.logger.Logger;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.command.easypayments.CommandEasyPayments;
import ru.easydonate.easypayments.command.exception.InitializationException;
import ru.easydonate.easypayments.command.shopcart.CommandShopCart;
import ru.easydonate.easypayments.core.Constants;
import ru.easydonate.easypayments.core.config.Configuration;
import ru.easydonate.easypayments.core.config.localized.Messages;
import ru.easydonate.easypayments.core.config.template.TemplateConfiguration;
import ru.easydonate.easypayments.core.easydonate4j.extension.client.EasyPaymentsClient;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.VersionResponse;
import ru.easydonate.easypayments.core.exception.ConfigurationValidationException;
import ru.easydonate.easypayments.core.formatting.RelativeTimeFormatter;
import ru.easydonate.easypayments.core.interceptor.InterceptorFactory;
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
import ru.easydonate.easypayments.execution.ExecutionController;
import ru.easydonate.easypayments.listener.CommandPreProcessListener;
import ru.easydonate.easypayments.listener.PlayerJoinQuitListener;
import ru.easydonate.easypayments.setup.InteractiveSetupProvider;
import ru.easydonate.easypayments.shopcart.ShopCartStorage;
import ru.easydonate.easypayments.task.PaymentsQueryTask;
import ru.easydonate.easypayments.task.PluginTask;
import ru.easydonate.easypayments.task.ReportCacheWorker;

import java.util.Calendar;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import static ru.easydonate.easypayments.core.util.AnsiColorizer.colorize;

public class EasyPaymentsPlugin extends JavaPlugin {

    public static final String COMMAND_EXECUTOR_NAME = "@EasyPayments";
    public static final String TROUBLESHOOTING_PAGE_URL = "https://easypayments.easydonate.ru";
    public static final String USER_AGENT_FORMAT = "EasyPayments %s";

    public static final int ACCESS_KEY_LENGTH = 32;
    public static final Pattern ACCESS_KEY_REGEX = Pattern.compile("[a-f\\d]{32}");
    public static final String CONFIG_KEY_ACCESS_KEY = "key";
    public static final String CONFIG_KEY_SERVER_ID = "server-id";

    private static EasyPaymentsPlugin instance;

    private final Configuration config;
    private final Messages messages;
    private final String userAgent;
    private boolean pluginEnabled;

    private DatabaseManager databaseManager;
    private PlatformProvider platformProvider;

    private EasyPaymentsClient easyPaymentsClient;
    private InteractiveSetupProvider setupProvider;
    private ShopCartStorage shopCartStorage;
    private ExecutionController executionController;
    private RelativeTimeFormatter relativeTimeFormatter;
    private VersionResponse versionResponse;

    private PluginTask paymentsQueryTask;
    private PluginTask reportCacheWorker;

    private String accessKey;
    private int serverId;
    private int permissionLevel;

    static {
        // Disable useless ORMLite logging
        Logger.setGlobalLogLevel(Level.ERROR);
    }

    public EasyPaymentsPlugin() {
        this.config = new TemplateConfiguration(this, "config.yml");
        this.config.setValidator(this::validateConfiguration);
        this.messages = new Messages(this, config);
        this.userAgent = String.format(USER_AGENT_FORMAT, getDescription().getVersion());
        this.pluginEnabled = true;
    }

    @Override
    public void onEnable() {
        instance = this;

        // resolving the platform implementation
        if (!resolvePlatformImplementation())
            return;

        try {
            // loading the plugin configurations
            loadConfigurations();

            // loading storage
            loadStorage();

            this.pluginEnabled = true;

            // API client initialization
            initializeApiClient();
        } catch (ConfigurationValidationException ex) {
            changeEnabledState(false);
            reportException(ex);
        } catch (StorageLoadException ex) {
            changeEnabledState(false);
        }

        // interactive setup provider initialization
        this.setupProvider = new InteractiveSetupProvider(this, config, messages);

        // shop carts storage initialization
        this.shopCartStorage = new ShopCartStorage(this);

        // relative time formatter initialization
        this.relativeTimeFormatter = new RelativeTimeFormatter(messages);

        // execution controller initialization
        loadExecutionController();

        // commands executors initialization
        registerCommands();

        // event listeners initialization
        registerListeners();

        if (pluginEnabled()) {
            // starting tasks
            launchTasks();

            info(" ");
            info(" &eEasyPayments &ris an official payment processing implementation.");
            info(" &6© EasyDonate 2020-%d &r- All rights reserved.", Calendar.getInstance().get(Calendar.YEAR));
            info(" ");
        }

        platformProvider.getScheduler().runAsyncNow(this, this::checkForUpdates);
    }

    @Override
    public void onDisable() {
        // closing internal tasks
        closeTasks();

        // shutting down execution controller
        shutdownExecutionController();

        // shutting down the API client
        shutdownApiClient();

        // closing storage
        closeStorage();
    }

    public @NotNull PlatformProvider getPlatformProvider() {
        return platformProvider;
    }

    public @NotNull DatabaseManager getStorage() {
        if (!isPluginEnabled() || databaseManager == null)
            throw new PluginUnavailableException();

        return databaseManager;
    }

    public @NotNull ExecutionController getExecutionController() {
        return executionController;
    }

    public @NotNull RelativeTimeFormatter getRelativeTimeFormatter() {
        return relativeTimeFormatter;
    }

    public @NotNull Optional<VersionResponse> getVersionResponse() {
        return Optional.ofNullable(versionResponse);
    }

    public @Nullable String getAccessKey() {
        return accessKey;
    }

    public int getServerId() {
        return serverId;
    }

    public int getPermissionLevel() {
        return permissionLevel;
    }

    public void reload() throws ConfigurationValidationException, StorageLoadException {
        synchronized (this) {
            this.pluginEnabled = false;

            // shutting down
            closeTasks();
            shutdownExecutionController();
            shutdownApiClient();
            closeStorage();

            // loading all again
            loadConfigurations();
            loadStorage();
            initializeApiClient();
            loadExecutionController();
            launchTasks();

            this.pluginEnabled = true;
        }
    }

    private synchronized void loadConfigurations() throws ConfigurationValidationException {
        ConfigurationValidationException exception = null;

        try {
            config.reload();
        } catch (ConfigurationValidationException ex) {
            exception = ex;
        }

        messages.reload();

        if (exception != null)
            throw exception;
    }

    private synchronized void loadStorage() throws StorageLoadException {
        this.databaseManager = null;

        try {
            // database initialization
            Database database = new Database(this, config)
                    .registerTables(Customer.class, Payment.class, Purchase.class)
                    .registerPersister(LocalDateTimePersister.getSingleton())
                    .complete();

            this.databaseManager = new DatabaseManager(this, config, database);
        } catch (CredentialsParseException ex) {
            reportException(ex, "Couldn't parse a database connection credentials:");
            throw new StorageLoadException(ex);
        } catch (DriverNotFoundException | DriverLoadException ex) {
            reportException(ex, "Couldn't load a database connection driver:");
            throw new StorageLoadException(ex);
        } catch (Exception ex) {
            reportException(ex, "An error has occurred when this plugin tried to establish the database connection:");
            disablePlugin();
            ex.printStackTrace();
            throw new StorageLoadException(ex);
        }
    }

    private synchronized void closeStorage() {
        if (databaseManager != null)
            databaseManager.shutdown();
    }

    private synchronized void loadExecutionController() {
        // execution controller initialization
        InterceptorFactory interceptorFactory = platformProvider.getInterceptorFactory();
        this.executionController = new ExecutionController(this, config, messages, easyPaymentsClient, shopCartStorage, interceptorFactory);
    }

    private synchronized void shutdownExecutionController() {
        if (executionController != null)
            executionController.shutdown();
    }

    private synchronized void validateConfiguration(@NotNull Configuration config) throws ConfigurationValidationException {
        // validating the shop key
        this.accessKey = config.getString("key");
        if (accessKey == null || accessKey.isEmpty())
            throw new ConfigurationValidationException("Please, specify your unique shop key in the config.yml!");

        // validating the shop key format
        this.accessKey = accessKey.toLowerCase();
        if (accessKey.length() != ACCESS_KEY_LENGTH || !ACCESS_KEY_REGEX.matcher(accessKey).matches())
            throw new ConfigurationValidationException("Please, specify a VALID shop key (32 hex chars) in the config.yml!");

        // validating the server ID
        this.serverId = config.getInt("server-id", 0);
        if (serverId < 1)
            throw new ConfigurationValidationException("Please, specify your valid server ID in the config.yml!");

        // changing the permission level
        this.permissionLevel = config.getInt("permission-level", 4);
        if (permissionLevel < 0)
            permissionLevel = 0;

        // updating permission level in existing platform provider instance
        if (platformProvider != null)
            ((PlatformProviderBase) platformProvider).updateInterceptorFactory(COMMAND_EXECUTOR_NAME, permissionLevel);
    }

    private boolean resolvePlatformImplementation() {
        try {
            PlatformResolver platformResolver = new PlatformResolver(this);
            this.platformProvider = platformResolver.resolve(COMMAND_EXECUTOR_NAME, permissionLevel);
            info("Using platform implementation: &b%s", platformProvider.getName());
            return true;
        } catch (UnsupportedPlatformException ex) {
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
        this.easyPaymentsClient = EasyPaymentsClient.create(accessKey, userAgent, serverId);
    }

    private void shutdownApiClient() {
        if (easyPaymentsClient != null)
            easyPaymentsClient.getLongPollClient().shutdown();
    }

    private void registerCommands() throws InitializationException {
        new CommandEasyPayments(this, config, messages, setupProvider);
        new CommandShopCart(this, messages, shopCartStorage);
    }

    private void registerListeners() {
        new PlayerJoinQuitListener(this, messages, shopCartStorage);
        new CommandPreProcessListener(this, setupProvider);
    }

    private void launchTasks() {
        this.reportCacheWorker = new ReportCacheWorker(this, executionController);
        this.reportCacheWorker.start();

        this.paymentsQueryTask = new PaymentsQueryTask(this, executionController);
        this.paymentsQueryTask.start();
    }

    private void closeTasks() {
        CompletableFuture<Void> paymentsQueryTaskFuture = null;
        CompletableFuture<Void> reportCacheWorkerFuture = null;

        if (paymentsQueryTask != null)
            paymentsQueryTaskFuture = paymentsQueryTask.shutdownAsync();

        if (reportCacheWorker != null)
            reportCacheWorkerFuture = reportCacheWorker.shutdownAsync();

        if (paymentsQueryTaskFuture != null || reportCacheWorkerFuture != null) {
            getLogger().info("Closing internal tasks...");

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

    private void checkForUpdates() {
        String currentVersion = getDescription().getVersion();
        try {
            VersionResponse response = easyPaymentsClient.checkForUpdates(currentVersion);
            if (response != null) {
                String downloadUrl = response.getDownloadUrl();
                String version = response.getVersion();
                if (downloadUrl != null && version != null) {
                    this.versionResponse = response;

                    info(" ");
                    info(" &rA new version of &eEasyPayments &ravailable!");
                    info(" &rYour version: &b%s&r, available version: &a%s", currentVersion, version);
                    info(" &rDownload: &6%s", downloadUrl);
                    info(" ");
                }
            }
        } catch (Exception ignored) {}
    }

    private void info(@NotNull String message, @Nullable Object... args) {
        getLogger().info(colorize(String.format(message, args)));
    }

    private void error(@NotNull String message, @Nullable Object... args) {
        getLogger().severe(colorize(String.format(message, args)));
    }

    private boolean pluginEnabled() {
        synchronized (this) {
            return pluginEnabled;
        }
    }

    private void changeEnabledState(boolean value) {
        synchronized (this) {
            this.pluginEnabled = value;
        }
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

    // --- TODO will be replaced with an own standalone logging system

    @Deprecated
    public static boolean isDebugEnabled() {
        return true;
    }

    @Deprecated
    public static boolean logQueryTaskErrors() {
        return true;
    }

    @Deprecated
    public static boolean logCacheWorkerWarnings() {
        return true;
    }

    @Deprecated
    public static boolean logCacheWorkerErrors() {
        return true;
    }

}