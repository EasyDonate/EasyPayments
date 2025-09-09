plugins {
    `kotlin-dsl`
}

repositories {
    mavenLocal()
    gradlePluginPortal()
}

dependencies {
    versionCatalogs.named("libs").let { libs ->
        implementation(libs.findLibrary("buildtools-plugin").orElseThrow())
        implementation(libs.findLibrary("paperweight-plugin").orElseThrow())
        implementation(libs.findLibrary("shadow-plugin").orElseThrow())
    }
}