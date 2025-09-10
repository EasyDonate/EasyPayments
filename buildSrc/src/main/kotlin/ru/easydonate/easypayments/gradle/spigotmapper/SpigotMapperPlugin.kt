package ru.easydonate.easypayments.gradle.spigotmapper

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.language.jvm.tasks.ProcessResources
import ru.easydonate.easypayments.gradle.spigotmapper.extension.SpigotMapperExtension
import ru.easydonate.easypayments.gradle.spigotmapper.model.MappingsName
import ru.easydonate.easypayments.gradle.spigotmapper.util.discoveryMappingsNames
import ru.easydonate.easypayments.gradle.spigotmapper.util.registerRemapJarTasks

class SpigotMapperPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = createExtension(project)
        configureTasks(project)

        val mappingsNames = discoveryMappingsNames(project)
        project.afterEvaluate { afterEvaluate(project, extension, mappingsNames) }
    }

    private fun afterEvaluate(
        project: Project,
        extension: SpigotMapperExtension,
        mappingsNames: List<MappingsName>
    ) {
        val remapJarsAggregatingTask = project.tasks.register("remapJars")
        project.tasks.named("build").configure { dependsOn(remapJarsAggregatingTask) }

        val remapJarTasks = registerRemapJarTasks(project, extension, mappingsNames)
        remapJarTasks.forEach { remapJarsAggregatingTask.configure { dependsOn(it) } }
    }

    private fun configureTasks(project: Project) {
        project.plugins.withId("java-library") {
            project.tasks.named("jar", Jar::class.java) {
                archiveFileName.set("base.jar")
            }

            project.tasks.withType(JavaCompile::class.java) {
                options.compilerArgs.add("-Xlint:-options")
            }

            project.tasks.withType(ProcessResources::class.java) {
                exclude("mappings/**")
            }
        }
    }

    private fun createExtension(project: Project): SpigotMapperExtension {
        return project.extensions.create("spigotMapper", SpigotMapperExtension::class.java)
    }

}