import easypayments.gradle.model.Platform
import easypayments.gradle.task.SpecialSourceTask
import java.util.*

plugins {
    `java-library`
    id("spigot-base")
}

val props = project.extra["props"] as Properties
val schemaVersion = (props["schema.version"] as String).toInt()

val remapJarsTask = tasks.register("remapJars")
tasks.build.get().dependsOn(remapJarsTask)

val platform: Platform = rootProject.extra["platform"] as Platform
platform.forEachInternal(schemaVersion) {
    if (it.usesRemappedSpigot)
        return@forEachInternal

    // remap assembled base JAR
    tasks.named<SpecialSourceTask>("${it.nmsSpec()}-remapBaseJar") {
        outputJar = tasks.jar.get().destinationDirectory.file("${it.nmsSpec()}.jar")
        remapJarsTask.get().dependsOn(this)
    }
}

