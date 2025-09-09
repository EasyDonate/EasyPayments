package ru.easydonate.easypayments.gradle.spigotmapper

import org.gradle.api.Plugin
import org.gradle.api.Project
import ru.easydonate.easypayments.gradle.spigotmapper.extension.SpigotMapperExtension

class SpigotMapperPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = createExtension(project)
        // TODO code
    }

    private fun createExtension(project: Project): SpigotMapperExtension {
        return project.extensions.create("spigotMapper", SpigotMapperExtension::class.java)
    }

}