package ru.sortix.parkourbeat.item;

import java.util.function.Consumer;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemUtils {
    @NonNull public static ItemStack modifyMeta(@NonNull ItemStack stack, @NonNull Consumer<ItemMeta> modifier) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            throw new IllegalArgumentException("Stack with material " + stack.getType() + " has no meta");
        }
        modifier.accept(meta);
        stack.setItemMeta(meta);
        return stack;
    }
}
