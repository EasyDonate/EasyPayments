package ru.easydonate.easypayments.gradle.spigotmapper.util

import ru.easydonate.easypayments.gradle.spigotmapper.model.MinecraftVersion
import java.util.function.BooleanSupplier

private const val SPIGOT_ALL = "all"
private const val SPIGOT_ONLY_INSTALLED = "only-installed"

private val buildPlatformSpigotInternals = property("platform.spigot-internals")?.toBooleanStrictOrNull() ?: true
private val buildSpigotRawSpec = property("spigot")?.lowercase()?.split(',') ?: listOf(SPIGOT_ONLY_INSTALLED)

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

private fun property(key: String): String? {
    return System.getProperty("easypayments.build.$key")
}