package ru.easydonate.easypayments.gradle.spigotmapper.extension;

import lombok.Getter;
import org.gradle.api.Action;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class SpigotMapperExtension {

    private final @NotNull GameVersionsCollector gameVersionsCollector;

    public SpigotMapperExtension() {
        this.gameVersionsCollector = new GameVersionsCollector();
    }

    public void gameVersions(@NotNull Action<GameVersionsCollector> action) {
        action.execute(gameVersionsCollector);
    }

}
