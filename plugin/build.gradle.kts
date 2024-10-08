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
    manifest {
        attributes(mapOf(
            "paperweight-mappings-namespace" to "mojang+yarn",
            "Organization-Name" to "EasyDonate",
            "Organization-Website" to "https://easydonate.ru/",
            "Source-Code" to "https://github.com/EasyDonate/EasyPayments/"
        ))
    }
}

// configure shading
tasks.shadowJar {
    archiveBaseName = rootProject.name
    archiveClassifier = null
    archiveVersion = rootProject.version.toString()
    destinationDirectory = rootProject.layout.buildDirectory
    includeEmptyDirs = false
    exclude("META-INF/maven/**", "META-INF/versions/**")

    listOf("folia", "paper:internals", "paper:universal").map { project(":platform:${it}") }.forEach {
        from(it.tasks.jar.get().archiveFile)
        dependsOn(it.tasks.jar)
    }

    val platform: Platform = rootProject.extra["platform"] as Platform
    platform.forEachInternal {
        val nmsSpec = it.nmsSpec()
        val spigotPlatformProject = project(":platform:spigot:v${it.schemaVersion}")

        from(spigotPlatformProject.layout.buildDirectory.get().dir("libs").file("${nmsSpec}.jar"))
        dependsOn(spigotPlatformProject.tasks.named(if (it.usesRemappedSpigot) "${nmsSpec}-remapReobfJar" else "${nmsSpec}-remapBaseJar"))
    }

    relocate("com.google.gson", "ru.easydonate.easypayments.libs.gson")
    relocate("com.j256.ormlite", "ru.easydonate.easypayments.libs.ormlite")
    relocate("ru.easydonate.easydonate4j", "ru.easydonate.easypayments.libs.easydonate4j")
    relocate("org.intellij", "ru.easydonate.easypayments.libs.intellij")
    relocate("org.jetbrains", "ru.easydonate.easypayments.libs.jetbrains")
}

// configure resources filtering
tasks.processResources {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val replacements = mapOf("copyright_years" to "2020-$currentYear")

    inputs.properties(replacements)
    inputs.property("project.name", rootProject.name)
    inputs.property("project.description", rootProject.description)
    inputs.property("project.version", rootProject.version)

    filesMatching(listOf("plugin.yml", "assets/templates/**/*.yml")) {
        expand(replacements + rootProject.properties)
    }
}

// shade after build
tasks.build {
    finalizedBy(tasks.shadowJar)
}