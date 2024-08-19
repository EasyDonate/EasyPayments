pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "EasyPayments"

// --- project modules
include(":core")
include(":internals:spigot-nms:v1")
include(":internals:spigot-nms:v2")
include(":internals:spigot-nms:v3")
include(":internals:spigot-nms:v4")
include(":plugin")