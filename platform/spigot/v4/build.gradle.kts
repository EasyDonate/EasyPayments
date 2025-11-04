plugins {
    `java-library`
    `spigot-platform`
    spigotmapper
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(17)
}

spigotMapper {
    minecraftVersions {
        include("1.19", "1.19.3", "1.19.4")
        include("1.20.1", "1.20.2", "1.20.4", "1.20.6")
        include("1.21.1", "1.21.3", "1.21.4", "1.21.5", "1.21.8", "1.21.10")
    }
}

dependencies {
    compileOnly(projects.core)

    val brigadierVersion = "1.0.18"
    val spigotVersion = "1.19-R0.1-SNAPSHOT"

    compileOnly(libs.brigadier) { version { strictly(brigadierVersion) } }
    compileOnly(libs.spigot.api) { version { strictly(spigotVersion) } }
    compileOnly(variantOf(libs.spigot.full) { classifier("remapped-mojang") } ) { version { strictly(spigotVersion) } }
}