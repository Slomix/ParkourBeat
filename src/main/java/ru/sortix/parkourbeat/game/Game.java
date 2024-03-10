package ru.sortix.parkourbeat.game;

import static ru.sortix.parkourbeat.utils.LocationUtils.isValidSpawnPoint;

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
import ru.sortix.parkourbeat.levels.settings.Song;
import ru.sortix.parkourbeat.levels.settings.WorldSettings;
import ru.sortix.parkourbeat.utils.TeleportUtils;

public class Game {

    private final LevelsManager levelsManager;
    private Level level;

    @Getter
    private Player player;

    @Getter
    private State currentState;

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

        this.levelsManager.loadLevel(levelId).thenAccept(level -> {
            if (level == null) {
                result.complete(false);
                return;
            }
            try {
                this.level = level;

                // TODO: Проверять это на этапе загрузки настроек мира и мне кажется
                // лучше чтобы мир отгружался при result.complete(false)
                LevelSettings levelSettings = level.getLevelSettings();
                if (!isValidSpawnPoint(levelSettings.getWorldSettings().getSpawn(), levelSettings)) {
                    player.sendMessage("Точка спауна установлена неверно. Невозможно начать игру.");
                    result.complete(false);

                    if (level.getWorld().getPlayers().isEmpty()) {
                        this.levelsManager.unloadLevelAsync(levelId);
                    }
                    return;
                }

                this.prepareGame();
                result.complete(true);
            } catch (Exception e) {
                this.getPlugin().getLogger().log(java.util.logging.Level.SEVERE, "Unable to prepare game", e);
                result.complete(false);
            }
        });
        return result;
    }

    private void prepareGame() {
        LevelSettings settings = this.level.getLevelSettings();
        WorldSettings worldSettings = settings.getWorldSettings();
        GameSettings gameSettings = settings.getGameSettings();

        TeleportUtils.teleportAsync(this.player, worldSettings.getSpawn()).thenAccept(success -> {
            if (!success) return;

            ParticleController particleController = settings.getParticleController();

            if (!particleController.isLoaded()) {
                particleController.loadParticleLocations(worldSettings.getWaypoints());
            }

            this.player.setGameMode(GameMode.ADVENTURE);

            this.gameMoveHandler = new GameMoveHandler(this);

            boolean ready = true;
            Song song = gameSettings.getSong();
            if (song != null) {
                String currentPlayList = AMusic.getPackName(this.player); // nullable
                String requiredPlaylist = song.getSongPlaylist();
                if (!requiredPlaylist.equals(currentPlayList)) {
                    ready = false;
                    Plugin plugin = this.getPlugin();
                    plugin.getServer()
                            .getScheduler()
                            .runTaskLater(plugin, () -> this.setPlaylist(requiredPlaylist), 20L);
                }
            }
            if (ready) {
                this.currentState = State.READY;
            }
        });
    }

    private void setPlaylist(@NonNull String requiredPlaylist) {
        try {
            AMusic.loadPack(this.player, requiredPlaylist, false);
        } catch (Throwable t) {
            this.levelsManager
                    .getPlugin()
                    .getLogger()
                    .log(
                            java.util.logging.Level.SEVERE,
                            "Не удалось загрузить плейлист " + requiredPlaylist + " игроку " + this.player.getName(),
                            t);
            this.player.sendMessage("Не удалось создать ресурспак с указанной песней");
            this.currentState = State.READY;
        }
    }

    public void start() {
        if (this.currentState != State.READY) {
            return;
        }
        LevelSettings settings = this.level.getLevelSettings();
        settings.getParticleController().startSpawnParticles(player);
        if (settings.getGameSettings().getSong() != null) {
            AMusic.setRepeatMode(player, null);
            AMusic.playSound(player, settings.getGameSettings().getSong().getSongName());
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
        TeleportUtils.teleportAsync(this.player, settings.getWorldSettings().getSpawn())
                .thenAccept(success -> {
                    if (!success) return;

                    player.setHealth(20);
                    player.setGameMode(GameMode.ADVENTURE);

                    player.sendTitle(reason.title, null, 10, 10, 10);

                    AMusic.stopSound(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_SILVERFISH_DEATH, 1, 1);
                    settings.getParticleController().stopSpawnParticlesForPlayer(player);
                    gameMoveHandler.getAccuracyChecker().reset();

                    Plugin plugin = this.getPlugin();
                    for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                        player.showPlayer(plugin, onlinePlayer);
                    }

                    currentState = State.READY;
                });
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
                this.levelsManager.unloadLevelAsync(settings.getGameSettings().getLevelId());
            }
        }

        Plugin plugin = this.getPlugin();
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            player.showPlayer(plugin, onlinePlayer);
        }

        settings.getParticleController().stopSpawnParticlesForPlayer(player);
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
