plugins {
    `java-library`
    `spigot-internals`
    spigotmapper
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(16)
}

spigotMapper {
    minecraftVersions {
        include("1.17.1")
        include("1.18.1", "1.18.2")
    }
}

dependencies {
    compileOnly(projects.core)

    val spigotVersion = "1.17.1-R0.1-SNAPSHOT"

    compileOnly(libs.spigot.api) { version { strictly(spigotVersion) } }
    compileOnly(variantOf(libs.spigot.full) { classifier("remapped-mojang") } ) { version { strictly(spigotVersion) } }
}