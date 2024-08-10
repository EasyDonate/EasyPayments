package ru.easydonate.easypayments.core.formatting;

import lombok.Getter;

import java.time.temporal.ChronoUnit;

@Getter
public enum RelativeTimeUnit {

    SECONDS(ChronoUnit.SECONDS),
    MINUTES(ChronoUnit.MINUTES),
    HOURS(ChronoUnit.HOURS),
    DAYS(ChronoUnit.DAYS),
    ;

    private final ChronoUnit chronoUnit;
    private final String formatLocaleKey;

    RelativeTimeUnit(ChronoUnit chronoUnit) {
        this.chronoUnit = chronoUnit;
        this.formatLocaleKey = "relative-time-format.time-units." + name().toLowerCase();
    }

}
