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

repositories {
    mavenCentral()
}

dependencies {
    api(libs.jetbrainsAnnotations)
    api(libs.spigotApi) { exclude(module = "gson" )}

    api(libs.easydonate4jApi)
    api(libs.easydonate4jHttpClientService)
    api(libs.easydonate4jJsonSerializationService)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}