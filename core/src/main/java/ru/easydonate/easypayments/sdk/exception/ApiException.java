package ru.easydonate.easypayments.sdk.exception;

import lombok.Getter;
import ru.easydonate.easypayments.sdk.response.ErrorResponse;

@Getter
public class ApiException extends BadResponseException {
    
    private final ErrorResponse errorResponse;

    public ApiException(ErrorResponse errorResponse, String rawResponse) {
        super(errorResponse.getMessage(), rawResponse);
        this.errorResponse = errorResponse;
    }

    @Override
    public String getMessage() {
        return errorResponse != null ? errorResponse.getMessage() : "Unexpected error!";
    }

}
