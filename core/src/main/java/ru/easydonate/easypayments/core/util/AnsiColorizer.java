package ru.easydonate.easypayments.core.util;

import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class AnsiColorizer {

    private final String ANSI_COLOR_PATTERN = "\u001b[38;5;%dm";
    private final String ANSI_FORMAT_PATTERN = "\u001b[%dm";
    private final Pattern BUKKIT_COLOR_PATTERN = Pattern.compile("(&[0-9a-fk-or])(?!.*\1)");

    public String colorize(String message) {
        if (message == null || message.isEmpty())
            return message;

        String copy = String.copyValueOf(message.toCharArray()) + AnsiColor.RESET.getAnsiColor();

        Matcher matcher = BUKKIT_COLOR_PATTERN.matcher(message);
        while (matcher.find()) {
            String result = matcher.group(1);
            AnsiColor color = AnsiColor.getColorByCode(result.charAt(1));
            copy = copy.replace(result, color.getAnsiColor());
        }

        return copy;
    }

    @Getter
    public enum AnsiColor {

        BLACK           ('0', 0),
        DARK_BLUE       ('1', 4),
        DARK_GREEN      ('2', 2),
        DARK_AQUA       ('3', 30),
        DARK_RED        ('4', 1),
        DARK_PURPLE     ('5', 54),
        GOLD            ('6', 172),
        GRAY            ('7', 246),
        DARK_GREY       ('8', 8),
        BLUE            ('9', 4),
        GREEN           ('a', 10),
        AQUA            ('b', 51),
        RED             ('c', 9),
        LIGHT_PURPLE    ('d', 13),
        YELLOW          ('e', 11),
        WHITE           ('f', 15),
        
        STRIKETHROUGH   ('m', 9),
        ITALIC          ('o', 3),
        BOLD            ('l', 1),
        UNDERLINE       ('n', 4),
        RESET           ('r', 0);
        
        private final char bukkitColor;
        private final String ansiColor;

        AnsiColor(char bukkitColor, int ansiCode) {
            this.bukkitColor = bukkitColor;
            
            String pattern = bukkitColor >= '0' && bukkitColor <= 'f' ? ANSI_COLOR_PATTERN : ANSI_FORMAT_PATTERN;
            this.ansiColor = String.format(pattern, ansiCode);
        }

        private static AnsiColor getColorByCode(char code) {
            for (AnsiColor color: values())
                if (color.bukkitColor == code) 
                    return color;
            
            throw new IllegalArgumentException(String.format("Color with code '%s' doesn't exists!", code));
        }
        
    }

}
