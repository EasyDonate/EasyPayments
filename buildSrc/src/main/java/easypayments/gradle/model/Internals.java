package easypayments.gradle.model;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;

@Slf4j
public record Internals(
        String gameVersion,
        int nmsRevision,
        String mappingsName,
        int schemaVersion,
        boolean usesRemappedSpigot
) {

    public String nmsSpec() {
        int dotIndex = gameVersion.indexOf('.');
        if (dotIndex == -1)
            throw new IllegalArgumentException("Invalid game version: " + gameVersion);

        dotIndex = gameVersion.indexOf('.', dotIndex + 1);
        String majorVersion = dotIndex == -1 ? gameVersion : gameVersion.substring(0, dotIndex);
        return 'v' + majorVersion.replace('.', '_') + "_R" + nmsRevision;
    }

    public String craftBukkitVersion() {
        return gameVersion + "-R0.1-SNAPSHOT";
    }

    public String dependencyNotation(DependencyTarget target) {
        return dependencyNotation(target, craftBukkitVersion());
    }

    public String dependencyNotation(DependencyTarget target, String version) {
        return target.getDependencyNotation(version);
    }

    public Path dependencyFilePath(Path baseDir, DependencyTarget target) {
        return dependencyFilePath(baseDir, target, craftBukkitVersion());
    }

    public Path dependencyFilePath(Path baseDir, DependencyTarget target, String version) {
        return target.getDependencyFilePath(baseDir, version);
    }

    public boolean areAllBuildArtifactsPresent(Path baseDir) {
        if (!isBuildArtifactPresent(baseDir, DependencyTarget.SPIGOT))
            return false;

        if (!usesRemappedSpigot)
            return true;

        return isBuildArtifactPresent(baseDir, DependencyTarget.SPIGOT_REMAPPED_MOJANG)
                && isBuildArtifactPresent(baseDir, DependencyTarget.SPIGOT_REMAPPED_OBF)
                && isBuildArtifactPresent(baseDir, DependencyTarget.MAPS_MOJANG)
                && isBuildArtifactPresent(baseDir, DependencyTarget.MAPS_SPIGOT);
    }

    private boolean isBuildArtifactPresent(Path baseDir, DependencyTarget target) {
        if (Files.isRegularFile(dependencyFilePath(baseDir, target)))
            return true;

        log.info("Build artifact '{}' not found in the local repository!", dependencyNotation(target));
        return false;
    }

    @AllArgsConstructor
    public enum DependencyTarget {

        SPIGOT_API              ("spigot-api"),
        SPIGOT                  ("spigot"),
        SPIGOT_REMAPPED_MOJANG  ("spigot", "remapped-mojang"),
        SPIGOT_REMAPPED_OBF     ("spigot", "remapped-obf"),
        MAPS_MOJANG             ("minecraft-server", "maps-mojang", "txt"),
        MAPS_SPIGOT             ("minecraft-server", "maps-spigot", "csrg"),
        MAPS_SPIGOT_MEMBERS     ("minecraft-server", "maps-spigot-members", "csrg"),
        ;

        private final String artifactId;
        private final String classifier;
        private final String extension;

        DependencyTarget(String artifactId) {
            this(artifactId, null, "jar");
        }

        DependencyTarget(String artifactId, String classifier) {
            this(artifactId, classifier, "jar");
        }

        public String getDependencyNotation(String version) {
            return MessageFormat.format(
                    (classifier != null ? "org.spigotmc:{0}:{1}:{2}@{3}" : "org.spigotmc:{0}:{1}@{3}"),
                    artifactId, version, classifier, extension
            );
        }

        public Path getDependencyFilePath(Path baseDir, String version) {
            String fileName = MessageFormat.format(
                    (classifier != null ? "{0}-{1}-{2}.{3}" : "{0}-{1}.{3}"),
                    artifactId, version, classifier, extension
            );

            return baseDir.resolve("org").resolve("spigotmc").resolve(artifactId).resolve(version).resolve(fileName);
        }

    }

}
