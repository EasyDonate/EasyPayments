import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

plugins {
    `java-library`
    alias(libs.plugins.paperweight.plugin)
}

private val buildFolia = providers.gradleProperty("buildFolia")
    .map { it.toBooleanStrictOrNull() }
    .getOrElse(true)

java {
    toolchain.languageVersion = JavaLanguageVersion.of(17)
}

paperweight {
    reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION
}

dependencies {
    compileOnly(projects.core)

    paperweight.foliaDevBundle(libs.versions.folia.dev.bundle)
}

if (!buildFolia) {
    tasks.forEach { it.enabled = false }
}