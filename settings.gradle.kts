pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "EasyPayments"

// -------------- PLATFORM-INDEPENDENT MODULES -------------------------------------------------------------------------

include(":core")
include(":plugin")

// -------------- OPTIONAL PLATFORM MODULES ----------------------------------------------------------------------------

// populate properties for submodules
private val buildPlatforms = System.getProperty("easypayments.build.platforms", "all").let(BuildPlatform::parse)
BuildPlatform.entries.forEach {
    System.setProperty(
        "easypayments.build.platform.${it.key}",
        buildPlatforms.contains(it).toString()
    )
}

// Folia support module
include(":platform:folia")

// Paper Internals : unrelocated CraftBukkit package
include(":platform:paper:internals")    // Paper 1.20.6 and newer

// Paper Universal : Paper API driven implementation
include(":platform:paper:universal")    // Paper 1.18.1 and newer

// Spigot Internals : relocated CraftBukkit and NMS packages
include(":platform:spigot:v1")          // Spigot 1.8  ~ 1.12.2
include(":platform:spigot:v2")          // Spigot 1.13 ~ 1.16.5
include(":platform:spigot:v3")          // Spigot 1.17 ~ 1.18.2
include(":platform:spigot:v4")          // Spigot 1.19 and newer

// -------------- INTERNAL CLASSES -------------------------------------------------------------------------------------

private enum class BuildPlatform(val key: String) {

    PAPER_INTERNALS     ("paper-internals"),
    PAPER_UNIVERSAL     ("paper-universal"),
    SPIGOT_INTERNALS    ("spigot-internals"),
    ;

    companion object {

        fun parse(input: String): Set<BuildPlatform> {
            val keys = input.split(',').map(String::lowercase)
            if (keys.contains("all"))
                return entries.toSet()

            return buildSet {
                keys.forEach { key ->
                    add(entries.firstOrNull { it.key == key } ?: throw GradleException("unknown platform: '$key'"))
                }
            }
        }

    }

}