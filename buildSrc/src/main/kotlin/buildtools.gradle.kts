import easypayments.gradle.model.Platform
import easypayments.gradle.task.FetchBuildInfoTask
import easypayments.gradle.task.FetchBuildToolsTask
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

plugins {
    java
}

val platform: Platform by extra(declarePlatformImplementations())
val localRepoPath: Path = Paths.get(repositories.mavenLocal().url.path)
val globalCacheDir: Path by extra(gradle.gradleUserHomeDir.toPath().resolve("caches").resolve("easypayments"))
val buildToolsCacheDir: Path by extra(globalCacheDir.resolve("build-tools"))
val buildToolsJarPath: Path by extra(buildToolsCacheDir.resolve("BuildTools.jar"))

val buildSpigotJarsTask = tasks.create("buildSpigotJars")

tasks.create<FetchBuildToolsTask>("fetchBuildTools") {
    jarPath = buildToolsJarPath
}

tasks.create("setupSpigotJars") {
    dependsOn("fetchBuildTools")

    platform.forEachInternal {
        if (it.areAllBuildArtifactsPresent(localRepoPath))
            return@forEachInternal

        val buildInfo = FetchBuildInfoTask.fetchBuildInfo(it.gameVersion)
        val requiredJavaVersion = JavaVersion.forClassVersion(buildInfo.requiredJavaVersion).majorVersion

        val versionBuildDir = buildToolsCacheDir.resolve(it.gameVersion)
        if (!Files.isDirectory(versionBuildDir))
            Files.createDirectories(versionBuildDir)

        tasks.create<JavaExec>("buildSpigotJars-${it.gameVersion}") {
            val args = ArrayList<String>()
            args.add("--rev")
            args.add(it.gameVersion)
            args.add("--nogui")

            if (it.usesRemappedSpigot)
                args.add("--remapped")

            args(args)
            classpath = files(buildToolsJarPath)
            javaLauncher = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(requiredJavaVersion) }
            logging.captureStandardOutput(LogLevel.DEBUG)
            logging.captureStandardError(LogLevel.DEBUG)
            workingDir = versionBuildDir.toFile()

            buildSpigotJarsTask.dependsOn(this)
        }
    }

    finalizedBy(buildSpigotJarsTask)
}

fun declarePlatformImplementations(): Platform {
    return Platform()
        .add("1.8", 1)
        .useMappingsName("1_8_R2")
        .add("1.8.3", 2)
        .add("1.8.8", 3)
        .useMappingsName("1_9_R1")
        .add("1.9.2", 1)
        .add("1.9.4", 2)
        .add("1.10.2", 1)
        .add("1.11.2", 1)
        .add("1.12.2", 1)
        .useSchemaVersion(2)
        .useMappingsName("1_13_R1")
        .add("1.13", 1)
        .add("1.13.2", 2)
        .useMappingsName("1_14_R1")
        .add("1.14.4", 1)
        .add("1.15.2", 1)
        .add("1.16.1", 1)
        .add("1.16.3", 2)
        .add("1.16.5", 3)
        .useRemappedSpigot()
        .useSchemaVersion(3)
        .useMappingsName("1_17_R1")
        .add("1.17.1", 1)
        .add("1.18.1", 1)
        .add("1.18.2", 2)
        .useSchemaVersion(4)
        .useMappingsName("1_19_R1")
        .add("1.19", 1)
        .add("1.19.3", 2)
        .add("1.19.4", 3)
        .add("1.20.1", 1)
        .add("1.20.2", 2)
        .add("1.20.4", 3)
        .add("1.20.6", 4)
        .add("1.21", 1)
}
