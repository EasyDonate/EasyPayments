package ru.easydonate.easypayments.gradle.spigotmapper.util

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.kotlin.dsl.register
import ru.easydonate.easypayments.gradle.spigotmapper.extension.SpigotMapperExtension
import ru.easydonate.easypayments.gradle.spigotmapper.model.MappingsName
import ru.easydonate.easypayments.gradle.spigotmapper.model.SpigotMapping
import ru.easydonate.easypayments.gradle.spigotmapper.task.SpecialSourceTask
import java.io.File

private const val TASK_GROUP_NAME = "spigotmapper";

fun registerRemapJarTasks(
    project: Project,
    extension: SpigotMapperExtension,
    buildSetup: SpigotBuildSetup,
    mappingsNames: List<MappingsName>
): List<SpecialSourceTask> {
    return createMappings(mappingsNames, extension.minecraftVersionsCollector).mapNotNull { spigotMapping ->
        val environment = RemapperEnvironment(project, spigotMapping)
        if (buildSetup.shouldMapSpigot(spigotMapping.minecraftVersion) { isSpigotInstalled(environment) }) {
            val copyMappingsTask = registerCopyMappingsTask(environment)
            val remapBaseJarTask = registerRemapBaseJarTask(environment, copyMappingsTask)

            if (environment.usesRemappedSpigot) {
                val remapMojangJarTask = registerRemapMojangJarTask(environment, remapBaseJarTask)
                val remapReobfJarTask = registerRemapReobfJarTask(environment, remapMojangJarTask)
                remapReobfJarTask.get()
            } else {
                remapBaseJarTask.get()
            }
        } else {
            null
        }
    }
}

private fun registerCopyMappingsTask(env: RemapperEnvironment): TaskProvider<Copy> {
    return env.tasks.register<Copy>("copyMappings@${env.nmsNotation}") {
        group = TASK_GROUP_NAME

        from(env.mainSourceSet.resources.sourceDirectories.singleFile.resolve("mappings")) {
            include(env.mapping.mappingsName.fileName())
            rename { "mappings.csrg" }
        }

        into(env.nmsBuildDir)
        expand(mapOf("nms" to env.nmsNotation))
    }
}

private fun registerRemapBaseJarTask(
    env: RemapperEnvironment,
    copyMappingsTask: TaskProvider<Copy>
): TaskProvider<SpecialSourceTask> {
    val remappedJarFileName = if (env.usesRemappedSpigot) "remapped-mojang.jar" else "remapped.jar"
    return registerSpecialSourceTask(env, "remapBaseJar", copyMappingsTask) {
        classpath(env.artifactFile(SpigotArtifactType.SPIGOT_JAR))
        inputFile.set(env.defaultJarTask.flatMap { it.archiveFile })
        mappingsFile.set(env.nmsBuildDir.file("mappings.csrg"))
        outputFile.set(env.nmsBuildDir.file(remappedJarFileName))
        dependsOn(env.defaultJarTask)
    }
}

private fun registerRemapMojangJarTask(
    env: RemapperEnvironment,
    remapBaseJarTask: TaskProvider<SpecialSourceTask>
): TaskProvider<SpecialSourceTask> {
    return registerSpecialSourceTask(env, "remapMojangJar", remapBaseJarTask) {
        classpath(env.artifactFile(SpigotArtifactType.SPIGOT_REMAPPED_MOJANG_JAR))
        inputFile.set(remapBaseJarTask.flatMap { it.outputFile })
        mappingsFile.set(env.artifactFile(SpigotArtifactType.MAPPINGS_MOJANG))
        outputFile.set(env.nmsBuildDir.file("remapped-reobf.jar"))
        reverseFlag.set(true)
    }
}

private fun registerRemapReobfJarTask(
    env: RemapperEnvironment,
    remapReobfJarTask: TaskProvider<SpecialSourceTask>
): TaskProvider<SpecialSourceTask> {
    return registerSpecialSourceTask(env, "remapReobfJar", remapReobfJarTask) {
        classpath(env.artifactFile(SpigotArtifactType.SPIGOT_REMAPPED_OBF_JAR))
        inputFile.set(remapReobfJarTask.flatMap { it.outputFile })
        mappingsFile.set(env.artifactFile(SpigotArtifactType.MAPPINGS_SPIGOT))
        outputFile.set(env.nmsBuildDir.file("remapped.jar"))
    }
}

private fun registerSpecialSourceTask(
    env: RemapperEnvironment,
    taskName: String,
    previousTask: Provider<out Task>,
    action: Action<SpecialSourceTask>
): TaskProvider<SpecialSourceTask> {
    return env.tasks.register<SpecialSourceTask>("$taskName@${env.nmsNotation}") {
        group = TASK_GROUP_NAME

        classpath(env.specialSourceJar)
        javaLauncher.set(env.defaultJavaLauncher)
        dependsOn(previousTask)
        action.execute(this)
    }
}

private fun isSpigotInstalled(env: RemapperEnvironment): Boolean {
    val artifactTypesToCheck = buildList {
        add(SpigotArtifactType.SPIGOT_JAR)
        if (env.usesRemappedSpigot) {
            addAll(arrayOf(SpigotArtifactType.MAPPINGS_MOJANG, SpigotArtifactType.MAPPINGS_SPIGOT))
            addAll(arrayOf(SpigotArtifactType.SPIGOT_REMAPPED_MOJANG_JAR, SpigotArtifactType.SPIGOT_REMAPPED_OBF_JAR))
        }
    }

    return artifactTypesToCheck.all(env::artifactPresent)
}

private data class RemapperEnvironment(
    val project: Project,
    val mapping: SpigotMapping
) {

    val defaultJarTask by lazy { tasks.named("jar", Jar::class.java) }
    val defaultJavaLauncher by lazy { javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(8)) } }
    val javaToolchains by lazy { project.extensions.getByType(JavaToolchainService::class.java) }
    val mainSourceSet by lazy { sourceSets.named("main").get() }
    val nmsBuildDir by lazy { nmsBuildRoot.dir(nmsNotation) }
    val nmsBuildRoot by lazy { project.layout.buildDirectory.dir("nms").get() }
    val nmsNotation by lazy { mapping.nmsNotation() }
    val sourceSets by lazy { project.extensions.getByType(SourceSetContainer::class.java) }
    val specialSourceJar by lazy { resolveSpecialSourceArtifact(project).file }
    val tasks by lazy { project.tasks }
    val usesRemappedSpigot by lazy { mapping.usesRemappedSpigot() }

    fun artifactPresent(artifactType: SpigotArtifactType): Boolean {
        val version = artifactVersion(artifactType)
        val artifactFile = resolveArtifactFile(project, artifactType, version)
        return artifactFile.exists() && artifactFile.isFile && artifactFile.length() > 0L
    }

    fun artifactFile(artifactType: SpigotArtifactType): File {
        val version = artifactVersion(artifactType)
        return resolveSpigotArtifact(project, artifactType, version).file
    }

    private fun artifactVersion(artifactType: SpigotArtifactType): String {
        var version = mapping.minecraftVersion.toString()
        if (usesRemappedSpigot || !artifactType.isMinecraftServer()) version += "-R0.1"
        return "$version-SNAPSHOT"
    }

}