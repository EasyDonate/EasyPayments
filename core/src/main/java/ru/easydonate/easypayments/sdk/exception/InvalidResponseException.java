package ru.easydonate.easypayments.sdk.exception;

public class InvalidResponseException extends BadResponseException {

    public InvalidResponseException(String rawResponse) {
        super("the EasyDonate API server sent an invalid response", rawResponse);
    }
}
