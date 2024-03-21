package ru.sortix.parkourbeat.item.editor;

import lombok.NonNull;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.activity.ActivityManager;
import ru.sortix.parkourbeat.activity.UserActivity;
import ru.sortix.parkourbeat.activity.type.EditActivity;
import ru.sortix.parkourbeat.item.UsableItem;

public abstract class EditorItem extends UsableItem {
    public EditorItem(@NonNull ParkourBeat plugin, int slot, int cooldownTicks, @NonNull ItemStack itemStack) {
        super(plugin, slot, cooldownTicks, itemStack.clone());
    }

    @Override
    protected final void onUse(@NonNull PlayerInteractEvent event) {
        UserActivity activity = this.plugin.get(ActivityManager.class).getActivity(event.getPlayer());
        if (!(activity instanceof EditActivity)) return;
        this.onUse(event, (EditActivity) activity);
    }

    public abstract void onUse(@NonNull PlayerInteractEvent event, @NonNull EditActivity activity);
}
