package ru.sortix.parkourbeat.item.editor.type;

import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.activity.type.EditActivity;
import ru.sortix.parkourbeat.item.editor.EditorItem;

public class TestGameItem extends EditorItem {
    @SuppressWarnings("deprecation")
    public TestGameItem(@NonNull ParkourBeat plugin, int slot) {
        super(plugin, slot, newStack(Material.DIAMOND, (meta) -> {
            meta.setDisplayName(ChatColor.AQUA + "Протестировать уровень");
        }));
    }

    @Override
    public void onUse(@NonNull PlayerInteractEvent event, @NonNull EditActivity activity) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (activity.isTesting()) activity.endTesting();
        else activity.startTesting();
    }
}
