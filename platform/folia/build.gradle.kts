import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "1.7.1"
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(17)
}

paperweight {
    reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION
}

dependencies {
    compileOnly(project(":core")) { exclude(group = "org.spigotmc") }
    paperweight.foliaDevBundle(libs.versions.foliaDevBundle)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}