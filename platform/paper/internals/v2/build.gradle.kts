plugins {
    `paper-internals`
}

dependencies {
    compileOnly(projects.core)

    val paperDevVersion = "1.21.11-R0.1-SNAPSHOT"

    paperweight.paperDevBundle(paperDevVersion)
}