import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

plugins {
    `java-library`
    alias(libs.plugins.paperweight.plugin)
}

private val buildPlatform = providers.systemProperty("buildPlatformPaperInternals")
    .map { it.toBooleanStrictOrNull() }
    .getOrElse(true)

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

paperweight {
    reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION
}

dependencies {
    compileOnly(projects.core)

    paperweight.paperDevBundle(libs.versions.paper.dev.bundle)
}

if (!buildPlatform) {
    tasks.forEach { it.enabled = false }
}