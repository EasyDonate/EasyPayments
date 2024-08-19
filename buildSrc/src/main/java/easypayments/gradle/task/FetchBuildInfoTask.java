package easypayments.gradle.task;

import easypayments.gradle.model.SpigotBuildInfo;
import easypayments.gradle.util.Downloader;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;

public abstract class FetchBuildInfoTask extends DefaultTask {

    private static final String URL_PATTERN = "https://hub.spigotmc.org/versions/%s.json";

    @TaskAction
    public void run() throws IOException {
        getBuildInfo().set(fetchBuildInfo(getGameVersion().get()));
    }

    public static SpigotBuildInfo fetchBuildInfo(String gameVersion) throws IOException {
        return Downloader.readAsJson(URL_PATTERN.formatted(gameVersion), SpigotBuildInfo.class);
    }

    @Input
    public abstract Property<String> getGameVersion();

    @Internal
    public abstract Property<SpigotBuildInfo> getBuildInfo();

}
