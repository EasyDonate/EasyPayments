import java.util.*

plugins {
    `java-library`
    alias(libs.plugins.shadow.plugin)
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
    implementation(projects.core)

    implementation(libs.ormlite.jdbc)

    compileOnly(libs.spigot.api) {
        exclude(module = "gson")
    }
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

    dependencies {
        exclude(dependency("org.jetbrains:annotations"))
    }

    lookupBuiltPlatformModules().forEach {
        from(it.tasks.jar.map(Jar::getArchiveFile).map(::zipTree))
        dependsOn(it.tasks.jar)
    }

    relocate("com.google.gson", "ru.easydonate.easypayments.libs.gson")
    relocate("com.j256.ormlite", "ru.easydonate.easypayments.libs.ormlite")
    relocate("ru.easydonate.easydonate4j", "ru.easydonate.easypayments.libs.easydonate4j")
}

// configure resources filtering
tasks.processResources {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val buildFolia = System.getProperty("easypayments.build.folia")?.toBooleanStrictOrNull() ?: true

    val replacements = mapOf(
        "copyright_years" to "2020-$currentYear",
        "folia_supported" to buildFolia.toString()
    )

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

private fun lookupBuiltPlatformModules(): List<Project> = buildList {
    val moduleMappings = mapOf(
        "folia" to projects.platform.folia,
        "platform.paper-internals" to projects.platform.paper.internals,
        "platform.paper-universal" to projects.platform.paper.universal,
    )

    moduleMappings.forEach { (propertyKey, moduleProject) ->
        if (System.getProperty("easypayments.build.$propertyKey")?.toBooleanStrictOrNull() ?: true) {
            add(project(moduleProject.path))
        }
    }
}