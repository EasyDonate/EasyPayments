plugins {
    java
    alias(libs.plugins.buildtools.plugin)
}

group = "ru.easydonate.easypayments"
version = "2.5.1"

description = "The official payment processing implementation as alternative for the RCON protocol"

buildTools {
    buildToolsVersion = 193
    minecraftVersions.addAll(
        // -------------- Spigot v1 ------------------------------------------------------------------------------------
        "1.8",      "1.8.3",    "1.8.8",
        "1.9.2",    "1.9.4",
        "1.10.2",
        "1.11.2",
        "1.12.2",
        // -------------- Spigot v2 ------------------------------------------------------------------------------------
        "1.13",     "1.13.2",
        "1.14.4",
        "1.15.2",
        "1.16.1",   "1.16.3",   "1.16.5",
        // -------------- Spigot v3 ------------------------------------------------------------------------------------
        "1.17.1",
        "1.18.1",   "1.18.2",
        // -------------- Spigot v4 ------------------------------------------------------------------------------------
        "1.19",     "1.19.3",   "1.19.4",
        "1.20.1",   "1.20.2",   "1.20.4",   "1.20.6",
        "1.21.1",   "1.21.3",   "1.21.4",   "1.21.5",   "1.21.8"
    )

    expectedArtifacts {
        bootstrapJar(false)
        minecraftServerJar(false)
        spigotApiJar(false)
    }
}

subprojects {
    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenLocal()
        mavenCentral()
    }

    plugins.withType(JavaLibraryPlugin::class.java) {
        dependencies {
            add("compileOnly", libs.jetbrains.annotations)
            add("compileOnly", libs.lombok)
            add("annotationProcessor", libs.lombok)

            constraints {
                add("compileOnly", libs.jetbrains.annotations)
                add("compileOnly", libs.lombok)
                add("annotationProcessor", libs.lombok)
            }
        }
    }
}

tasks.jar {
    enabled = false
}