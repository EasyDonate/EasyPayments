package ru.easydonate.easypayments.gradle.spigotmapper.util

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.internal.component.external.model.ModuleComponentArtifactIdentifier
import org.gradle.kotlin.dsl.getByName

fun resolveSpecialSourceArtifact(project: Project): ResolvedArtifact {
    val versionCatalogs = project.extensions.getByName<VersionCatalogsExtension>("versionCatalogs")
    val specialSourceDependency = versionCatalogs.named("libs").findLibrary("special-source").orElseThrow()
    val specialSourceNotation = specialSourceDependency.map(MinimalExternalModuleDependency::notation)
    return specialSourceNotation.map { resolveDependency(project, "$it:shaded") }.get()
}

fun resolveSpigotArtifact(project: Project, artifactType: SpigotArtifactType, version: String): ResolvedArtifact {
    return resolveDependency(project, artifactType.dependencyNotation(version))
}

fun resolveDependency(project: Project, dependencyNotation: Any): ResolvedArtifact {
    val dependency = project.dependencies.create(dependencyNotation)
    val configuration = project.configurations.detachedConfiguration(dependency)
    return configuration.resolvedConfiguration.resolvedArtifacts.first { it.id is ModuleComponentArtifactIdentifier }
}

fun MinimalExternalModuleDependency.notation() = "${module.group}:${module.name}${version?.let { ":$it" } ?: ""}"

enum class SpigotArtifactType(
    val artifactId: String,
    val classifier: String? = null,
    val extension: String? = null
) {

    MAPPINGS_MOJANG             ("minecraft-server",    "maps-mojang",          "txt"),
    MAPPINGS_SPIGOT             ("minecraft-server",    "maps-spigot",          "csrg"),
    SPIGOT_JAR                  ("spigot"),
    SPIGOT_REMAPPED_MOJANG_JAR  ("spigot",              "remapped-mojang"),
    SPIGOT_REMAPPED_OBF_JAR     ("spigot",              "remapped-obf"),
    ;

    fun dependencyNotation(version: String): String {
        var notation = "org.spigotmc:$artifactId:$version"
        classifier?.let { notation += ":$it" }
        extension?.let { notation += "@$it" }
        return notation
    }

    fun isMinecraftServer(): Boolean = artifactId == "minecraft-server"

    override fun toString(): String = dependencyNotation("<version>")

}