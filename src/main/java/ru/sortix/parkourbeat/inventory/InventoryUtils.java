package ru.sortix.parkourbeat.inventory;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

@UtilityClass
public class InventoryUtils {
    public boolean isInventoryOpen(@NonNull Player player) {
        return player.getOpenInventory().getTopInventory().getType() != InventoryType.CRAFTING;
    }
}
