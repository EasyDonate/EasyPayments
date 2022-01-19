package ru.easydonate.easypayments.gui.item;

import lombok.Builder;
import lombok.Getter;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.gui.item.type.ItemType;
import ru.easydonate.easypayments.placeholder.bean.PlaceholderSupportingBean;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Getter
@Builder(buildMethodName = "create", setterPrefix = "with")
public class MenuItem {

    protected final ItemType type;
    protected final PlaceholderSupportingBean<Integer> amount;
    protected final String name;
    protected final List<String> lore;
    protected final int[] slots;
    protected final Integer customModelData;
    protected final ItemFlag[] itemFlags;
    protected final Map<String, PlaceholderSupportingBean<String>> nbtStrings;
    protected final Map<String, PlaceholderSupportingBean<Integer>> nbtInts;
    protected final boolean updateable;

    protected final boolean dynamicAmountEnabled;
    protected final boolean closesMenuOnClick;
    protected final List<String> playerCommands;
    protected final List<String> serverCommands;

    @Override
    public @NotNull String toString() {
        return "MenuItem{" +
                "type=" + type +
                ", amount=" + amount +
                ", name='" + name + '\'' +
                ", lore=" + lore +
                ", slots=" + Arrays.toString(slots) +
                ", customModelData=" + customModelData +
                ", itemFlags=" + Arrays.toString(itemFlags) +
                ", nbtStrings=" + nbtStrings +
                ", nbtInts=" + nbtInts +
                ", updateable=" + updateable +
                ", dynamicAmountEnabled=" + dynamicAmountEnabled +
                ", closesMenuOnClick=" + closesMenuOnClick +
                ", playerCommands=" + playerCommands +
                ", serverCommands=" + serverCommands +
                '}';
    }

}
