pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "EasyPayments"

// --- project modules

// platform-independent logic
include(":core")

// complete plugin JAR
include(":plugin")