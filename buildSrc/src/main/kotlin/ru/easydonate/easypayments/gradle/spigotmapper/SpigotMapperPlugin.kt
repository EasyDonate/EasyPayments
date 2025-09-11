package ru.easydonate.easypayments.gradle.spigotmapper

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.language.jvm.tasks.ProcessResources
import ru.easydonate.easypayments.gradle.spigotmapper.extension.SpigotMapperExtension
import ru.easydonate.easypayments.gradle.spigotmapper.model.MappingsName
import ru.easydonate.easypayments.gradle.spigotmapper.util.SpigotBuildSetup
import ru.easydonate.easypayments.gradle.spigotmapper.util.discoveryMappingsNames
import ru.easydonate.easypayments.gradle.spigotmapper.util.registerRemapJarTasks

class SpigotMapperPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = createExtension(project)
        val buildSetup = SpigotBuildSetup(project)

        if (buildSetup.shouldBuildSpigotPlatform()) {
            configureTasks(project)

            val mappingsNames = discoveryMappingsNames(project)
            project.afterEvaluate { afterEvaluate(project, extension, buildSetup, mappingsNames) }
        } else {
            disableTasks(project)
        }
    }

    private fun afterEvaluate(
        project: Project,
        extension: SpigotMapperExtension,
        buildSetup: SpigotBuildSetup,
        mappingsNames: List<MappingsName>
    ) {
        val remapJarsAggregatingTask = project.tasks.register("remapJars")
        project.tasks.named("build").configure { dependsOn(remapJarsAggregatingTask) }

        val remapJarTasks = registerRemapJarTasks(project, extension, buildSetup, mappingsNames)
        remapJarTasks.forEach { remapJarsAggregatingTask.configure { dependsOn(it) } }

        project.rootProject.project(":plugin").tasks.named("shadowJar", ShadowJar::class.java) {
            remapJarTasks.forEach { from(project.zipTree(it.outputFile)) }
            dependsOn(remapJarsAggregatingTask)
        }
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

    private fun disableTasks(project: Project) {
        project.plugins.withId("java-library") {
            project.tasks.forEach { it.enabled = false }
        }
    }

    private fun createExtension(project: Project): SpigotMapperExtension {
        return project.extensions.create("spigotMapper", SpigotMapperExtension::class.java)
    }

}