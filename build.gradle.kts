plugins {
    base
}

// configure gradle wrapper
tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
    gradleVersion = "8.9"
}

group = "ru.easydonate.easypayments"
version = "2.3.0"
description = "The official payment processing implementation as alternative for the RCON protocol"

// provide group and version all subprojects
subprojects {
    group = rootProject.group
    version = rootProject.version
}