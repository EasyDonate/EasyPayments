package easypayments.gradle.model;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.stream.IntStream;

@Getter
public final class SpigotBuildInfo {

    public static final int DEFAULT_JAVA_VERSION = 52; // Java 8

    @SerializedName("toolsVersion")
    private int buildToolsVersion;

    @SerializedName("javaVersions")
    private int[] supportedJavaVersions;

    public int getRequiredJavaVersion() {
        return supportedJavaVersions != null
                ? IntStream.of(supportedJavaVersions).min().orElse(DEFAULT_JAVA_VERSION)
                : DEFAULT_JAVA_VERSION;
    }

}
