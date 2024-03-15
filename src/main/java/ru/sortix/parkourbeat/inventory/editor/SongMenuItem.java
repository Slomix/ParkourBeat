package ru.sortix.parkourbeat.inventory.editor;

import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class SongMenuItem {

    private final int slot;

    public SongMenuItem(int slot) {
        this.slot = slot;
    }

    public int getSlot() {
        return slot;
    }

    public abstract ItemStack getItemStack();

    public abstract void onClick(@NonNull Player player);
}
