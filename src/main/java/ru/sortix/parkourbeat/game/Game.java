package ru.sortix.parkourbeat.game;

import lombok.Getter;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.game.movement.GameMoveHandler;
import ru.sortix.parkourbeat.levels.Level;
import ru.sortix.parkourbeat.levels.LevelsManager;
import ru.sortix.parkourbeat.levels.ParticleController;
import ru.sortix.parkourbeat.levels.settings.LevelSettings;
import ru.sortix.parkourbeat.player.music.MusicTrack;
import ru.sortix.parkourbeat.player.music.MusicTracksManager;
import ru.sortix.parkourbeat.world.LocationUtils;
import ru.sortix.parkourbeat.world.TeleportUtils;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Getter
public class Game {
    private final @NonNull LevelsManager levelsManager;
    private final @NonNull Player player;
    private final @NonNull Level level;
    private final @NonNull GameMoveHandler gameMoveHandler;
    private @NonNull State currentState = State.PREPARING;
    private BukkitTask bossBarTask;
    private BossBar bossBar;

    private Game(@NonNull ParkourBeat plugin, @NonNull Player player, @NonNull Level level) {
        this.levelsManager = plugin.get(LevelsManager.class);
        this.player = player;
        this.level = level;
        this.gameMoveHandler = new GameMoveHandler(this);
        this.prepareGame(plugin);
    }

    @NonNull
    public static CompletableFuture<Game> createAsync(
        @NonNull ParkourBeat plugin, @NonNull Player player, @NonNull UUID levelId, boolean preventWrongSpawn) {
        CompletableFuture<Game> result = new CompletableFuture<>();
        LevelsManager levelsManager = plugin.get(LevelsManager.class);
        levelsManager.loadLevel(levelId, null).thenAccept(level -> {
            if (level == null) {
                result.complete(null);
                // TODO Отгружать мир
                return;
            }
            try {
                // TODO Проверять валидность точки спауна при загрузке мира и при установке новой точки
                // TODO Отключить данную проверку для уровней, прошедших модерацию
                if (!LocationUtils.isValidSpawnPoint(level.getSpawn(), level.getLevelSettings())) {
                    if (preventWrongSpawn) {
                        player.sendMessage("Игра не может быть начата, поскольку точка спауна установлена неверно.");

                        if (level.getWorld().getPlayers().isEmpty()) {
                            levelsManager.unloadLevelAsync(levelId, false);
                        }

                        result.complete(null);
                        return;
                    } else {
                        player.sendMessage(Component.text("Точка спауна установлена неверно", NamedTextColor.YELLOW));
                    }
                }

                result.complete(new Game(plugin, player, level));
            } catch (Exception e) {
                plugin.getLogger().log(java.util.logging.Level.SEVERE, "\n| Невозможно начать игру\n", e);
                result.complete(null);
                // TODO Отгружать мир
            }
        });
        return result;
    }

