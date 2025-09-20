plugins {
    `java-library`
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(8)
}

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        content {
            includeGroup("org.spigotmc")
            includeGroup("org.bukkit")
        }
    }
    mavenLocal {
        content {
            includeGroup("org.spigotmc")
        }
    }
    maven("https://libraries.minecraft.net/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    // fallback repository for the bungeecord-chat
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    mavenCentral()
}