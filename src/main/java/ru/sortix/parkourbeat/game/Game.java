package ru.sortix.parkourbeat.game;

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
import ru.sortix.parkourbeat.world.TeleportUtils;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static ru.sortix.parkourbeat.world.LocationUtils.isValidSpawnPoint;

@Getter
public class Game {
    private final @NonNull LevelsManager levelsManager;
    private final @NonNull Player player;
    private final @NonNull Level level;
    private final @NonNull GameMoveHandler gameMoveHandler;
    private @NonNull State currentState = State.PREPARING;

    private Game(@NonNull ParkourBeat plugin, @NonNull Player player, @NonNull Level level) {
        this.levelsManager = plugin.get(LevelsManager.class);
        this.player = player;
        this.level = level;
        this.gameMoveHandler = new GameMoveHandler(this);
    }

    @NonNull
    public static CompletableFuture<Game> createAsync(
        @NonNull ParkourBeat plugin, @NonNull Player player, @NonNull UUID levelId, boolean preventWrongSpawn) {
        CompletableFuture<Game> result = new CompletableFuture<>();
        LevelsManager levelsManager = plugin.get(LevelsManager.class);
        levelsManager.loadLevel(levelId, null).thenAccept(level -> {
            if (level == null) {
                result.complete(null);
                return;
            }
            try {
                // TODO: Проверять это на этапе загрузки настроек мира и мне кажется
                // лучше чтобы мир отгружался при result.complete(false)
                if (!isValidSpawnPoint(level.getSpawn(), level.getLevelSettings())) {
                    if (preventWrongSpawn) {
                        player.sendMessage("Точка спауна установлена неверно. Невозможно начать игру");

                        if (level.getWorld().getPlayers().isEmpty()) {
                            levelsManager.unloadLevelAsync(levelId);
                        }

                        result.complete(null);
                        return;
                    } else {
                        player.sendMessage("Точка спауна установлена неверно");
                    }
                }

                Game game = new Game(plugin, player, level);
                prepareGame(plugin, game)
                    .thenAccept(success -> result.complete(success ? game : null));
            } catch (Exception e) {
                plugin.getLogger().log(java.util.logging.Level.SEVERE, "Unable to prepare game", e);
                result.complete(null);
            }
        });
        return result;
    }

    @NonNull
    private static CompletableFuture<Boolean> prepareGame(@NonNull ParkourBeat plugin, @NonNull Game game) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        Player player = game.getPlayer();
        LevelSettings settings = game.level.getLevelSettings();

        TeleportUtils.teleportAsync(plugin, player, game.level.getSpawn()).thenAccept(success -> {
            if (!success) {
                result.complete(false);
                return;
            }

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
            result.complete(true);
        });
        return result;
    }

    @NonNull
    public ParkourBeat getPlugin() {
        return this.levelsManager.getPlugin();
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

    public @NonNull CompletableFuture<Void> failLevel(@NonNull String reason) {
        return this.stopGame(reason, false);
    }

    public @NonNull CompletableFuture<Void> completeLevel() {
        return this.stopGame("§aВы прошли уровень", true);
    }

    private @NonNull CompletableFuture<Void> stopGame(@NonNull String title, boolean levelComplete) {
        return
        TeleportUtils.teleportAsync(this.getPlugin(), this.player, this.level.getSpawn())
            .thenAccept(success -> {
                if (!success) return;
                this.resetRunningLevelGame(title, levelComplete);
                this.forceStopLevelGame();
                this.currentState = State.READY;
            });
    }

    private void resetRunningLevelGame(@NonNull String title, boolean levelComplete) {
                if (this.currentState != State.RUNNING) return;


                this.player.sendTitle(title, null, 10, 10, 10);


                if (levelComplete) {
                    this.player.playSound(this.player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                    this.player.playSound(this.player.getLocation(), Sound.ENTITY_SILVERFISH_DEATH, 0.5f, 1);
                } else {
                    this.player.playSound(this.player.getLocation(), Sound.ENTITY_SILVERFISH_DEATH, 1, 1);
                }

                this.gameMoveHandler.getAccuracyChecker().reset();


    }

    public void forceStopLevelGame() {
        this.player.setHealth(20);
        this.player.setGameMode(GameMode.ADVENTURE);
        AMusic.stopSound(this.player);

        this.level.getLevelSettings().getParticleController().stopSpawnParticlesForPlayer(this.player);

        Plugin plugin = this.getPlugin();
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            this.player.showPlayer(plugin, onlinePlayer);
        }

        this.currentState = State.PREPARING;
    }

    public enum State {
        PREPARING,
        READY,
        RUNNING,
    }
}
