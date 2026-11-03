plugins {
    `paper-unrelocated`
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

dependencies {
    compileOnly(projects.core)

    val paperDevVersion = "26.3.build.238-stable"

    paperweight.paperDevBundle(paperDevVersion)
}
