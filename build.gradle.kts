plugins {
    alias(libs.plugins.buildtools.plugin)
}

group = "ru.easydonate.easypayments"
version = "2.4.2"

description = "The official payment processing implementation as alternative for the RCON protocol"

repositories {
    mavenLocal()
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