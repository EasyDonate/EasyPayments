plugins {
    `java-library`
    `spigot-internals`
    spigotmapper
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

spigotMapper {
    minecraftVersions {
        include("1.21.11")
    }
}

dependencies {
    compileOnly(projects.core)

    val brigadierVersion = "1.3.10"
    val spigotVersion = "1.21.11-R0.1-SNAPSHOT"

    compileOnly(libs.brigadier) { version { strictly(brigadierVersion) } }
    compileOnly(libs.spigot.api) { version { strictly(spigotVersion) } }
    compileOnly(variantOf(libs.spigot.full) { classifier("remapped-mojang") } ) { version { strictly(spigotVersion) } }
}