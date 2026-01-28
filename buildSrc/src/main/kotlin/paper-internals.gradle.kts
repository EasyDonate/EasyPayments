import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

private val buildPlatform = providers.systemProperty("buildPlatformPaperInternals")
    .map { it.toBooleanStrictOrNull() }
    .getOrElse(true)

plugins {
    `java-library`
    id("io.papermc.paperweight.userdev")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

paperweight {
    reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION
}

if (!buildPlatform) {
    tasks.forEach { it.enabled = false }
}