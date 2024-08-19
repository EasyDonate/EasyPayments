import easypayments.gradle.model.Internals.DependencyTarget
import easypayments.gradle.model.Platform
import easypayments.gradle.task.SpecialSourceTask
import easypayments.gradle.util.ArtifactResolver
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

plugins {
    `java-library`
}

val props = project.extra["props"] as Properties
val schemaVersion = (props["schema.version"] as String).toInt()
val javaVersion = (props["java.version"] as String).toInt()
val spigotApiVersion = props["spigot-api.version"]
val brigadierVersion = props["brigadier.version"]
val useRemappedSpigot = props["use-remapped-spigot"]

val depsConfigName = "${project.name}Deps"
configurations.create("specialSource")
configurations.create(depsConfigName)
configurations.compileClasspath {
    extendsFrom(configurations[depsConfigName])
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)
}

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        content {
            includeGroup("org.spigotmc")
            includeGroup("org.bukkit")
        }
    }
    mavenLocal {
        content {
            includeGroup("org.spigotmc")
        }
    }
    maven("https://libraries.minecraft.net/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    mavenCentral()
}

dependencies {
    compileOnly(project(":core")) { exclude(group = "org.spigotmc") }

    if (spigotApiVersion != null) {
        val spigotDepTarget = when {
            useRemappedSpigot == null || !(useRemappedSpigot as String).toBoolean() -> DependencyTarget.SPIGOT
            else -> DependencyTarget.SPIGOT_REMAPPED_MOJANG
        }

        add(depsConfigName, spigotDepTarget.getDependencyNotation("${spigotApiVersion}-R0.1-SNAPSHOT"))
        add(depsConfigName, DependencyTarget.SPIGOT_API.getDependencyNotation("${spigotApiVersion}-R0.1-SNAPSHOT"))
    }

    if (brigadierVersion != null) {
        add(depsConfigName, "com.mojang:brigadier:${brigadierVersion}")
    }

    add("specialSource", "net.md-5:SpecialSource:1.11.4:shaded")

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
}

val platform: Platform = rootProject.extra["platform"] as Platform
val localRepoPath: Path = Paths.get(repositories.mavenLocal().url.path)
val specialSourceJarFile: File = ArtifactResolver.resolveSpecialSourceJarFile(configurations["specialSource"])

platform.forEachInternal(schemaVersion) {
    val nmsSpec = it.nmsSpec()
    val buildDir = layout.buildDirectory.dir("nms/${nmsSpec}").get()

    // generate mappings in the build directory
    tasks.create("${nmsSpec}-copyMappings") {
        doFirst {
            copy {
                from(sourceSets.main.get().resources.sourceDirectories.singleFile.resolve("mappings")) {
                    include("${it.mappingsName}.csrg")
                    rename { "mappings.csrg" }
                }

                into(buildDir)
                expand(mapOf("nms" to nmsSpec))
            }
        }
    }

    // remap assembled base JAR
    tasks.create<SpecialSourceTask>("${nmsSpec}-remapBaseJar") {
        classpath(listOf(specialSourceJarFile, it.dependencyFilePath(localRepoPath, DependencyTarget.SPIGOT)))
        javaLauncher = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(8) }

        inputJar = tasks.jar.get().archiveFile
        mappingsPath = buildDir.file("mappings.csrg")

        dependsOn(tasks.jar)
        dependsOn("${nmsSpec}-copyMappings")
    }
}

// change default assemble JAR file name
tasks.jar {
    archiveFileName = "base.jar"
}

// configure all java compile tasks
tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xlint:-options")
    sourceCompatibility = javaVersion.toString()
    targetCompatibility = javaVersion.toString()
}

// disable default resources processing
tasks.withType<ProcessResources> {
    enabled = false
}