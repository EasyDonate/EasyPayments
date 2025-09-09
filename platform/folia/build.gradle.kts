import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

plugins {
    `java-library`
    alias(libs.plugins.paperweight.plugin)
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(17)
}

paperweight {
    reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION
}

dependencies {
    compileOnly(projects.core) {
        exclude(group = "org.spigotmc")
    }

    paperweight.foliaDevBundle(libs.versions.folia.dev.bundle)
}