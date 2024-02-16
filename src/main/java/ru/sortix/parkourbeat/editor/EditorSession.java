package ru.sortix.parkourbeat.editor;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.editor.items.EditorItem;
import ru.sortix.parkourbeat.editor.items.ItemsContainer;
import ru.sortix.parkourbeat.editor.menu.SongMenu;
import ru.sortix.parkourbeat.game.GameManager;
import ru.sortix.parkourbeat.levels.Level;
import ru.sortix.parkourbeat.levels.LevelsManager;
import ru.sortix.parkourbeat.levels.ParticleController;
import ru.sortix.parkourbeat.levels.settings.LevelSettings;
import ru.sortix.parkourbeat.levels.settings.WorldSettings;

public class EditorSession {

  private final Player owner;
  private final Level level;
  private final LevelsManager levelsManager;
  private final ItemsContainer editorItems;
  private final GameManager gameManager;
  private final Inventory songMenu;

  public EditorSession(
      Player owner, Level level, LevelsManager levelsManager, GameManager gameManager) {
    this.owner = owner;
    this.level = level;
    this.levelsManager = levelsManager;
    this.gameManager = gameManager;
    this.editorItems = new ItemsContainer(owner, level, gameManager);
    songMenu =
        new SongMenu(ParkourBeat.getSongs(), owner, level.getLevelSettings().getGameSettings())
            .getInventory();
  }

  public void openSongMenu() {
    owner.openInventory(songMenu);
  }

  public <T extends EditorItem> T getEditorItem(Class<T> editorItemClass) {
    return editorItems.getEditorItem(editorItemClass);
  }

  public void start() {
    PlayerInventory inventory = owner.getInventory();
    inventory.clear();
    editorItems.giveToPlayer();

    LevelSettings levelSettings = level.getLevelSettings();
    WorldSettings worldSettings = levelSettings.getWorldSettings();
    ParticleController particleController = levelSettings.getParticleController();

    particleController.loadParticleLocations(worldSettings.getWaypoints());
    particleController.startSpawnParticles(owner);

    owner.setGameMode(GameMode.CREATIVE);
    owner.teleport(worldSettings.getSpawn());
    owner.sendMessage("Редактор уровня " + level.getName() + " успешно запущен");

    level.setEditing(true);
  }

  public void stop() {
    level.getLevelSettings().getParticleController().stopSpawnParticles(owner);
    level.setEditing(false);

    gameManager.removeGame(owner, false);

    owner.setGameMode(GameMode.ADVENTURE);
    owner.getInventory().clear();
    owner.teleport(Settings.getLobbySpawn());
    owner.sendMessage("Редактор уровня " + level.getName() + " успешно остановлен");

    levelsManager.saveLevel(level);
    levelsManager.unloadLevel(level.getName());
  }

  public void onPlayerInteract(PlayerInteractEvent e) {
    if (e.getItem() == null) {
      return;
    }
    EditorItem editorItem = editorItems.getEditorItems().get(e.getItem());
    if (editorItem != null) {
      editorItem.onClick(e.getAction(), e.getClickedBlock());
      e.setCancelled(true);
    }
  }
}
