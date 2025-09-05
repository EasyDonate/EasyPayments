
import easypayments.gradle.model.Internals.DependencyTarget
import easypayments.gradle.model.Platform
import easypayments.gradle.task.SpecialSourceTask
import easypayments.gradle.util.ArtifactResolver
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

plugins {
    `java-library`
    id("spigot-base")
}

val props = project.extra["props"] as Properties
val schemaVersion = (props["schema.version"] as String).toInt()

val remapJarsTask = tasks.register("remapJars")
tasks.build.get().dependsOn(remapJarsTask)

val platform: Platform = rootProject.extra["platform"] as Platform
val localRepoPath: Path = Paths.get(repositories.mavenLocal().url.path)
val specialSourceJarFile: File = ArtifactResolver.resolveSpecialSourceJarFile(configurations["specialSource"])

platform.forEachInternal(schemaVersion) {
    if (!it.usesRemappedSpigot)
        return@forEachInternal

    val nmsSpec = it.nmsSpec()
    val buildDir = layout.buildDirectory.dir("nms/${nmsSpec}").get()

    // remap assembled base JAR
    tasks.named<SpecialSourceTask>("${it.nmsSpec()}-remapBaseJar") {
        outputJar = buildDir.file("${it.nmsSpec()}-mojang.jar")
    }

    // remap JAR: mojang -> reobf
    tasks.register<SpecialSourceTask>("${nmsSpec}-remapMojangJar") {
        classpath(listOf(specialSourceJarFile, it.dependencyFilePath(localRepoPath, DependencyTarget.SPIGOT_REMAPPED_MOJANG)))
        javaLauncher = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(8) }

        inputJar = buildDir.file("${it.nmsSpec()}-mojang.jar")
        outputJar = buildDir.file("${it.nmsSpec()}-reobf.jar")
        mappingsPath = it.dependencyFilePath(localRepoPath, DependencyTarget.MAPS_MOJANG).toFile()
        reverseFlag = true

        dependsOn("${nmsSpec}-remapBaseJar")
    }

    // remap JAR: reobf -> spigot
    tasks.register<SpecialSourceTask>("${nmsSpec}-remapReobfJar") {
        classpath(listOf(specialSourceJarFile, it.dependencyFilePath(localRepoPath, DependencyTarget.SPIGOT_REMAPPED_OBF)))
        javaLauncher = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(8) }

        inputJar = buildDir.file("${it.nmsSpec()}-reobf.jar")
        outputJar = tasks.jar.get().destinationDirectory.file("${it.nmsSpec()}.jar")
        mappingsPath = it.dependencyFilePath(localRepoPath, DependencyTarget.MAPS_SPIGOT).toFile()

        dependsOn("${nmsSpec}-remapMojangJar")
        remapJarsTask.get().dependsOn(this)
    }
}