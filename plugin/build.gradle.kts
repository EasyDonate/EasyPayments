import easypayments.gradle.model.Platform
import java.util.*

plugins {
    java
    id("easypayments")
    id("io.github.goooler.shadow")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(8)
}

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        content {
            includeGroup("org.spigotmc")
            includeGroup("org.bukkit")
        }
    }
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    mavenCentral()
}

dependencies {
    implementation(project(":core"))

    implementation(libs.ormliteJdbc)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}

// configure JAR assembly
tasks.jar {
    // TODO manifest
}

// configure shading
tasks.shadowJar {
    archiveClassifier = null
    destinationDirectory = rootProject.layout.buildDirectory
    includeEmptyDirs = false

    relocate("com.google.gson", "ru.easydonate.easypayments.libs.gson")
    relocate("com.j256.ormlite", "ru.easydonate.easypayments.libs.ormlite")
    relocate("ru.easydonate.easydonate4j", "ru.easydonate.easypayments.libs.easydonate4j")
    relocate("org.intellij", "ru.easydonate.easypayments.libs.intellij")
    relocate("org.jetbrains", "ru.easydonate.easypayments.libs.jetbrains")

    val platform: Platform = rootProject.extra["platform"] as Platform
    platform.forEachInternal {
        val nmsSpec = it.nmsSpec()
        val platformProject = project(":internals:spigot-nms:v${it.schemaVersion}")

        from(platformProject.layout.buildDirectory.get().dir("libs").file("${nmsSpec}.jar"))
        dependsOn(platformProject.tasks.named(if (it.usesRemappedSpigot) "${nmsSpec}-remapReobfJar" else "${nmsSpec}-remapBaseJar"))
    }

    doLast {
        copy {
            from(destinationDirectory) {
                include(archiveFileName.get())
            }

            into(file("/opt/servers/Rolling Spigot/plugins/"))
        }
    }
}

// configure resources filtering
tasks.processResources {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val replacements = mapOf("copyright_years" to "2020-$currentYear") + rootProject.properties

    filesMatching(listOf("plugin.yml", "assets/templates/**/*.yml")) {
        expand(replacements)
    }
}

// shade after build
tasks.build {
    finalizedBy(tasks.shadowJar)
}