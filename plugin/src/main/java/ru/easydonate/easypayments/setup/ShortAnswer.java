package ru.easydonate.easypayments.setup;

public enum ShortAnswer {

    YES,
    NO,
    UNDEFINED;

    public boolean isUndefined() {
        return this == UNDEFINED;
    }

}
