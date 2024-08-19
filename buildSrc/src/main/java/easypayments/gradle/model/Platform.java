package easypayments.gradle.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class Platform {

    private final List<Internals> internals;

    private String mappingsName;
    private int schemaVersion;
    private boolean useRemappedSpigot;

    public Platform() {
        this("1_8_R1", 1);
    }

    public Platform(String mappingsName, int schemaVersion) {
        this.internals = new ArrayList<>();
        this.mappingsName = mappingsName;
        this.schemaVersion = schemaVersion;
    }

    public Platform add(Internals internals) {
        this.internals.add(internals);
        return this;
    }

    public Platform add(String gameVersion, int nmsRevision) {
        return add(new Internals(gameVersion, nmsRevision, mappingsName, schemaVersion, useRemappedSpigot));
    }

    public Platform useMappingsName(String mappingsName) {
        this.mappingsName = mappingsName;
        return this;
    }

    public Platform useSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
        return this;
    }

    public Platform useRemappedSpigot() {
        this.useRemappedSpigot = true;
        return this;
    }

    public void forEachInternal(Consumer<Internals> consumer) {
        this.internals.forEach(consumer);
    }

    public void forEachInternal(int schemaVersion, Consumer<Internals> consumer) {
        this.internals.stream().filter(internal -> internal.schemaVersion() == schemaVersion).forEach(consumer);
    }

}
