package ru.easydonate.easypayments;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.cache.ReportCache;
import ru.easydonate.easypayments.config.AbstractConfiguration;
import ru.easydonate.easypayments.config.Configuration;
import ru.easydonate.easypayments.config.Messages;
import ru.easydonate.easypayments.database.Database;
import ru.easydonate.easypayments.database.DatabaseManager;
import ru.easydonate.easypayments.database.model.Customer;
import ru.easydonate.easypayments.database.model.Purchase;
import ru.easydonate.easypayments.database.persister.LocalDateTimePersister;
import ru.easydonate.easypayments.easydonate4j.extension.client.EasyPaymentsClient;
import ru.easydonate.easypayments.exception.ConfigurationValidationException;
import ru.easydonate.easypayments.exception.CredentialsParseException;
import ru.easydonate.easypayments.exception.DriverLoadException;
import ru.easydonate.easypayments.exception.DriverNotFoundException;
import ru.easydonate.easypayments.nms.NMSHelper;
import ru.easydonate.easypayments.nms.UnsupportedVersionException;
import ru.easydonate.easypayments.task.PaymentsQueryTask;
import ru.easydonate.easypayments.task.PluginTask;
import ru.easydonate.easypayments.task.ReportCacheWorker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class EasyPaymentsPlugin extends JavaPlugin {

    public static final String COMMAND_EXECUTOR_NAME = "@EasyPayments";
    public static final String TROUBLESHOOTING_POST_URL = "https://vk.cc/c3JBSF";
    public static final String USER_AGENT_FORMAT = "EasyPayments %s";

    private static EasyPaymentsPlugin instance;

    private final Configuration config;
    private final Messages messages;
    private final String userAgent;

    private DatabaseManager databaseManager;

    private NMSHelper nmsHelper;
    private EasyPaymentsClient easyPaymentsClient;
    private ReportCache reportCache;

    private PluginTask paymentsQueryTask;
    private PluginTask reportCacheWorker;

    private String accessKey;
    private int serverId;
    private int permissionLevel;

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
                    .registerTable(Purchase.class)
                    .registerPersister(LocalDateTimePersister.getSingleton())
                    .complete();

            this.databaseManager = new DatabaseManager(this, database);
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

        // loading some important things
        this.easyPaymentsClient = EasyPaymentsClient.create(accessKey, userAgent, serverId);
        this.reportCache = new ReportCache(this);
        this.reportCache.loadReports();

        // starting tasks
        launchTasks();

        info(" ");
        info(" &eEasyPayments &ris an official payment processing implementation.");
        info(" &6© EasyDonate 2020-2021 &r- All rights reserved.");
        info(" ");

        getServer().getScheduler().runTaskAsynchronously(this, this::checkForUpdates);
    }

    @Override
    public void onDisable() {
        if(paymentsQueryTask != null)
            paymentsQueryTask.shutdown();
        if(reportCacheWorker != null)
            reportCacheWorker.shutdown();
    }

    private void loadConfigurations() {
        config.reload();
        messages.reload();
    }

    private void validateConfiguration(@NotNull Configuration config) {
        // validating the shop key
        String accessKey = config.getString("key");
        if(accessKey == null || accessKey.isEmpty())
            throw new ConfigurationValidationException("Please, specify your unique shop key in the config.yml!");

        // validating the server ID
        int serverId = config.getInt("server-id", -1);
        if(serverId < 1)
            throw new ConfigurationValidationException("Please, specify your valid server ID in the config.yml!");

        // changing the permission level
        this.permissionLevel = config.getInt("permission-level", 4);
        if(permissionLevel < 0)
            permissionLevel = 0;
    }

    private boolean resolveNMSImplementation() {
        try {
            this.nmsHelper = NMSHelper.resolve(this, COMMAND_EXECUTOR_NAME, permissionLevel);
            return true;
        } catch (UnsupportedVersionException ex) {
            error("Couldn't find a NMS implementation for your server version!");
            error("Currently supported versions is all from %s to %s.", Constants.MIN_SUPPORTED_VERSION_X, Constants.MAX_SUPPORTED_VERSION_X);
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }
    }

    private void launchTasks() {
        this.reportCacheWorker = new ReportCacheWorker(this, easyPaymentsClient, reportCache);
        this.reportCacheWorker.start();

        this.paymentsQueryTask = new PaymentsQueryTask(this, easyPaymentsClient, nmsHelper, reportCache);
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

    // TODO refactoring
    private void checkForUpdates() {
        InputStream resource = getClass().getResourceAsStream("/module");
        if(resource == null) {
            onFailedToCheckForUpdates(10);
            return;
        }

        String moduleId = null;
        try {
            InputStreamReader streamReader = new InputStreamReader(resource, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(streamReader);

            moduleId = bufferedReader.readLine();
        } catch (IOException ignored) {}

        if(moduleId == null || moduleId.isEmpty()) {
            onFailedToCheckForUpdates(11);
            return;
        }

        if(!moduleId.equals("alcor") && !moduleId.equals("sirius")) {
            onFailedToCheckForUpdates(12);
            return;
        }

        String currentVersion = getDescription().getVersion();
        try {
            VersionResponse response = easyPaymentsClient.checkForUpdates(moduleId);
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

    private void onFailedToCheckForUpdates(int errorCode) {
        error("Failed to load plugin! Error code: %d.", errorCode);
        error("The solution for this problem can be found here: %s", TROUBLESHOOTING_POST_URL);
        getServer().getPluginManager().disablePlugin(this);
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