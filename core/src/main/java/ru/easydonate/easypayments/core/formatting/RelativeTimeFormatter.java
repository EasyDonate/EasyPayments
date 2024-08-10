package ru.easydonate.easypayments.core.formatting;

import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.config.localized.Messages;

import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class RelativeTimeFormatter {

    private final Messages messages;
    private final Map<RelativeTimeUnit, String> timeUnitFormats;
    private String timeUnitDelimiter;

    public RelativeTimeFormatter(@NotNull Messages messages) {
        this.messages = messages;
        this.timeUnitFormats = new ConcurrentHashMap<>();
        update();
    }

    public void update() {
        this.timeUnitDelimiter = messages.getColoredString("relative-time-format.delimiter", "");
        this.timeUnitFormats.clear();

        for (RelativeTimeUnit timeUnit : RelativeTimeUnit.values()) {
            String timeUnitFormat = messages.getColoredString(timeUnit.getFormatLocaleKey(), "");
            if (!timeUnitFormat.isEmpty()) {
                this.timeUnitFormats.put(timeUnit, timeUnitFormat);
            }
        }
    }

    public @NotNull String formatElapsedTime(@NotNull LocalDateTime from) {
        LocalDateTime now = LocalDateTime.now();

        int seconds = calculateTimeUnitValue(RelativeTimeUnit.SECONDS, from, now) % 60;
        int minutes = calculateTimeUnitValue(RelativeTimeUnit.MINUTES, from, now) % 60;
        int hours = calculateTimeUnitValue(RelativeTimeUnit.HOURS, from, now) % 24;
        int days = calculateTimeUnitValue(RelativeTimeUnit.DAYS, from, now);

        List<String> timeUnitValues = new ArrayList<>();
        addTimeUnitValue(timeUnitValues, RelativeTimeUnit.DAYS, days);
        addTimeUnitValue(timeUnitValues, RelativeTimeUnit.HOURS, hours);
        addTimeUnitValue(timeUnitValues, RelativeTimeUnit.MINUTES, minutes);
        addTimeUnitValue(timeUnitValues, RelativeTimeUnit.SECONDS, seconds);

        if (timeUnitValues.isEmpty())
            addTimeUnitValue(timeUnitValues, RelativeTimeUnit.SECONDS, 1);

        return String.join(timeUnitDelimiter, timeUnitValues);
    }

    private int calculateTimeUnitValue(@NotNull RelativeTimeUnit timeUnit, @NotNull Temporal from, @NotNull Temporal to) {
        return timeUnitFormats.containsKey(timeUnit)
                ? (int) timeUnit.getChronoUnit().between(from, to)
                : 0;
    }

    private void addTimeUnitValue(@NotNull List<String> addTo, @NotNull RelativeTimeUnit timeUnit, int value) {
        if (value > 0)
            addTo.add(formatTimeUnitValue(timeUnit, value));
    }

    private @NotNull String formatTimeUnitValue(@NotNull RelativeTimeUnit timeUnit, int value) {
        String format = timeUnitFormats.get(timeUnit);
        if (format == null)
            return "";

        try {
            return String.format(format, value);
        } catch (IllegalFormatException ignored) {
            return "";
        }
    }

}
