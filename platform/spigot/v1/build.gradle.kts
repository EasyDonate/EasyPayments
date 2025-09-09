plugins {
    `java-library`
    `spigot-platform`
    spigotmapper
}

spigotMapper {
    minecraftVersions {
        include("1.8", "1.8.3", "1.8.8")
        include("1.9.2", "1.9.4")
        include("1.10.2")
        include("1.11.2")
        include("1.12.2")
    }
}

dependencies {
    compileOnly(projects.core)

    val spigotVersion = "1.8-R0.1-SNAPSHOT"

    compileOnly(libs.spigot.api) { version { strictly(spigotVersion) } }
    compileOnly(libs.spigot.full) { version { strictly(spigotVersion) } }
}