package ru.soknight.easypayments.sdk.response;

import com.google.gson.annotations.SerializedName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorResponse {
    
    @SerializedName("success")
    private boolean success;
    @SerializedName("response")
    private String message;

    public static ErrorResponse internal(String message) {
        return new ErrorResponse(false, message);
    }

}
