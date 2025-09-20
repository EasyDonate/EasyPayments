package ru.easydonate.easypayments.core.easydonate4j.extension.data.model;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Getter
public final class PluginVersionModel {

    @SerializedName("version")
    private String version;

    @SerializedName("download")
    private String downloadUrl;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PluginVersionModel that = (PluginVersionModel) o;
        return Objects.equals(version, that.version) &&
                Objects.equals(downloadUrl, that.downloadUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, downloadUrl);
    }

    @Override
    public @NotNull String toString() {
        return "PluginVersion{" +
                "version='" + version + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                '}';
    }

}
