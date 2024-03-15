package ru.sortix.parkourbeat.item;

import java.util.function.Consumer;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.sortix.parkourbeat.ParkourBeat;

@RequiredArgsConstructor
@Getter
public abstract class UsableItem {
    @NonNull public static ItemStack newStack(@NonNull Material material, @NonNull Consumer<ItemMeta> metaBuilder) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        metaBuilder.accept(meta);
        stack.setItemMeta(meta);
        return stack;
    }

    protected final @NonNull ParkourBeat plugin;
    protected final int slot;
    protected final @NonNull ItemStack itemStack;

    protected abstract void onUse(@NonNull PlayerInteractEvent event);
}
