package easypayments.gradle.task;

import easypayments.gradle.util.Downloader;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;
import java.util.zip.ZipException;

public abstract class FetchBuildToolsTask extends DefaultTask {

    private static final String URL_PATTERN = "https://hub.spigotmc.org/jenkins/job/BuildTools/%d/artifact/target/BuildTools.jar";

    @TaskAction
    public void run() throws IOException {
        Path jarPath = getJarPath().get();
        int currentToolsVersion = fetchCurrentToolsVersion(jarPath);
        int latestToolsVersion = fetchLatestToolsVersion();

        if (latestToolsVersion <= currentToolsVersion) {
            getLogger().info("BuildTools is up to date: {} >= {}", currentToolsVersion, latestToolsVersion);
            return;
        }

        getLogger().info("BuildTools is out of date! Updating to #{}...", latestToolsVersion);
        Downloader.readAsFile(URL_PATTERN.formatted(latestToolsVersion), jarPath);
    }

    private int fetchCurrentToolsVersion(Path jarPath) throws IOException {
        if (!Files.isRegularFile(jarPath))
            return -1;

        try (JarFile jarFile = new JarFile(jarPath.toFile(), false)) {
            if (jarFile.getManifest().getMainAttributes().get("Implementation-Version") instanceof String implVersion) {
                return Integer.parseInt(implVersion.substring(implVersion.lastIndexOf('-') + 1));
            } else {
                return -1;
            }
        } catch (ZipException ex) {
            getLogger().warn("Failed to read BuildTools.jar!", ex);
            return -1;
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new GradleException("Failed to fetch current tools version from the existing JAR file!", ex);
        }
    }

    private int fetchLatestToolsVersion() {
        try {
            int toolsVersion = FetchBuildInfoTask.fetchBuildInfo("latest").getBuildToolsVersion();
            getLogger().info("Fetched latest BuildTools version: #{}", toolsVersion);
            return toolsVersion;
        } catch (Exception ex) {
            throw new GradleException("Failed to fetch latest tools version from the Spigot HUB!", ex);
        }
    }

    @Input
    public abstract Property<Path> getJarPath();

}
