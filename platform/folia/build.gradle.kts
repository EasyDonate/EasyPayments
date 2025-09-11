import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

plugins {
    `java-library`
    alias(libs.plugins.paperweight.plugin)
}

private val buildFolia = System.getProperty("easypayments.build.folia")?.toBooleanStrictOrNull() ?: true

java {
    toolchain.languageVersion = JavaLanguageVersion.of(17)
}

paperweight {
    reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION
}

dependencies {
    compileOnly(projects.core)

    if (buildFolia) {
        paperweight.foliaDevBundle(libs.versions.folia.dev.bundle)
    }
}

if (!buildFolia) {
    tasks.forEach { it.enabled = false }
}