package ru.soknight.easypayments.sdk.exception;

public class ErrorResponseException extends Exception {

    public ErrorResponseException(Throwable throwable) {
        super(null, throwable, false, false);
    }

}
