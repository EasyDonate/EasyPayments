plugins {
    `java-library`
    `spigot-platform`
    spigotmapper
}

spigotMapper {
    minecraftVersions {
        include("1.13", "1.13.2")
        include("1.14.4")
        include("1.15.2")
        include("1.16.1", "1.16.3", "1.16.5")
    }
}

dependencies {
    compileOnly(projects.core)

    val spigotVersion = "1.13-R0.1-SNAPSHOT"

    compileOnly(libs.spigot.api) { version { strictly(spigotVersion) } }
    compileOnly(libs.spigot.full) { version { strictly(spigotVersion) } }
}