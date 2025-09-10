package ru.easydonate.easypayments.gradle.spigotmapper.extension

import ru.easydonate.easypayments.gradle.spigotmapper.model.MinecraftVersion

class MinecraftVersionsCollector {

    private val minecraftVersions = sortedMapOf<Int, MutableSet<MinecraftVersion>>()

    fun include(version: String) {
        val minecraftVersion = MinecraftVersion.parse(version)
        val majorVersion = minecraftVersion.major
        this.minecraftVersions.computeIfAbsent(majorVersion) { sortedSetOf() }.add(minecraftVersion)
    }

    fun include(vararg versions: String) {
        versions.forEach(this::include)
    }

    fun versions() = minecraftVersions

}