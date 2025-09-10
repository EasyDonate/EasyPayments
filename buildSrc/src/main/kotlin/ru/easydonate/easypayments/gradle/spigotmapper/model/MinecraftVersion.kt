package ru.easydonate.easypayments.gradle.spigotmapper.model

import org.gradle.api.GradleException

data class MinecraftVersion(
    val major: Int,
    val minor: Int,
) : Comparable<MinecraftVersion> {

    companion object {

        fun parse(version: String): MinecraftVersion {
            val numbers = version.split(".", limit = 3).map { it.toInt() }
            if (numbers.size < 2)
                throw GradleException("invalid minecraft version: '$version'")

            val major = numbers[1]
            val minor = if (numbers.size > 2) numbers[2] else 0
            return MinecraftVersion(major, minor)
        }

    }

    override fun compareTo(other: MinecraftVersion) = when {
        this.major != other.major -> this.major.compareTo(other.major)
        else -> this.minor.compareTo(other.minor)
    }

    override fun toString() = if (minor == 0) "1.$major" else "1.$major.$minor"

}
