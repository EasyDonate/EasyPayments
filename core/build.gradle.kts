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
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    mavenCentral()
}

dependencies {
    api(libs.easydonate4j.api)
    api(libs.easydonate4j.jdk.legacy.http.client)
    api(libs.easydonate4j.gson.json.provider)

    compileOnly(libs.spigot.api) {
        exclude(module = "gson")
    }
}