package ru.easydonate.easypayments.gradle.spigotmapper.util

import org.gradle.api.Project
import ru.easydonate.easypayments.gradle.spigotmapper.extension.MinecraftVersionsCollector
import ru.easydonate.easypayments.gradle.spigotmapper.model.MappingsName
import ru.easydonate.easypayments.gradle.spigotmapper.model.MinecraftVersion
import ru.easydonate.easypayments.gradle.spigotmapper.model.SpigotMapping
import java.nio.file.Files
import java.nio.file.Path

fun createMappings(
    mappingsNames: List<MappingsName>,
    versionsCollector: MinecraftVersionsCollector
): List<SpigotMapping> = createMappings(mappingsNames, versionsCollector.versions())

fun createMappings(
    mappingsNames: List<MappingsName>,
    versions: Map<Int, Set<MinecraftVersion>>
): List<SpigotMapping> {
    if (versions.isEmpty())
        return emptyList()

    return versions.keys.sorted().flatMap { majorVersion ->
        var nmsRevision = 1
        versions[majorVersion]?.map { minecraftVersion ->
            val currentMappingName = MappingsName(minecraftVersion.major, nmsRevision)
            val nearestMappingName = findNearestMappingName(mappingsNames, currentMappingName)
            SpigotMapping(minecraftVersion, nmsRevision++, nearestMappingName)
        } ?: emptyList()
    }
}

fun discoveryMappingsNames(project: Project): List<MappingsName> {
    val mappingsDirPath = project.layout.projectDirectory.dir("src/main/resources/mappings").asFile.toPath()
    return Files.newDirectoryStream(mappingsDirPath, "*.csrg").use { stream ->
        stream.map(Path::getFileName)
            .map(Path::toString)
            .map { it.substringBeforeLast(".csrg") }
            .map(MappingsName::parse)
    }
}

private fun findNearestMappingName(mappingsNames: List<MappingsName>, current: MappingsName): MappingsName {
    return mappingsNames
        .filter { it.majorVersion <= current.majorVersion }
        .filter { it.majorVersion < current.majorVersion || it.nmsRevision <= current.nmsRevision }
        .maxWith(Comparator.comparingInt(MappingsName::majorVersion).thenComparingInt(MappingsName::nmsRevision))
}