plugins {
    java
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
    implementation(project(":core"))

    implementation(libs.ormliteJdbc)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}