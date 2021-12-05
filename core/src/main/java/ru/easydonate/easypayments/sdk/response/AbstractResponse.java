package ru.easydonate.easypayments.sdk.response;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public abstract class AbstractResponse<R> {

    @SerializedName("success")
    protected boolean success;
    @SerializedName("response")
    protected R responseObject;

    public boolean requiresStringResponse() {
        return false;
    }

    @Override
    public String toString() {
        return "AbstractResponse{" +
                "success=" + success +
                ", responseObject=" + responseObject +
                '}';
    }

}
