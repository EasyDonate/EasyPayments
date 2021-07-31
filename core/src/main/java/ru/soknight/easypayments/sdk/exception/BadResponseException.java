package ru.soknight.easypayments.sdk.exception;

import lombok.Getter;

@Getter
public abstract class BadResponseException extends Exception {
    
    private final String rawResponse;

    public BadResponseException(String message, String rawResponse) {
        super(message);
        this.rawResponse = rawResponse;
    }
}
