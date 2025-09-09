import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

plugins {
    `java-library`
    alias(libs.plugins.paperweight.plugin)
}

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