private val buildPlatform = providers.systemProperty("buildPlatformPaperUniversal")
    .map { it.toBooleanStrictOrNull() }
    .getOrElse(true)

plugins {
    `java-library`
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(17)
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://libraries.minecraft.net/")
}

dependencies {
    compileOnly(projects.core)

    if (buildPlatform) {
        compileOnly(libs.authlib)
        compileOnly(libs.paper.api)
    }
}

if (!buildPlatform) {
    tasks.forEach { it.enabled = false }
}