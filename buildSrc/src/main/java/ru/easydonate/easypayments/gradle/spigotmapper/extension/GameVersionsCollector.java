package ru.easydonate.easypayments.gradle.spigotmapper.extension;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@Getter
public final class GameVersionsCollector {

    private final @NotNull Map<String, Set<String>> gameVersions;

    GameVersionsCollector() {
        this.gameVersions = new LinkedHashMap<>();
    }

    public synchronized void include(@NotNull String gameVersion) {
        var nmsVersion = resolveNmsVersion(gameVersion);
        this.gameVersions.computeIfAbsent(nmsVersion, key -> new LinkedHashSet<>()).add(gameVersion);
    }

    public void include(@NotNull String @NotNull... gameVersions) {
        Stream.of(gameVersions).forEach(this::include);
    }

    private static @NotNull String resolveNmsVersion(@NotNull String gameVersion) {
        int firstDotIndex = gameVersion.indexOf('.');
        if (firstDotIndex == -1)
            throw new IllegalArgumentException("invalid game version: '%s'".formatted(gameVersion));

        int secondDotIndex = gameVersion.indexOf('.', firstDotIndex + 1);
        var majorVersion = secondDotIndex > 0 ? gameVersion.substring(0, secondDotIndex) : gameVersion;
        return majorVersion.replace('.', '_');
    }

}
