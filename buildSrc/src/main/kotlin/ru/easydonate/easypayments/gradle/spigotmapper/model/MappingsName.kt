package ru.easydonate.easypayments.gradle.spigotmapper.model

import org.gradle.api.GradleException

data class MappingsName(
    val majorVersion: Int,
    val nmsRevision: Int,
) {

    companion object {

        private val regex = Regex("""^(\d+)_(\d+)_R(\d+)$""")

        fun parse(fileName: String): MappingsName {
            val match = regex.matchEntire(fileName) ?: throw GradleException("invalid mapping name: '$fileName'")
            val (_, major, revision) = match.destructured
            return MappingsName(major.toInt(), revision.toInt())
        }

    }

    fun fileName() = "${toString()}.csrg"

    override fun toString() = "1_${majorVersion}_R$nmsRevision"

}