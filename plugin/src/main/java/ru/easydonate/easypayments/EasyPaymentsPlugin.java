package ru.easydonate.easypayments;

import com.j256.ormlite.logger.Level;
import com.j256.ormlite.logger.Logger;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.config.AbstractConfiguration;
import ru.easydonate.easypayments.config.Configuration;
import ru.easydonate.easypayments.config.Messages;
import ru.easydonate.easypayments.database.Database;
import ru.easydonate.easypayments.database.DatabaseManager;
import ru.easydonate.easypayments.database.model.Customer;
import ru.easydonate.easypayments.database.model.Payment;
import ru.easydonate.easypayments.database.model.Purchase;
import ru.easydonate.easypayments.database.persister.LocalDateTimePersister;
import ru.easydonate.easypayments.easydonate4j.extension.client.EasyPaymentsClient;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.VersionResponse;
import ru.easydonate.easypayments.exception.ConfigurationValidationException;
import ru.easydonate.easypayments.exception.CredentialsParseException;
import ru.easydonate.easypayments.exception.DriverLoadException;
import ru.easydonate.easypayments.exception.DriverNotFoundException;
import ru.easydonate.easypayments.execution.ExecutionController;
import ru.easydonate.easypayments.execution.interceptor.InterceptorFactory;
import ru.easydonate.easypayments.nms.UnsupportedVersionException;
import ru.easydonate.easypayments.nms.provider.VersionedFeaturesProvider;
import ru.easydonate.easypayments.shopcart.ShopCartStorage;
import ru.easydonate.easypayments.task.PaymentsQueryTask;
import ru.easydonate.easypayments.task.PluginTask;
import ru.easydonate.easypayments.task.ReportCacheWorker;

import java.util.concurrent.CompletableFuture;

public class EasyPaymentsPlugin extends JavaPlugin {

    public static final String COMMAND_EXECUTOR_NAME = "@EasyPayments";
    public static final String TROUBLESHOOTING_POST_URL = "https://vk.cc/c3JBSF";
    public static final String USER_AGENT_FORMAT = "EasyPayments %s";

    private static EasyPaymentsPlugin instance;

    private final Configuration config;
    private final Messages messages;
    private final String userAgent;

    private DatabaseManager databaseManager;
    private VersionedFeaturesProvider versionedFeaturesProvider;

