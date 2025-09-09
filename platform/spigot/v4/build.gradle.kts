plugins {
    `java-library`
    `spigot-platform`
    spigotmapper
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(17)
}

dependencies {
    compileOnly(projects.core)

    val brigadierVersion = "1.0.18"
    val spigotVersion = "1.19-R0.1-SNAPSHOT"

    compileOnly(libs.brigadier) { version { strictly(brigadierVersion) } }
    compileOnly(libs.spigot.api) { version { strictly(spigotVersion) } }
    compileOnly(variantOf(libs.spigot.full) { classifier("remapped-mojang") } ) { version { strictly(spigotVersion) } }
}