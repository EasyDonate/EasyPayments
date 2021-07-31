package ru.soknight.easypayments;

import org.bukkit.plugin.java.JavaPlugin;
import ru.soknight.easypayments.cache.ReportCache;
import ru.soknight.easypayments.nms.NMSHelper;
import ru.soknight.easypayments.nms.UnsupportedVersionException;
import ru.soknight.easypayments.sdk.EasyPaymentsSDK;
import ru.soknight.easypayments.sdk.data.model.VersionResponse;
import ru.soknight.easypayments.task.PaymentsQueryTask;
import ru.soknight.easypayments.task.ReportCacheWorker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class EasyPaymentsPlugin extends JavaPlugin {

    private static final String TROUBLESHOOTING_POST_URL;

    private static EasyPaymentsPlugin instance;

    private PaymentsQueryTask paymentsQueryTask;
    private ReportCacheWorker reportCacheWorker;

    @Override
    public void onEnable() {
        instance = this;

        // loading the plugin configuration
        loadConfiguration();

        // changing the permission level
        int permissionLevel = getConfig().getInt("permission-level", 4);
        if(permissionLevel < 0)
            permissionLevel = 0;

        // validating the shop key
        String shopKey = getConfig().getString("key");
        if(shopKey == null || shopKey.isEmpty()) {
            getLogger().severe("Please, specify your unique shop key in the config.yml");
            getLogger().severe("The solution for this problem can be found here: " + TROUBLESHOOTING_POST_URL);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // validating the server ID
        int serverId = getConfig().getInt("server-id", -1);
        if(serverId < 1) {
            getLogger().severe("Please, specify your server ID in the config.yml");
            getLogger().severe("The solution for this problem can be found here: " + TROUBLESHOOTING_POST_URL);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // resolving the NMS implementation
        NMSHelper nmsHelper;
        try {
            nmsHelper = NMSHelper.resolve(this, "@EasyPayments", permissionLevel);
        } catch (UnsupportedVersionException ex) {
            getLogger().severe("Couldn't find a NMS implementation for your server version!");
            getLogger().severe("Current supported versions is all from 1.8 to 1.17.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // loading some important things
        EasyPaymentsSDK sdk = EasyPaymentsSDK.create(this, shopKey, serverId);
        ReportCache reportCache = new ReportCache(this);
        reportCache.loadReports();

        // starting tasks
        this.reportCacheWorker = new ReportCacheWorker(this, sdk, reportCache);
        this.reportCacheWorker.start();

        this.paymentsQueryTask = new PaymentsQueryTask(this, sdk, nmsHelper, reportCache);
        this.paymentsQueryTask.start();

        info(" ");
        info(" §eEasyPayments §ris an official payment processing implementation.");
        info(" §6© EasyDonate 2020-2021 §r- All rights reserved.");
        info(" ");

        getServer().getScheduler().runTaskAsynchronously(this, () -> checkForUpdates(sdk));
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

    private void checkForUpdates(EasyPaymentsSDK sdk) {
        InputStream resource = getClass().getResourceAsStream("/module");
        if(resource == null) {
            getLogger().severe("Failed to load plugin! Error code: 10");
            getLogger().severe("The solution for this problem can be found here: " + TROUBLESHOOTING_POST_URL);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        String moduleId = null;
        try {
            InputStreamReader streamReader = new InputStreamReader(resource, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(streamReader);

            moduleId = bufferedReader.readLine();
        } catch (IOException ignored) {}

        if(moduleId == null || moduleId.isEmpty()) {
            getLogger().severe("Failed to load plugin! Error code: 11");
            getLogger().severe("The solution for this problem can be found here: " + TROUBLESHOOTING_POST_URL);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if(!moduleId.equals("alcor") && !moduleId.equals("sirius")) {
            getLogger().severe("Failed to load plugin! Error code: 12");
            getLogger().severe("The solution for this problem can be found here: " + TROUBLESHOOTING_POST_URL);
            getServer().getPluginManager().disablePlugin(this);
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

    static {
        TROUBLESHOOTING_POST_URL = "https://vk.cc/c3JBSF";
    }

}