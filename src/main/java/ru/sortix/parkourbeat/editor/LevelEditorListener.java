package ru.sortix.parkourbeat.editor;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class LevelEditorListener implements Listener {

  private final LevelEditorsManager levelEditorsManager;

  public LevelEditorListener(LevelEditorsManager levelEditorsManager) {
    this.levelEditorsManager = levelEditorsManager;
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent e) {
    levelEditorsManager.onPlayerInteract(e);
  }
}
