plugins {
    `java-library`
}

private val propertyName = "easypayments.build.platform.paper-universal"
private val buildPlatform = System.getProperty(propertyName)?.toBooleanStrictOrNull() ?: true

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
        compileOnly(libs.paper.api)
    }
}

if (!buildPlatform) {
    tasks.forEach { it.enabled = false }
}