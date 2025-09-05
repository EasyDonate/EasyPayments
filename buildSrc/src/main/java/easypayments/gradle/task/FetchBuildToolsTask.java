package easypayments.gradle.task;

import easypayments.gradle.util.Downloader;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.file.Path;

public abstract class FetchBuildToolsTask extends DefaultTask {

    private static final String LSB_ARTIFACT_URL = "https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar";

    @TaskAction
    public void run() throws IOException {
        getLogger().info("Downloading BuildTools.jar...");
        Downloader.readAsFile(LSB_ARTIFACT_URL, getJarPath().get());
    }

    @Input
    public abstract Property<Path> getJarPath();

}
