plugins {
    `java-library`
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(17)
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://libraries.minecraft.net/")
}

dependencies {
    compileOnly(projects.core)

    compileOnly(libs.paper.api)
}