    private void prepareGame(@NonNull ParkourBeat plugin) {
        LevelSettings settings = this.level.getLevelSettings();

        ParticleController particleController = settings.getParticleController();

        if (!particleController.isLoaded()) {
            particleController.loadParticleLocations(
                settings.getWorldSettings().getWaypoints());
        }

        this.player.setGameMode(GameMode.ADVENTURE);

        this.setCurrentState(State.READY);

        MusicTrack musicTrack = settings.getGameSettings().getMusicTrack();
        if (musicTrack == null || musicTrack.isResourcepackCurrentlySet(this.player)) return;

        if (!musicTrack.isAvailable()) {
            this.player.sendMessage("Трек \"" + musicTrack.getName() + "\" в данный момент недоступен для загрузки");
            return;
        }

        this.setCurrentState(State.PREPARING);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {

            boolean startedSuccessfully = musicTrack.setResourcepackAsync(this.getPlugin(), this.player);
            if (startedSuccessfully) return;

            this.player.sendMessage("Не удалось загрузить трек \"" + musicTrack.getName() + "\"");
            this.setCurrentState(State.READY);

        }, 20L);
    }

    @NonNull
    public ParkourBeat getPlugin() {
        return this.levelsManager.getPlugin();
    }

    public void start() {
        if (this.currentState != State.READY) {
            return;
        }

        this.setCurrentState(State.RUNNING);

        if (!this.player.isSprinting() || this.player.isSneaking()) {
            this.failLevel("§cНе отпускайте клавишу Ctrl", null);
            return;
        }

        if (!((LivingEntity) this.player).isOnGround()) {
            this.failLevel(null, "§cНе начинайте уровень с прыжка!");
            return;
        }

        LevelSettings settings = this.level.getLevelSettings();
        settings.getParticleController().startSpawnParticles(this.player);

        MusicTrack musicTrack = settings.getGameSettings().getMusicTrack();
        if (musicTrack != null) {
            this.getPlugin().get(MusicTracksManager.class).playSongFromLoadedResourcepack(this.player);
        }

        Plugin plugin = this.levelsManager.getPlugin();
        for (Player onlinePlayer : this.player.getWorld().getPlayers()) {
            this.player.hidePlayer(plugin, onlinePlayer);
        }

        createBossBar();
    }

    public void setCurrentState(@NonNull State newState) {
        if (this.currentState == newState) return;
        this.currentState = newState;
    }

    public void failLevel(@Nullable String reasonFirstLine, @Nullable String reasonSecondLine) {
        this.resetLevelGame(reasonFirstLine, reasonSecondLine, false);
        TeleportUtils.teleportAsync(this.getPlugin(), this.player, this.level.getSpawn());
    }

    public void completeLevel() {
        this.resetLevelGame("§a§lУровень Пройден!", null, true);
        TeleportUtils.teleportAsync(this.getPlugin(), this.player, this.level.getSpawn());
    }

    public void resetLevelGame(@Nullable String reasonFirstLine, @Nullable String reasonSecondLine, boolean levelComplete) {
        boolean switchState = this.currentState == State.RUNNING;
        this.resetRunningLevelGame(reasonFirstLine, reasonSecondLine, levelComplete);
        this.forceStopLevelGame();
        if (switchState) this.setCurrentState(State.READY);
    }

    private void resetRunningLevelGame(@Nullable String reasonFirstLine, @Nullable String reasonSecondLine, boolean levelComplete) {
        if (this.currentState != State.RUNNING) return;

        this.player.sendTitle(
            reasonFirstLine == null ? "" : reasonFirstLine,
            reasonSecondLine == null ? "" : reasonSecondLine,
            10, 10, 10
        );

        if (levelComplete) {
            this.player.playSound(this.player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            this.player.playSound(this.player.getLocation(), Sound.ENTITY_SILVERFISH_DEATH, 1, 1);
        } else {
            this.player.playSound(this.player.getLocation(), Sound.ENTITY_SILVERFISH_DEATH, 1, 1);
        }

        this.gameMoveHandler.getAccuracyChecker().reset();
    }

    public void forceStopLevelGame() {
        this.player.setHealth(20);
        this.player.setGameMode(GameMode.ADVENTURE);
        this.getPlugin().get(MusicTracksManager.class).stopSongFromLoadedResourcepack(this.player);

        this.level.getLevelSettings().getParticleController().stopSpawnParticlesForPlayer(this.player);

        Plugin plugin = this.getPlugin();
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            this.player.showPlayer(plugin, onlinePlayer);
        }

        removeBossBar();
    }

    public enum State {
        PREPARING,
        READY,
        RUNNING,
    }

    private void createBossBar() {
        removeBossBar();

        String worldName = "§e§l" + player.getWorld().getName();
        bossBar = Bukkit.createBossBar(worldName, BarColor.YELLOW, BarStyle.SOLID);
        bossBar.addPlayer(player);

        bossBarTask = Bukkit.getScheduler().runTaskTimer(getPlugin(), this::updateBossBar, 0L, 1L);
    }


    private void removeBossBar() {
        if (bossBarTask != null && !bossBarTask.isCancelled()) {
            bossBarTask.cancel();
            bossBarTask = null;
        }

        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }
    }

    private void updateBossBar() {
        Location startPoint = this.level.getLevelSettings().getStartWaypoint();
        Location endPoint = this.level.getLevelSettings().getFinishWaypoint();

        double playerCoordinate = this.level.getLevelSettings().getDirectionChecker().getCoordinateWithSign(player.getLocation());
        double startCoordinate = this.level.getLevelSettings().getDirectionChecker().getCoordinateWithSign(startPoint);
        double endCoordinate = this.level.getLevelSettings().getDirectionChecker().getCoordinateWithSign(endPoint);

        double traveledDistance = startCoordinate + playerCoordinate;
        double totalDistance = endCoordinate - startCoordinate;
        double progress = Math.min(1, Math.max(0, traveledDistance / totalDistance));

        String message = String.format("§e§l%d%%", Math.round(progress * 100));

        bossBar.setTitle(message);
        bossBar.setProgress(progress);
    }
}
