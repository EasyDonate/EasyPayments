package ru.easydonate.easypayments.core.platform;

import lombok.Getter;

@Getter
public class UnsupportedVersionException extends Exception {

    private final String version;

    public UnsupportedVersionException(String version) {
        this.version = version;
    }

}
