package ru.sortix.parkourbeat.item.editor.type;

import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.activity.type.EditActivity;
import ru.sortix.parkourbeat.item.ItemUtils;
import ru.sortix.parkourbeat.item.editor.EditorItem;

public class TestGameItem extends EditorItem {
    @SuppressWarnings("deprecation")
    public TestGameItem(@NonNull ParkourBeat plugin, int slot) {
        super(plugin, slot, 20, ItemUtils.create(Material.DIAMOND, (meta) -> {
            meta.setDisplayName(ChatColor.AQUA + "Протестировать уровень");
        }));
    }

    @Override
    public void onUse(@NonNull PlayerInteractEvent event, @NonNull EditActivity activity) {
        if (activity.isTesting()) activity.endTesting();
        else activity.startTesting();
    }
}
