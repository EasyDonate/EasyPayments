plugins {
    `java-library`
    `spigot-unrelocated`
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

dependencies {
    compileOnly(projects.core)

    val brigadierVersion = "1.3.10"
    val spigotVersion = "26.1-R0.1-SNAPSHOT"

    compileOnly(libs.brigadier) { version { strictly(brigadierVersion) } }
    compileOnly(libs.spigot.full) { version { strictly(spigotVersion) } }
}