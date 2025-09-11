import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

plugins {
    `java-library`
    alias(libs.plugins.paperweight.plugin)
}

private val propertyName = "easypayments.build.platform.paper-internals"
private val buildPlatform = System.getProperty(propertyName)?.toBooleanStrictOrNull() ?: true

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

paperweight {
    reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION
}

dependencies {
    compileOnly(projects.core)

    if (buildPlatform) {
        paperweight.paperDevBundle(libs.versions.paper.dev.bundle)
    }
}

if (!buildPlatform) {
    tasks.forEach { it.enabled = false }
}