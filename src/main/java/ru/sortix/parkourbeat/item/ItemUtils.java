package ru.sortix.parkourbeat.item;

import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class ItemUtils {
    @NonNull
    public static ItemStack create(@NonNull Material material, @NonNull Consumer<ItemMeta> metaBuilder) {
        return modifyMeta(new ItemStack(material), metaBuilder);
    }

    @NonNull
    public static ItemStack modifyMeta(@NonNull ItemStack stack, @NonNull Consumer<ItemMeta> modifier) {
        if (!stack.editMeta(modifier)) {
            throw new IllegalArgumentException("Stack with material " + stack.getType() + " has no meta");
        }
        return stack;
    }

    public static ItemStack fixItalic(@Nullable ItemStack stack) {
        if (stack == null) return null;
        return modifyMeta(stack, meta -> {
            Component displayName = meta.displayName();
            if (displayName != null) {
                meta.displayName(displayName.decoration(TextDecoration.ITALIC, false));
            }
            List<Component> lore = meta.lore();
            if (lore != null) {
                lore.replaceAll(component ->
                    component.hasDecoration(TextDecoration.ITALIC)
                        ? component
                        : component.decoration(TextDecoration.ITALIC, false)
                );
                meta.lore(lore);
            }
        });
    }
}
