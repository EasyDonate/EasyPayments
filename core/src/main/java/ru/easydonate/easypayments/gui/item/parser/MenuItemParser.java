package ru.easydonate.easypayments.gui.item.parser;

import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.config.AbstractConfiguration;
import ru.easydonate.easypayments.exception.MenuItemParseException;
import ru.easydonate.easypayments.gui.item.MenuItem;
import ru.easydonate.easypayments.gui.item.type.ItemType;
import ru.easydonate.easypayments.gui.item.type.resolver.ItemTypeResolver;
import ru.easydonate.easypayments.placeholder.bean.PlaceholderSupportingBean;
import ru.easydonate.easypayments.utility.ForwardingFunction;
import ru.easydonate.easypayments.utility.Pair;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

@Setter
@Accessors(chain = true)
public final class MenuItemParser {

    private static final ItemTypeResolver ITEM_TYPE_RESOLVER = ItemTypeResolver.getRelevantResolver();
    private static final Pattern SLOTS_STRING_PATTERN = Pattern.compile("(\\d{1,2})(?:\\s?-\\s?(\\d{1,2}))?(?:,\\?(\\d{1,2})(?:\\s?-\\s?(\\d{1,2}))?)*");

    private final ConfigurationSection config;

    private boolean itemTypeRequired = true;
    private boolean slotsRequired = true;
    private boolean customModelDataSupported = true;
    private boolean nbtTagsPlaceholdersSupported = true;

    public MenuItemParser(@NotNull ConfigurationSection config) {
        this.config = config;
    }

    public @NotNull MenuItem parse() throws MenuItemParseException {
        ItemType itemType = resolveItemType();
        int[] slots = parseSlots();

        boolean updateable = config.getBoolean("updateable", false);
        boolean dynamicAmountEnabled = config.getBoolean("dynamic-amount", false);
        boolean closesMenuOnClick = config.getBoolean("close-menu", false);

        return MenuItem.builder()
                .withType(itemType)
                .withAmount(parseAmount())
                .withName(parseColoredString("name"))
                .withLore(parseColoredList("list"))
                .withSlots(slots)
                .withCustomModelData(parseCustomModelData())
                .withItemFlags(parseItemFlags())
                .withNbtStrings(parseNbtStrings())
                .withNbtInts(parseNbtInts())
                .withUpdateable(updateable)
                .withDynamicAmountEnabled(dynamicAmountEnabled)
                .withClosesMenuOnClick(closesMenuOnClick)
                .withPlayerCommands(parseColoredList("player-commands"))
                .withServerCommands(parseColoredList("server-commands"))
                .create();
    }

    private @NotNull ItemType resolveItemType() throws MenuItemParseException {
        return ITEM_TYPE_RESOLVER.resolve(config, itemTypeRequired);
    }

    private @NotNull PlaceholderSupportingBean<Integer> parseAmount() {
        if(config.isInt("amount")) {
            return PlaceholderSupportingBean.constant(config.getInt("amount", 1));
        }

        if(config.isString("amount")) {
            return PlaceholderSupportingBean.placeholder(config.getString("amount"), Integer::parseInt);
        }

        return PlaceholderSupportingBean.constant(1);
    }

    private @Nullable String parseColoredString(@NotNull String key) {
        return AbstractConfiguration.colorize(config.getString(key));
    }

    private @Nullable List<String> parseColoredList(@NotNull String key) {
        return AbstractConfiguration.colorize(config.getStringList(key));
    }

