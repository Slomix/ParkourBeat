package ru.sortix.parkourbeat.activity.type;

import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.player.*;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.activity.ActivityManager;
import ru.sortix.parkourbeat.activity.UserActivity;
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.game.Game;
import ru.sortix.parkourbeat.inventory.editor.SelectSongMenu;
import ru.sortix.parkourbeat.item.ItemsManager;
import ru.sortix.parkourbeat.item.editor.EditorItem;
import ru.sortix.parkourbeat.item.editor.type.EditTrackParticleItem;
import ru.sortix.parkourbeat.levels.Level;
import ru.sortix.parkourbeat.levels.LevelsManager;
import ru.sortix.parkourbeat.levels.ParticleController;
import ru.sortix.parkourbeat.levels.settings.WorldSettings;
import ru.sortix.parkourbeat.utils.TeleportUtils;

public class EditActivity extends UserActivity {
    @NonNull public static CompletableFuture<EditActivity> createAsync(
            @NonNull ParkourBeat plugin, @NonNull Player player, @NonNull Level level) {
        UserActivity activity = plugin.get(ActivityManager.class).getActivity(player);
        if (activity instanceof EditActivity && activity.getLevel().getLevelId().equals(level.getLevelId())) {
            return CompletableFuture.completedFuture((EditActivity) activity);
        }

        CompletableFuture<EditActivity> result = new CompletableFuture<>();
        Game.createAsync(plugin, player, level.getLevelId()).thenAccept(game -> {
            if (game == null) {
                result.complete(null);
                return;
            }
            EditActivity editActivity = new EditActivity(plugin, player, level);
            editActivity.start().thenAccept(success1 -> {
                result.complete(success1 ? editActivity : null);
            });
        });
        return result;
    }

    @Getter
    private final SelectSongMenu selectSongMenu;

    @Getter
    @Setter
    private Color currentColor = EditTrackParticleItem.DEFAULT_PARTICLES_COLOR;

    @Getter
    @Setter
    private double currentHeight = 0;

    private @Nullable PlayActivity testingActivity = null;

    private EditActivity(@NonNull ParkourBeat plugin, @NonNull Player player, @NonNull Level level) {
        super(plugin, player, level);
        this.selectSongMenu = new SelectSongMenu(plugin, level);
    }

    @Override
    public void startActivity() {
        if (this.testingActivity != null) {
            this.testingActivity.startActivity();
        } else {
            this.player.setGameMode(GameMode.CREATIVE);
            this.player.setFlying(true);

            this.player.getInventory().clear();
            this.plugin.get(ItemsManager.class).putAllItems(this.player, EditorItem.class);

            this.level.getLevelSettings().getParticleController().startSpawnParticles(this.player);
        }
    }

    @Override
    public void on(@NonNull PlayerResourcePackStatusEvent event) {
        if (this.testingActivity != null) this.testingActivity.on(event);
    }

    @Override
    public void on(@NonNull PlayerMoveEvent event) {
        if (this.testingActivity != null) this.testingActivity.on(event);
    }

    @Override
    public void onTick() {
        if (this.testingActivity != null) this.testingActivity.onTick();
    }

    @Override
    public void on(@NonNull PlayerToggleSprintEvent event) {
        if (this.testingActivity != null) this.testingActivity.on(event);
    }

    @Override
    public void on(@NonNull PlayerToggleSneakEvent event) {
        if (this.testingActivity != null) this.testingActivity.on(event);
    }

    @Override
    public void on(@NonNull PlayerInteractEvent event) {
        if (this.testingActivity != null) this.testingActivity.on(event);
    }

    @Override
    public int getFallHeight() {
        return this.getFallHeight(this.testingActivity == null);
    }

    @Override
    public void onPlayerFall() {
        if (this.testingActivity != null) this.testingActivity.onPlayerFall();
        TeleportUtils.teleportAsync(
                this.getPlugin(),
                this.player,
                this.level.getLevelSettings().getWorldSettings().getSpawn());
    }

    @Override
    public void endActivity() {
        if (this.testingActivity != null) this.testingActivity.endActivity();

        this.player.setGameMode(GameMode.ADVENTURE);
        this.player.getInventory().clear();

        TeleportUtils.teleportAsync(this.plugin, this.player, Settings.getLobbySpawn())
                .thenAccept(success -> {
                    if (!success) return;

                    this.level.getLevelSettings().getParticleController().stopSpawnParticles();
                    this.level.setEditing(false);

                    this.player.sendMessage("Редактор уровня \"" + this.level.getLevelName() + "\" успешно остановлен");

                    this.plugin.get(LevelsManager.class).saveLevelSettingsAndBlocks(this.level);
                    this.plugin.get(LevelsManager.class).unloadLevelAsync(this.level.getLevelId());
                });
    }

    public void startTesting() {
        if (this.testingActivity != null) throw new IllegalArgumentException("Testing already started");

        PlayActivity.createAsync(this.plugin, this.player, this.level.getLevelId(), true)
                .thenAccept(playActivity -> {
                    if (playActivity == null) {
                        this.player.sendMessage("Не удалось войти в режим тестирования");
                        return;
                    }

                    this.level.getLevelSettings().getParticleController().stopSpawnParticlesForPlayer(this.player);
                    this.testingActivity = playActivity;
                    this.testingActivity.startActivity();

                    this.player.sendMessage("Вы вошли в режим тестирования");
                });
    }

    public void endTesting() {
        if (this.testingActivity == null) throw new IllegalArgumentException("Testing not started");

        TeleportUtils.teleportAsync(
                        this.plugin,
                        this.player,
                        this.level.getLevelSettings().getWorldSettings().getSpawn())
                .thenAccept(success -> {
                    if (!success) {
                        this.player.sendMessage("Не удалось покинуть режим тестирования");
                        return;
                    }

                    this.testingActivity.endActivity();
                    this.testingActivity = null;
                    this.startActivity();

                    this.player.sendMessage("Вы покинули режим тестирования");
                });
    }

    public boolean isTesting() {
        return this.testingActivity != null;
    }

    public void openSongMenu() {
        this.selectSongMenu.open(this.player);
    }

    @NonNull private CompletableFuture<Boolean> start() {
        WorldSettings worldSettings = this.level.getLevelSettings().getWorldSettings();
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        TeleportUtils.teleportAsync(this.plugin, this.player, worldSettings.getSpawn())
                .thenAccept(success -> {
                    if (!success) {
                        result.complete(false);
                        return;
                    }

                    ParticleController particleController =
                            this.level.getLevelSettings().getParticleController();

                    this.player.sendMessage("Редактор уровня \"" + this.level.getLevelName() + "\" успешно запущен");

                    particleController.loadParticleLocations(worldSettings.getWaypoints());

                    this.level.setEditing(true);
                    result.complete(true);
                });
        return result;
    }
}
