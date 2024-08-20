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
include(":platform:folia")              // Folia implementation
include(":platform:paper:internals")    // Paper Internals (unrelocated CraftBukkit)
include(":platform:paper:universal")    // Paper Universal (via API)
include(":platform:spigot:v1")          // Spigot Internals  1.8 ~ 1.12.2
include(":platform:spigot:v2")          // Spigot Internals 1.13 ~ 1.16.5
include(":platform:spigot:v3")          // Spigot Internals 1.17 ~ 1.18.2
include(":platform:spigot:v4")          // Spigot Internals 1.19 ~ latest
include(":plugin")