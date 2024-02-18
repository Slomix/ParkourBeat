package ru.sortix.parkourbeat.editor.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ExitItem extends SongMenuItem {

  private final Player player;

  public ExitItem(int slot, Player player) {
    super(slot);
    this.player = player;
  }

  @Override
  public ItemStack getItemStack() {
    ItemStack item = new ItemStack(Material.BARRIER, 1);
    ItemMeta meta = item.getItemMeta();
    meta.setDisplayName("Выход");
    item.setItemMeta(meta);
    return item;
  }

  @Override
  public void onClick() {
    player.closeInventory();
  }
}
