package ru.soknight.easypayments.nms;

import lombok.Getter;

@Getter
public class UnsupportedVersionException extends Exception {

    private final String version;

    public UnsupportedVersionException(String version) {
        this.version = version;
    }

}