    private @Nullable int[] parseSlots() throws MenuItemParseException {
        Set<Integer> slots = new LinkedHashSet<>();

        // slot: 4
        if(config.isInt("slot")) {
            slots.add(config.getInt("slot"));
        }

        // slots: ?
        if(config.contains("slots")) {
            if(config.isInt("slots")) {
                // slots: 4 (possible user mistake)
                slots.add(config.getInt("slots"));

            } else if(config.isList("slots")) {
                // slots: [1, 4, 7]
                List<Integer> asIntList = config.getIntegerList("slots");
                slots.addAll(asIntList);

            } else if(config.isString("slots")) {
                // slots: '<any combination>'
                String value = config.getString("slots");
                if(!value.isEmpty()) {
                    Matcher matcher = SLOTS_STRING_PATTERN.matcher(value);
                    while(matcher.find()) {
                        int groupCount = matcher.groupCount();
                        try {
                            if(groupCount == 2) {
                                // single index
                                slots.add(Integer.parseInt(matcher.group(1)));
                            } else if(groupCount == 3) {
                                // indexes range
                                int first = Integer.parseInt(matcher.group(1));
                                int second = Integer.parseInt(matcher.group(2));

                                if(first >= 0 && second >= 0) {
                                    if(first == second) {
                                        slots.add(first);
                                    } else {
                                        int min = Math.min(first, second);
                                        int max = Math.max(first, second);
                                        IntStream.rangeClosed(min, max).forEach(slots::add);
                                    }
                                }
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
        }

        if(slots.isEmpty() && slotsRequired)
            throw new MenuItemParseException(config, "Cannot parse the menu item display slots!");

        if(slots.stream().anyMatch(i -> i < 0))
            throw new MenuItemParseException(config, "A display slot cannot be a negative digit!");

        return slots.stream().mapToInt(Integer::intValue).toArray();
    }

    private @Nullable Integer parseCustomModelData() {
        if(!customModelDataSupported)
            return null;

        return config.isInt("custom-model-data") ? config.getInt("custom-model-data") : null;
    }

    private @Nullable ItemFlag[] parseItemFlags() {
        Set<ItemFlag> itemFlags = new LinkedHashSet<>();
        addWithCondition(itemFlags, config.getBoolean("hide-attributes", false), ItemFlag.HIDE_ATTRIBUTES);
        addWithCondition(itemFlags, config.getBoolean("hide-destroys", false), ItemFlag.HIDE_DESTROYS);
        addWithCondition(itemFlags, config.getBoolean("hide-enchants", false), ItemFlag.HIDE_ENCHANTS);
        addWithCondition(itemFlags, config.getBoolean("hide-placed-on", false), ItemFlag.HIDE_PLACED_ON);
        addWithCondition(itemFlags, config.getBoolean("hide-potion-effects", false), ItemFlag.HIDE_POTION_EFFECTS);
        addWithCondition(itemFlags, config.getBoolean("hide-unbreakable", false), ItemFlag.HIDE_UNBREAKABLE);
        return !itemFlags.isEmpty() ? itemFlags.toArray(new ItemFlag[0]) : null;
    }

    private <T> void addWithCondition(@NotNull Collection<T> collection, boolean condition, @Nullable T value) {
        if(condition)
            collection.add(value);
    }

    private @Nullable Map<String, PlaceholderSupportingBean<String>> parseNbtStrings() throws MenuItemParseException {
        return parseNbtTags("nbt-string", ForwardingFunction.create());
    }

    private @Nullable Map<String, PlaceholderSupportingBean<Integer>> parseNbtInts() throws MenuItemParseException {
        return parseNbtTags("nbt-ints", Integer::parseInt);
    }

    private <T> @Nullable Map<String, PlaceholderSupportingBean<T>> parseNbtTags(
            @NotNull String baseKey,
            @NotNull Function<String, T> valueConverter
    ) throws MenuItemParseException {
        Map<String, PlaceholderSupportingBean<T>> nbtTags = new LinkedHashMap<>();

        String singleTag = config.getString(baseKey);
        if(singleTag != null && !singleTag.isEmpty()) {
            Pair<String, PlaceholderSupportingBean<T>> nbtTag = parseNbtTag(singleTag, valueConverter);
            nbtTags.put(nbtTag.getKey(), nbtTag.getValue());
        }

        List<String> tagsList = config.getStringList(baseKey + 's');
        if(tagsList != null && !tagsList.isEmpty()) {
            for(String rawTag : tagsList) {
                Pair<String, PlaceholderSupportingBean<T>> nbtTag = parseNbtTag(rawTag, valueConverter);
                nbtTags.put(nbtTag.getKey(), nbtTag.getValue());
            }
        }

        return !nbtTags.isEmpty() ? nbtTags : null;
    }

    private <T> @NotNull Pair<String, PlaceholderSupportingBean<T>> parseNbtTag(
            @NotNull String string,
            @NotNull Function<String, T> valueConverter
    ) throws MenuItemParseException {
        int delimiterIndex = string.indexOf(":");
        if(delimiterIndex == -1 || delimiterIndex == 0 || delimiterIndex == string.length() - 1)
            throw new MenuItemParseException(config, "Couldn't parse NBT tag '%s', it must be formatted as '<key>:<value>'!", string);

        String key = string.substring(0, delimiterIndex);
        String rawValue = string.substring(delimiterIndex + 1);
        PlaceholderSupportingBean<T> valueBean;

        try {
            T value = valueConverter.apply(rawValue);

            if(value instanceof String && nbtTagsPlaceholdersSupported) {
                valueBean = PlaceholderSupportingBean.placeholder(rawValue, valueConverter::apply);
            } else {
                valueBean = PlaceholderSupportingBean.constant(value);
            }
        } catch (NumberFormatException ignored) {
            if(nbtTagsPlaceholdersSupported) {
                valueBean = PlaceholderSupportingBean.placeholder(rawValue, valueConverter::apply);
            } else {
                throw new MenuItemParseException(config, "NBT tag '%s' has an invalid type of its value! (%s)", key, rawValue);
            }
        }

        return new Pair<>(key, valueBean);
    }

}
