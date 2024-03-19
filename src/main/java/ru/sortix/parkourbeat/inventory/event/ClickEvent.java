package ru.sortix.parkourbeat.inventory.event;

import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ClickEvent {
    private final Player player;
    private final boolean left;
    private final boolean shift;

    @Nullable public static ClickEvent newInstance(@NonNull InventoryClickEvent event) {
        boolean isLeft;
        if (event.getClick().isLeftClick()) {
            isLeft = true;
        } else if (event.getClick().isRightClick()) {
            isLeft = false;
        } else {
            return null;
        }
        boolean isShift = event.getClick().isShiftClick();
        return new ClickEvent(((Player) event.getWhoClicked()), isLeft, isShift);
    }
}
