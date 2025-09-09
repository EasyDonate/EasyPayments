package ru.easydonate.easypayments.gradle.spigotmapper.extension

import org.gradle.api.GradleException

class MinecraftVersionsCollector {

    companion object {

        private fun resolveNmsVersion(minecraftVersion: String): String {
            val firstDotIndex = minecraftVersion.indexOf('.')
            if (firstDotIndex == -1)
                throw GradleException("invalid minecraft version: '$minecraftVersion'")

            val secondDotIndex = minecraftVersion.indexOf('.', firstDotIndex + 1)
            val majorVersion = if (secondDotIndex > 0) minecraftVersion.take(secondDotIndex) else minecraftVersion
            return majorVersion.replace('.', '_')
        }

    }

    val minecraftVersions = mutableMapOf<String, MutableSet<String>>()

    fun include(minecraftVersion: String) {
        val nmsVersion = resolveNmsVersion(minecraftVersion)
        this.minecraftVersions.computeIfAbsent(nmsVersion) { LinkedHashSet() }.add(minecraftVersion)
    }

    fun include(vararg minecraftVersions: String) {
        minecraftVersions.forEach(this::include)
    }

}