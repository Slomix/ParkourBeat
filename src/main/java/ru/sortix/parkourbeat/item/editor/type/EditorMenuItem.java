package ru.sortix.parkourbeat.item.editor.type;

import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.activity.type.EditActivity;
import ru.sortix.parkourbeat.inventory.type.editor.EditorMainMenu;
import ru.sortix.parkourbeat.item.ItemUtils;
import ru.sortix.parkourbeat.item.editor.EditorItem;

public class EditorMenuItem extends EditorItem {
    public EditorMenuItem(@NonNull ParkourBeat plugin, int slot) {
        super(plugin, slot, 20, ItemUtils.create(Material.COMPARATOR, (meta) -> {
            meta.displayName(Component.text("Параметры уровня", NamedTextColor.RED));
        }));
    }

    @Override
    public void onUse(@NonNull PlayerInteractEvent event, @NonNull EditActivity activity) {
        new EditorMainMenu(this.plugin, activity).open(event.getPlayer());
    }
}
