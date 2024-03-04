package ru.sortix.parkourbeat.game;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import ru.sortix.parkourbeat.levels.Level;
import ru.sortix.parkourbeat.levels.LevelsManager;

public class GameManager {

    private final Map<Player, Game> currentGames = new HashMap<>();
    private final LevelsManager levelsManager;

    public GameManager(LevelsManager levelsManager) {
        this.levelsManager = levelsManager;
    }

    @NonNull public CompletableFuture<Boolean> createNewGame(@NonNull Player player, @NonNull UUID levelId) {
        if (this.currentGames.containsKey(player)) {
            return CompletableFuture.completedFuture(true);
        }
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        Game game = new Game(this.levelsManager);
        game.prepare(player, levelId)
                .thenAccept(
                        success -> {
                            if (!success) {
                                result.complete(false);
                                return;
                            }
                            this.currentGames.put(player, game);
                            result.complete(true);
                        });
        return result;
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

    @NonNull public Collection<Player> getPlayersOnLevel(@NonNull Level level) {
        List<Player> result = new ArrayList<>();
        for (Map.Entry<Player, Game> entry : this.currentGames.entrySet()) {
            if (entry.getValue().getLevel() != level) continue;
            result.add(entry.getKey());
        }
        return result;
    }
}
