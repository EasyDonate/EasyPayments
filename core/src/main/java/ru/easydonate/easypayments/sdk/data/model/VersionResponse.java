package ru.easydonate.easypayments.sdk.data.model;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.Objects;

@Getter
public final class VersionResponse {

    @SerializedName("version")
    private String version;
    @SerializedName("download")
    private String downloadUrl;

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        VersionResponse that = (VersionResponse) o;
        return Objects.equals(version, that.version) &&
                Objects.equals(downloadUrl, that.downloadUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, downloadUrl);
    }

    @Override
    public String toString() {
        return "VersionResponse{" +
                "version=" + version +
                ", downloadUrl='" + downloadUrl + '\'' +
                '}';
    }

}