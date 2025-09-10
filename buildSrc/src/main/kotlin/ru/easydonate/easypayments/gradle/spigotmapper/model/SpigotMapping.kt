package ru.easydonate.easypayments.gradle.spigotmapper.model

data class SpigotMapping(
    val minecraftVersion: MinecraftVersion,
    val nmsRevision: Int,
    val mappingsName: MappingsName,
) {

    fun nmsNotation() = "v1_${minecraftVersion.major}_R$nmsRevision"

    fun usesRemappedSpigot() = minecraftVersion.major >= 17

}