package ru.easydonate.easypayments.gradle.spigotmapper.util

import org.gradle.api.Project
import ru.easydonate.easypayments.gradle.spigotmapper.model.MinecraftVersion
import java.util.function.BooleanSupplier

class SpigotBuildSetup(project: Project) {

    private val providers = project.providers

    private val buildPlatformSpigotInternals = providers.systemProperty("buildPlatformSpigotInternals")
        .map { it.toBooleanStrictOrNull() }
        .getOrElse(false)

    private val buildSpigotRawSpec = providers.gradleProperty("buildSpigot")
        .map { it.lowercase().split(',') }
        .getOrElse(listOf(SPIGOT_ONLY_INSTALLED))

    private val buildSpigotAll = buildSpigotRawSpec.contains(SPIGOT_ALL)
    private val buildSpigotOnlyInstalled = buildSpigotRawSpec.contains(SPIGOT_ONLY_INSTALLED)
    private val buildSpigotVersions = buildSpigotRawSpec.mapNotNull { parseVersion(it) }

    fun shouldBuildSpigotPlatform() = buildPlatformSpigotInternals

    fun shouldMapSpigot(version: MinecraftVersion, isInstalledSupplier: BooleanSupplier): Boolean {
        if (!shouldBuildSpigotPlatform()) return false
        if (buildSpigotAll || buildSpigotVersions.contains(version)) return true
        return buildSpigotOnlyInstalled && isInstalledSupplier.asBoolean
    }

    private fun parseVersion(version: String): MinecraftVersion? {
        if (version == SPIGOT_ALL || version == SPIGOT_ONLY_INSTALLED)
            return null

        return MinecraftVersion.parse(version)
    }

    companion object {
        private const val SPIGOT_ALL = "all"
        private const val SPIGOT_ONLY_INSTALLED = "only-installed"
    }

}