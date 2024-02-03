package parkourbeat.game;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class GameManager {

    private static final Map<Player, Game> currentGames = new HashMap<>();

    public static void createNewGame(Player player, String arenaName) {
        if (!currentGames.containsKey(player)) {
            Game game = new Game();
            game.prepare(player, arenaName);
            currentGames.put(player, game);
        }
    }

    @Nullable
    public static Game getCurrentGame(Player player) {
        return currentGames.get(player);
    }

    public static boolean isInGame(Player player) {
        return currentGames.containsKey(player);
    }

    public static void removeGame(Player player) {
        Game game = currentGames.remove(player);
        if (game != null) {
            game.endGame();
        }
    }
}
