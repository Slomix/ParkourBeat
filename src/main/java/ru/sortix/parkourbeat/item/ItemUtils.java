package ru.sortix.parkourbeat.item;

import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Consumer;

public class ItemUtils {
    @NonNull
    public static ItemStack create(@NonNull Material material, @NonNull Consumer<ItemMeta> metaBuilder) {
        return modifyMeta(new ItemStack(material), metaBuilder);
    }

    @NonNull
    public static ItemStack modifyMeta(@NonNull ItemStack stack, @NonNull Consumer<ItemMeta> modifier) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            throw new IllegalArgumentException("Stack with material " + stack.getType() + " has no meta");
        }
        modifier.accept(meta);
        stack.setItemMeta(meta);
        return stack;
    }
}
