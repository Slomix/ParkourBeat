package ru.sortix.parkourbeat.game;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.bomb.amusic.AMusic;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.sortix.parkourbeat.game.movement.GameMoveHandler;
import ru.sortix.parkourbeat.levels.Level;
import ru.sortix.parkourbeat.levels.LevelsManager;
import ru.sortix.parkourbeat.levels.ParticleController;
import ru.sortix.parkourbeat.levels.settings.GameSettings;
import ru.sortix.parkourbeat.levels.settings.LevelSettings;
import ru.sortix.parkourbeat.levels.settings.WorldSettings;

public class Game {

    private final LevelsManager levelsManager;
    private Level level;
    @Getter private Player player;
    @Getter private State currentState;
    private GameMoveHandler gameMoveHandler;

    public Game(LevelsManager levelsManager) {
        currentState = State.PREPARING;
        player = null;
        level = null;
        gameMoveHandler = null;
        this.levelsManager = levelsManager;
    }

    @NonNull public CompletableFuture<Boolean> prepare(@NonNull Player player, @NonNull UUID levelId) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        currentState = State.PREPARING;
        this.player = player;

        this.levelsManager
                .loadLevel(levelId)
                .thenAccept(
                        level -> {
                            try {
                                this.level = level;
                                this.prepareGame();
                                result.complete(true);
                            } catch (Exception e) {
                                e.printStackTrace();
                                result.complete(false);
                            }
                        });
        return result;
    }

    private void prepareGame() {
        LevelSettings settings = this.level.getLevelSettings();
        WorldSettings worldSettings = settings.getWorldSettings();
        GameSettings gameSettings = settings.getGameSettings();
        ParticleController particleController = settings.getParticleController();

        if (!particleController.isLoaded()) {
            particleController.loadParticleLocations(worldSettings.getWaypoints());
        }

        this.player.teleport(worldSettings.getSpawn());
        this.gameMoveHandler = new GameMoveHandler(this);

        String songPlayListName = gameSettings.getSongPlayListName();
        if (gameSettings.getSongName() != null
                && !songPlayListName.equals(AMusic.getPackName(player))) {
            this.getPlugin()
                    .getServer()
                    .getScheduler()
                    .scheduleSyncDelayedTask(
                            this.getPlugin(),
                            () -> {
                                try {
                                    AMusic.loadPack(player, songPlayListName, false);
                                } catch (Throwable t) {
                                    this.levelsManager
                                            .getPlugin()
                                            .getLogger()
                                            .log(
                                                    java.util.logging.Level.SEVERE,
                                                    "Не удалось загрузить пак "
                                                            + songPlayListName
                                                            + " игроку "
                                                            + player.getName(),
                                                    t);
                                }
                            },
                            20L);
        } else {
            currentState = State.READY;
        }
    }

    public void start() {
        if (currentState != State.READY) {
            return;
        }
        LevelSettings settings = this.level.getLevelSettings();
        settings.getParticleController().startSpawnParticles(player);
        if (settings.getGameSettings().getSongName() != null) {
            AMusic.setRepeatMode(player, null);
            AMusic.playSound(player, settings.getGameSettings().getSongName());
        }
        Plugin plugin = this.levelsManager.getPlugin();
        for (Player onlinePlayer : player.getWorld().getPlayers()) {
            player.hidePlayer(plugin, onlinePlayer);
        }

        currentState = State.RUNNING;
    }

    @NotNull public Level getLevel() {
        return this.level;
    }

    @NotNull public GameMoveHandler getGameMoveHandler() {
        if (this.gameMoveHandler == null) {
            throw new IllegalStateException("Unable to get " + GameMoveHandler.class.getSimpleName());
        }
        return this.gameMoveHandler;
    }

    public void setCurrentState(State currentState) {
        player.sendMessage("State: " + currentState);
        this.currentState = currentState;
    }

    public void stopGame(StopReason reason) {
        LevelSettings settings = this.level.getLevelSettings();
        player.setFallDistance(0f);
        player.teleport(settings.getWorldSettings().getSpawn());
        player.setHealth(20);
        player.setGameMode(GameMode.ADVENTURE);

        player.sendTitle(reason.title, null, 10, 10, 10);

        AMusic.stopSound(player);
        player.playSound(player.getLocation(), Sound.ENTITY_SILVERFISH_DEATH, 1, 1);
        settings.getParticleController().stopSpawnParticles(player);
        gameMoveHandler.getAccuracyChecker().reset();

        Plugin plugin = this.getPlugin();
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            player.showPlayer(plugin, onlinePlayer);
        }

        currentState = State.READY;
    }

    @NonNull public Plugin getPlugin() {
        return this.levelsManager.getPlugin();
    }

    public void endGame() {
        endGame(true);
    }

    public void endGame(boolean unloadLevel) {
        LevelSettings settings = this.level.getLevelSettings();
        player.setHealth(20);
        AMusic.stopSound(player);

        if (unloadLevel) {
            if (settings.getWorldSettings().isWorldEmpty()) {
                this.levelsManager.unloadLevel(settings.getGameSettings().getLevelId());
            }
        }

        Plugin plugin = this.getPlugin();
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            player.showPlayer(plugin, onlinePlayer);
        }

        settings.getParticleController().stopSpawnParticles(player);
    }

    public enum State {
        PREPARING,
        READY,
        RUNNING,
    }

    @RequiredArgsConstructor
    public enum StopReason {
        FINISH("§aВы прошли уровень"),
        DEATH("§cВы умерли"),
        WRONG_DIRECTION("§cНельзя бежать назад"),
        STOP_MOVEMENT("§cВы остановились"),
        FALL("§cВы упали");

        @NonNull private final String title;
    }
}
