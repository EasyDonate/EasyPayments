package ru.soknight.easypayments.sdk.exception;

import ru.soknight.easypayments.sdk.response.AbstractResponse;

public class UnsuccessfulResponseException extends BadResponseException {

    private final AbstractResponse<?> response;

    public UnsuccessfulResponseException(AbstractResponse<?> response, String rawResponse) {
        super("an API response returned false, but was parsed correctly...", rawResponse);
        this.response = response;
    }

    @SuppressWarnings("unchecked")
    public <T> AbstractResponse<T> getResponse() {
        return (AbstractResponse<T>) response;
    }

}
