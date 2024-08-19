import java.util.*

plugins {
    base
    buildtools
}

group = "ru.easydonate.easypayments"
version = "2.3.0"
description = "The official payment processing implementation as alternative for the RCON protocol"

// provide group and version all subprojects
subprojects {
    group = rootProject.group
    version = rootProject.version

    val props by extra(loadProperties(project))
}

fun loadProperties(project: Project): Properties {
    val file = project.file("build.properties")
    val props = Properties()

    if (file.isFile()) {
        props.apply {
            load(file.reader())
        }
    }

    return props
}