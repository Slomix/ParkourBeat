package ru.sortix.parkourbeat.editor.items;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import ru.sortix.parkourbeat.levels.Level;

public abstract class EditorItem {

    protected final int slot;
    protected final Player player;
    protected final Level level;
    protected ItemStack itemStack;

    public EditorItem(ItemStack itemStack, int slot, Player player, Level level) {
        this.itemStack = itemStack;
        this.slot = slot;
        this.player = player;
        this.level = level;
    }

    public int getSlot() {
        return slot;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public abstract void onClick(Action action, Block block);

}
