package ru.sortix.parkourbeat.item.editor.type;

import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.activity.type.EditActivity;
import ru.sortix.parkourbeat.inventory.type.editor.EditorMainMenu;
import ru.sortix.parkourbeat.item.ItemUtils;
import ru.sortix.parkourbeat.item.editor.EditorItem;

public class EditorMenuItem extends EditorItem {
    @SuppressWarnings("deprecation")
    public EditorMenuItem(@NonNull ParkourBeat plugin, int slot) {
        super(plugin, slot, 20, ItemUtils.create(Material.COMPARATOR, (meta) -> {
            meta.setDisplayName(ChatColor.RED + "Параметры уровня");
        }));
    }

    @Override
    public void onUse(@NonNull PlayerInteractEvent event, @NonNull EditActivity activity) {
        new EditorMainMenu(this.plugin, activity).open(event.getPlayer());
    }
}
