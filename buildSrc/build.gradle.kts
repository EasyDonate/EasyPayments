plugins {
    java
    `kotlin-dsl`
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.10")
    implementation("io.github.goooler.shadow:shadow-gradle-plugin:8.1.8")

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
}