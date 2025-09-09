package ru.easydonate.easypayments.gradle.spigotmapper.extension

import org.gradle.api.Action

abstract class SpigotMapperExtension {

    val minecraftVersionsCollector = MinecraftVersionsCollector()

    fun minecraftVersions(action: Action<in MinecraftVersionsCollector>) {
        action.execute(minecraftVersionsCollector)
    }

}