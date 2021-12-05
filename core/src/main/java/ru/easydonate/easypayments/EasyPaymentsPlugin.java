package ru.easydonate.easypayments;

import org.bukkit.plugin.java.JavaPlugin;
import ru.easydonate.easypayments.nms.NMSHelper;
import ru.easydonate.easypayments.nms.UnsupportedVersionException;
import ru.easydonate.easypayments.sdk.EasyPaymentsSDK;
import ru.easydonate.easypayments.sdk.data.model.VersionResponse;
import ru.easydonate.easypayments.task.ReportCacheWorker;
import ru.easydonate.easypayments.cache.ReportCache;
import ru.easydonate.easypayments.task.PaymentsQueryTask;
import ru.easydonate.easypayments.task.PluginTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class EasyPaymentsPlugin extends JavaPlugin {

    private static final String COMMAND_EXECUTOR_NAME;
    private static final String TROUBLESHOOTING_POST_URL;

    private static EasyPaymentsPlugin instance;

    private String shopKey;
    private int serverId;
    private int permissionLevel;
    private NMSHelper nmsHelper;

    private EasyPaymentsSDK sdk;
    private ReportCache reportCache;

    private PluginTask paymentsQueryTask;
    private PluginTask reportCacheWorker;

    static {
        COMMAND_EXECUTOR_NAME = "@EasyPayments";
        TROUBLESHOOTING_POST_URL = "https://vk.cc/c3JBSF";
    }

    @Override
    public void onEnable() {
        instance = this;

        // loading the plugin configuration
        loadConfiguration();

        // validating the plugin configuration
        if(!validateConfiguration())
            return;

        // resolving the NMS implementation
        if(!resolveNMSImplementation())
            return;

        // loading some important things
        this.sdk = EasyPaymentsSDK.create(this, shopKey, serverId);
        this.reportCache = new ReportCache(this);
        this.reportCache.loadReports();

        // starting tasks
        launchTasks();

        info(" ");
        info(" §eEasyPayments §ris an official payment processing implementation.");
        info(" §6© EasyDonate 2020-2021 §r- All rights reserved.");
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

    private void loadConfiguration() {
        saveDefaultConfig();
        reloadConfig();
    }

    private boolean validateConfiguration() {
        // validating the shop key
        this.shopKey = getConfig().getString("key");
        if(shopKey == null || shopKey.isEmpty()) {
            onFailedToValidateConfiguration("Please, specify your unique shop key in the config.yml");
            return false;
        }

        // validating the server ID
        this.serverId = getConfig().getInt("server-id", -1);
        if(serverId < 1) {
            onFailedToValidateConfiguration("Please, specify your server ID in the config.yml");
            return false;
        }

        // changing the permission level
        this.permissionLevel = getConfig().getInt("permission-level", 4);
        if(permissionLevel < 0)
            permissionLevel = 0;

        return true;
    }

    private boolean resolveNMSImplementation() {
        try {
            this.nmsHelper = NMSHelper.resolve(this, COMMAND_EXECUTOR_NAME, permissionLevel);
            return true;
        } catch (UnsupportedVersionException ex) {
            getLogger().severe("Couldn't find a NMS implementation for your server version!");
            getLogger().severe("Current supported versions is all from 1.8 to 1.17.");
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }
    }

    private void launchTasks() {
        this.reportCacheWorker = new ReportCacheWorker(this, sdk, reportCache);
        this.reportCacheWorker.start();

        this.paymentsQueryTask = new PaymentsQueryTask(this, sdk, nmsHelper, reportCache);
        this.paymentsQueryTask.start();
    }

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
            VersionResponse response = sdk.checkForUpdates(moduleId);
            if(response != null) {
                String downloadUrl = response.getDownloadUrl();
                String version = response.getVersion();
                if(downloadUrl != null && version != null) {
                    info(" ");
                    info(" §cHey! §rA new version of §eEasyPayments §ravailable!");
                    info(" §rYour version: §b%s§r, available version: §a%s", currentVersion, version);
                    info(" §rDownload: §6%s", downloadUrl);
                    info(" ");
                }
            }
        } catch (Exception ignored) {}
    }

    private void onFailedToValidateConfiguration(String message) {
        getLogger().severe(message);
        getLogger().severe("The solution for this problem can be found here: " + TROUBLESHOOTING_POST_URL);
        getServer().getPluginManager().disablePlugin(this);
    }

    private void onFailedToCheckForUpdates(int errorCode) {
        getLogger().severe("Failed to load plugin! Error code: " + errorCode);
        getLogger().severe("The solution for this problem can be found here: " + TROUBLESHOOTING_POST_URL);
        getServer().getPluginManager().disablePlugin(this);
    }

    private void info(String msg, Object... args) {
        getLogger().info(String.format(msg, args));
    }

    public static String getVersion() {
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