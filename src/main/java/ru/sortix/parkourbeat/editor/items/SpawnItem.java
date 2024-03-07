package ru.sortix.parkourbeat.editor.items;

import java.util.ArrayList;
import java.util.Collections;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;
import ru.sortix.parkourbeat.levels.Level;

public class SpawnItem extends EditorItem {

    private static final ItemStack ITEM;
    private static final int SLOT = 2;

    static {
        ITEM = new ItemStack(Material.ENDER_PEARL);
        ItemMeta meta = ITEM.getItemMeta();
        meta.setDisplayName("Точка спавна");
        meta.setLore(new ArrayList<>(Collections.singletonList("Устанавливает точку спавна")));
        ITEM.setItemMeta(meta);
    }

    public SpawnItem(@NonNull Player player, @NonNull Level level) {
        super(ITEM.clone(), SLOT, player, level);
    }

    @Override
    public void onClick(Action action, Block block, @Nullable Location interactionPoint) {
        this.level.getLevelSettings().getWorldSettings().setSpawn(this.player.getLocation());
        this.player.sendMessage("Точка спауна установлена на уровне ваших ног. "
                + "Убедитесь, что направление взгляда выбрано корректно! "
                + "Именно в эту сторону будут повёрнуты игроки при телепортации");
    }
}