    private EasyPaymentsClient easyPaymentsClient;
    private ShopCartStorage shopCartStorage;
    private ExecutionController executionController;

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
        this.config = new Configuration(this, "config.yml").withValidator(this::validateConfiguration);
        this.messages = new Messages(this, config);
        this.userAgent = String.format(USER_AGENT_FORMAT, getDescription().getVersion());
    }

    @Override
    public void onEnable() {
        instance = this;

        // loading the plugin configurations
        try {
            loadConfigurations();
        } catch (ConfigurationValidationException ex) {
            reportException(ex);
            return;
        }

        // database initialization
        try {
            Database database = new Database(this, config)
                    .registerTable(Customer.class)
                    .registerTable(Payment.class)
                    .registerTable(Purchase.class)
                    .registerPersister(LocalDateTimePersister.getSingleton())
                    .complete();

            this.databaseManager = new DatabaseManager(this, config, database);
        } catch (CredentialsParseException ex) {
            reportException(ex, "Couldn't parse a database connection credentials:");
            return;
        } catch (DriverNotFoundException | DriverLoadException ex) {
            reportException(ex, "Couldn't load a database connection driver:");
            return;
        } catch (Exception ex) {
            reportException(ex, "An error has occurred when this plugin tried to establish the database connection:");
            ex.printStackTrace();
            return;
        }

        // resolving the NMS implementation
        if(!resolveNMSImplementation())
            return;

        // API client initialization
        this.easyPaymentsClient = EasyPaymentsClient.create(accessKey, userAgent, serverId);

        // shop carts storage initialization
        this.shopCartStorage = new ShopCartStorage(databaseManager);

        // execution controller initialization
        InterceptorFactory interceptorFactory = versionedFeaturesProvider.getInterceptorFactory();
        this.executionController = new ExecutionController(this, config, databaseManager, shopCartStorage, interceptorFactory);

        // starting tasks
        launchTasks();

        info(" ");
        info(" &eEasyPayments &ris an official payment processing implementation.");
        info(" &6© EasyDonate 2020-2022 &r- All rights reserved.");
        info(" ");

        getServer().getScheduler().runTaskAsynchronously(this, this::checkForUpdates);
    }

    @Override
    public void onDisable() {
        CompletableFuture<Void> paymentsQueryTaskFuture = null;
        CompletableFuture<Void> reportCacheWorkerFuture = null;

        if(paymentsQueryTask != null)
            paymentsQueryTaskFuture = paymentsQueryTask.shutdownAsync();

        if(reportCacheWorker != null)
            reportCacheWorkerFuture = reportCacheWorker.shutdownAsync();

        if(paymentsQueryTaskFuture != null || reportCacheWorkerFuture != null) {
            getLogger().info("Closing internal tasks...");

            if(paymentsQueryTaskFuture != null) {
                paymentsQueryTaskFuture.join();
            }

            if(reportCacheWorkerFuture != null) {
                reportCacheWorkerFuture.join();
            }
        }

        if(databaseManager != null)
            databaseManager.shutdown();
    }

    private void loadConfigurations() {
        config.reload();
        messages.reload();
    }

    private void validateConfiguration(@NotNull Configuration config) {
        // validating the shop key
        this.accessKey = config.getString("key");
        if(accessKey == null || accessKey.isEmpty())
            throw new ConfigurationValidationException("Please, specify your unique shop key in the config.yml!");

        // validating the server ID
        this.serverId = config.getInt("server-id", 0);
        if(serverId < 1)
            throw new ConfigurationValidationException("Please, specify your valid server ID in the config.yml!");

        // changing the permission level
        this.permissionLevel = config.getInt("permission-level", 4);
        if(permissionLevel < 0)
            permissionLevel = 0;
    }

    private boolean resolveNMSImplementation() {
        try {
            this.versionedFeaturesProvider = VersionedFeaturesProvider.builder(this)
                    .withExecutorName(COMMAND_EXECUTOR_NAME)
                    .withPermissionLevel(permissionLevel)
                    .create();

            return true;
        } catch (UnsupportedVersionException ex) {
            error("Couldn't find a NMS implementation for your server version!");
            error("Currently supported versions is all from %s to %s.", Constants.MIN_SUPPORTED_VERSION_X, Constants.MAX_SUPPORTED_VERSION_X);
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }
    }

    private void launchTasks() {
        this.reportCacheWorker = new ReportCacheWorker(this, executionController, easyPaymentsClient);
        this.reportCacheWorker.start();

        this.paymentsQueryTask = new PaymentsQueryTask(this, executionController, easyPaymentsClient);
        this.paymentsQueryTask.start();
    }

    private void reportException(@NotNull Throwable ex) {
        reportException(ex, null);
    }

    private void reportException(@NotNull Throwable ex, @Nullable String message, @Nullable Object... args) {
        if(message != null)
            error(message, args);

        error(ex.toString());
        error("Need a help? Please, check our guide here: %s", TROUBLESHOOTING_POST_URL);
        error("Disabling plugin...");
        getServer().getPluginManager().disablePlugin(this);
    }

    private void checkForUpdates() {
        String currentVersion = getDescription().getVersion();
        try {
            VersionResponse response = easyPaymentsClient.checkForUpdates(currentVersion);
            if(response != null) {
                String downloadUrl = response.getDownloadUrl();
                String version = response.getVersion();
                if(downloadUrl != null && version != null) {
                    info(" ");
                    info(" &cHey! &rA new version of &eEasyPayments &ravailable!");
                    info(" &rYour version: &b%s&r, available version: &a%s", currentVersion, version);
                    info(" &rDownload: &6%s", downloadUrl);
                    info(" ");
                }
            }
        } catch (Exception ignored) {}
    }

    private void info(@NotNull String message, @Nullable Object... args) {
        getLogger().info(AbstractConfiguration.colorize(String.format(message, args)));
    }

    private void error(@NotNull String message, @Nullable Object... args) {
        getLogger().severe(AbstractConfiguration.colorize(String.format(message, args)));
    }

    public static @NotNull String getVersion() {
        return instance.getDescription().getVersion();
    }

    public static boolean isDebugEnabled() {
        return instance.getConfig().getBoolean("logging.debug", false);
    }

    public static boolean logQueryTaskErrors() {
        return instance.getConfig().getBoolean("logging.query-task-errors", false);
    }

    public static boolean logCacheWorkerWarnings() {
        return instance.getConfig().getBoolean("logging.cache-worker-warnings", false);
    }

    public static boolean logCacheWorkerErrors() {
        return instance.getConfig().getBoolean("logging.cache-worker-errors", false);
    }

}