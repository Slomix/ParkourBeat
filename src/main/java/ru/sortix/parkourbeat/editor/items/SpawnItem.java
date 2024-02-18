package ru.sortix.parkourbeat.editor.items;

import java.util.ArrayList;
import java.util.Collections;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.sortix.parkourbeat.levels.Level;

public class SpawnItem extends EditorItem {

  private static final ItemStack spawnItem;
  private static final int slot = 3;

  static {
    spawnItem = new ItemStack(Material.ENDER_PEARL);
    ItemMeta meta = spawnItem.getItemMeta();
    meta.setDisplayName("Точка спавна");
    meta.setLore(new ArrayList<>(Collections.singletonList("Устанавливает точку спавна")));
    spawnItem.setItemMeta(meta);
  }

  public SpawnItem(Player player, Level level) {
    super(spawnItem.clone(), slot, player, level);
  }

  @Override
  public void onClick(Action action, Block block) {
    if (action == Action.RIGHT_CLICK_AIR) {
      level.getLevelSettings().getWorldSettings().setSpawn(player.getLocation());
      player.sendMessage("Точка спавна установлена");
    }
  }
}
