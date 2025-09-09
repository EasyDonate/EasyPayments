plugins {
    `java-library`
    `spigot-platform`
    spigotmapper
}

dependencies {
    compileOnly(projects.core)

    val spigotVersion = "1.13-R0.1-SNAPSHOT"

    compileOnly(libs.spigot.api) { version { strictly(spigotVersion) } }
    compileOnly(libs.spigot.full) { version { strictly(spigotVersion) } }
}