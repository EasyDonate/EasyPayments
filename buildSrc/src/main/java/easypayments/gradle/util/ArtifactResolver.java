package easypayments.gradle.util;

import org.gradle.api.artifacts.Configuration;

import java.io.File;

public final class ArtifactResolver {

    public static File resolveSpecialSourceJarFile(Configuration configuration) {
        return configuration.getResolvedConfiguration()
                .getFirstLevelModuleDependencies().stream()
                .filter(dep -> "SpecialSource".equals(dep.getModuleName())).findFirst().orElseThrow()
                .getModuleArtifacts().stream()
                .findFirst().orElseThrow().getFile();
    }

}
