package ru.sortix.parkourbeat.item.editor.type;

import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.activity.ActivityManager;
import ru.sortix.parkourbeat.activity.type.EditActivity;
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.item.editor.EditorItem;
import ru.sortix.parkourbeat.utils.TeleportUtils;

public class LeaveEditorItem extends EditorItem {
    @SuppressWarnings("deprecation")
    public LeaveEditorItem(@NonNull ParkourBeat plugin, int slot) {
        super(plugin, slot, newStack(Material.REDSTONE_TORCH, (meta) -> {
            meta.setDisplayName("Покинуть редактор");
        }));
    }

    @Override
    public void onUse(@NonNull PlayerInteractEvent event, @NonNull EditActivity activity) {
        Player player = event.getPlayer();
        TeleportUtils.teleportAsync(this.plugin, player, Settings.getLobbySpawn())
                .thenAccept(success -> {
                    if (!success) return;
                    this.plugin.get(ActivityManager.class).setActivity(player, null);
                });
    }
}
