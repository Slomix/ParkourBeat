package ru.sortix.parkourbeat.game;

import static ru.sortix.parkourbeat.utils.LocationUtils.isValidSpawnPoint;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import lombok.NonNull;
import me.bomb.amusic.AMusic;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.game.movement.GameMoveHandler;
import ru.sortix.parkourbeat.levels.Level;
import ru.sortix.parkourbeat.levels.LevelsManager;
import ru.sortix.parkourbeat.levels.ParticleController;
import ru.sortix.parkourbeat.levels.settings.LevelSettings;
import ru.sortix.parkourbeat.levels.settings.Song;
import ru.sortix.parkourbeat.utils.TeleportUtils;

@Getter
public class Game {
    @NonNull public static CompletableFuture<Game> createAsync(
            @NonNull ParkourBeat plugin, @NonNull Player player, @NonNull UUID levelId) {
        CompletableFuture<Game> result = new CompletableFuture<>();
        final LevelsManager levelsManager = plugin.get(LevelsManager.class);
        levelsManager.loadLevel(levelId).thenAccept(level -> {
            if (level == null) {
                result.complete(null);
                return;
            }
            try {
                // TODO: Проверять это на этапе загрузки настроек мира и мне кажется
                // лучше чтобы мир отгружался при result.complete(false)
                if (!isValidSpawnPoint(level.getSpawn(), level.getLevelSettings())) {
                    player.sendMessage("Точка спауна установлена неверно. Невозможно начать игру.");
                    result.complete(null);

                    if (level.getWorld().getPlayers().isEmpty()) {
                        levelsManager.unloadLevelAsync(levelId);
                    }
                    return;
                }

                Game game = new Game(plugin, player, level);
                prepareGame(plugin, game);
                result.complete(game);
            } catch (Exception e) {
                plugin.getLogger().log(java.util.logging.Level.SEVERE, "Unable to prepare game", e);
                result.complete(null);
            }
        });
        return result;
    }

    private static void prepareGame(@NonNull ParkourBeat plugin, @NonNull Game game) {
        Player player = game.getPlayer();
        Level level = game.level;
        LevelSettings settings = level.getLevelSettings();

        TeleportUtils.teleportAsync(plugin, player, level.getSpawn()).thenAccept(success -> {
            if (!success) return;

            ParticleController particleController = settings.getParticleController();

            if (!particleController.isLoaded()) {
                particleController.loadParticleLocations(
                        settings.getWorldSettings().getWaypoints());
            }

            player.setGameMode(GameMode.ADVENTURE);

            boolean ready = true;
            Song song = settings.getGameSettings().getSong();
            if (song != null) {
                String currentPlayList = AMusic.getPackName(player); // nullable
                String requiredPlaylist = song.getSongPlaylist();
                if (!requiredPlaylist.equals(currentPlayList)) {
                    ready = false;
                    plugin.getServer()
                            .getScheduler()
                            .runTaskLater(plugin, () -> game.setPlaylist(requiredPlaylist), 20L);
                }
            }
            if (ready) {
                game.currentState = State.READY;
            }
        });
    }

    private final @NonNull LevelsManager levelsManager;
    private final @NonNull Player player;
    private final @NonNull Level level;
    private final @NonNull GameMoveHandler gameMoveHandler;
    private State currentState = State.PREPARING;

    private Game(@NonNull ParkourBeat plugin, @NonNull Player player, @NonNull Level level) {
        this.levelsManager = plugin.get(LevelsManager.class);
        this.player = player;
        this.level = level;
        this.gameMoveHandler = new GameMoveHandler(this);
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

        if (!this.player.isSprinting() || this.player.isSneaking()) {
            this.currentState = State.RUNNING;
            this.failLevel("§cЗажмите бег!");
            return;
        }

        LevelSettings settings = this.level.getLevelSettings();
        settings.getParticleController().startSpawnParticles(this.player);

        if (settings.getGameSettings().getSong() != null) {
            AMusic.setRepeatMode(this.player, null);
            AMusic.playSound(this.player, settings.getGameSettings().getSong().getSongName());
        }

        Plugin plugin = this.levelsManager.getPlugin();
        for (Player onlinePlayer : this.player.getWorld().getPlayers()) {
            this.player.hidePlayer(plugin, onlinePlayer);
        }

        this.currentState = State.RUNNING;
    }

    public void setCurrentState(@NonNull State currentState) {
        this.currentState = currentState;
    }

    public void failLevel(@NonNull String reason) {
        this.stopGame(reason, false);
    }

    public void completeLevel() {
        this.stopGame("§aВы прошли уровень", true);
    }

    private void stopGame(@NonNull String title, boolean levelComplete) {
        Plugin plugin = this.getPlugin();
        TeleportUtils.teleportAsync(this.getPlugin(), this.player, this.level.getSpawn())
                .thenAccept(success -> {
                    if (!success) return;
                    if (this.currentState != State.RUNNING) return;

                    this.player.setHealth(20);
                    this.player.setGameMode(GameMode.ADVENTURE);
                    this.player.sendTitle(title, null, 10, 10, 10);

                    AMusic.stopSound(this.player);
                    if (levelComplete) {
                        this.player.playSound(this.player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                        this.player.playSound(this.player.getLocation(), Sound.ENTITY_SILVERFISH_DEATH, 0.5f, 1);
                    } else {
                        this.player.playSound(this.player.getLocation(), Sound.ENTITY_SILVERFISH_DEATH, 1, 1);
                    }
                    this.level.getLevelSettings().getParticleController().stopSpawnParticlesForPlayer(this.player);
                    this.gameMoveHandler.getAccuracyChecker().reset();

                    for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                        this.player.showPlayer(plugin, onlinePlayer);
                    }

                    this.currentState = State.READY;
                });
    }

    @NonNull public ParkourBeat getPlugin() {
        return this.levelsManager.getPlugin();
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

        this.currentState = State.READY;
        settings.getParticleController().stopSpawnParticlesForPlayer(player);
    }

    public enum State {
        PREPARING,
        READY,
        RUNNING,
    }
}
