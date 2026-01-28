plugins {
    `paper-internals`
}

dependencies {
    compileOnly(projects.core)

    val paperDevVersion = "1.20.6-R0.1-SNAPSHOT"

    paperweight.paperDevBundle(paperDevVersion)
}