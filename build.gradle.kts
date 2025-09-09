plugins {
    java
    alias(libs.plugins.buildtools.plugin)
}

group = "ru.easydonate.easypayments"
version = "2.4.2"

description = "The official payment processing implementation as alternative for the RCON protocol"

buildTools {
    buildToolsVersion = 193
    minecraftVersions.addAll("1.8", "1.13", "1.17.1", "1.19")

    expectedArtifacts {
        bootstrapJar(false)
        minecraftServerJar(false)
        spigotApiJar(false)
    }
}

subprojects {
    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenLocal()
        mavenCentral()
    }

    plugins.withType(JavaLibraryPlugin::class.java) {
        dependencies {
            add("compileOnly", libs.jetbrains.annotations)
            add("compileOnly", libs.lombok)
            add("annotationProcessor", libs.lombok)

            constraints {
                add("compileOnly", libs.jetbrains.annotations)
                add("compileOnly", libs.lombok)
                add("annotationProcessor", libs.lombok)
            }
        }
    }
}