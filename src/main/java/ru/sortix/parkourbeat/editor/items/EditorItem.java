package ru.sortix.parkourbeat.editor.items;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import ru.sortix.parkourbeat.levels.Level;

public abstract class EditorItem {

    @Getter
    protected final int slot;

    protected final Player player;
    protected final Level level;

    @Getter
    protected ItemStack itemStack;

    public EditorItem(ItemStack itemStack, int slot, Player player, Level level) {
        this.itemStack = itemStack;
        this.slot = slot;
        this.player = player;
        this.level = level;
    }

    public abstract void onClick(Action action, Block block, @Nullable Location interactionPoint);
}
