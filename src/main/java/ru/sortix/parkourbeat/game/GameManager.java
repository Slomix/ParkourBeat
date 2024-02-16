package ru.sortix.parkourbeat.game;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import ru.sortix.parkourbeat.levels.LevelsManager;

public class GameManager {

  private final Map<Player, Game> currentGames = new HashMap<>();
  private final LevelsManager levelsManager;

  public GameManager(LevelsManager levelsManager) {
    this.levelsManager = levelsManager;
  }

  public void createNewGame(Player player, String arenaName) {
    if (!currentGames.containsKey(player)) {
      Game game = new Game(levelsManager);
      game.prepare(player, arenaName);
      currentGames.put(player, game);
    }
  }

  @Nullable public Game getCurrentGame(Player player) {
    return currentGames.get(player);
  }

  public boolean isInGame(Player player) {
    return currentGames.containsKey(player);
  }

  public void removeGame(Player player) {
    removeGame(player, true);
  }

  public void removeGame(Player player, boolean unloadLevel) {
    Game game = currentGames.remove(player);
    if (game != null) {
      game.endGame(unloadLevel);
    }
  }
}
