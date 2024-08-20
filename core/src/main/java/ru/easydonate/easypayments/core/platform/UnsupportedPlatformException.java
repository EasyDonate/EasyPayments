package ru.easydonate.easypayments.core.platform;

public final class UnsupportedPlatformException extends Exception {

    public UnsupportedPlatformException() {
        super();
    }

    public UnsupportedPlatformException(String message) {
        super(message);
    }

    public UnsupportedPlatformException(Throwable cause) {
        super(cause);
    }

    public UnsupportedPlatformException(String message, Throwable cause) {
        super(message, cause);
    }

}
