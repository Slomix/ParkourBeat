package ru.sortix.parkourbeat.activity.type;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.ItemStack;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.activity.ActivityManager;
import ru.sortix.parkourbeat.activity.UserActivity;
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.game.Game;
import ru.sortix.parkourbeat.item.ItemsManager;
import ru.sortix.parkourbeat.item.editor.EditorItem;
import ru.sortix.parkourbeat.item.editor.type.EditTrackPointsItem;
import ru.sortix.parkourbeat.levels.Level;
import ru.sortix.parkourbeat.levels.LevelsManager;
import ru.sortix.parkourbeat.world.TeleportUtils;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class EditActivity extends UserActivity {
    @Getter
    @Setter
    private Color currentColor = EditTrackPointsItem.DEFAULT_PARTICLES_COLOR;
    @Getter
    @Setter
    private double currentHeight = 0;
    private @Nullable PlayActivity testingActivity = null;
    private ItemStack[] creativeInventoryContents = null;
    private EditActivity(@NonNull ParkourBeat plugin, @NonNull Player player, @NonNull Level level) {
        super(plugin, player, level);
    }

    @NonNull
    public static CompletableFuture<EditActivity> createAsync(
        @NonNull ParkourBeat plugin, @NonNull Player player, @NonNull Level level) {
        UserActivity activity = plugin.get(ActivityManager.class).getActivity(player);
        if (activity instanceof EditActivity
            && activity.getLevel().getUniqueId().equals(level.getUniqueId())) {
            return CompletableFuture.completedFuture((EditActivity) activity);
        }

        CompletableFuture<EditActivity> result = new CompletableFuture<>();
        Game.createAsync(plugin, player, level.getUniqueId(), false).thenAccept(game -> {
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

    @Override
    public @NonNull CompletableFuture<Void> startActivity() {
        if (this.testingActivity != null) {
            return this.testingActivity.startActivity();
        } else {
            this.player.setGameMode(GameMode.CREATIVE);
            this.player.setFlying(true);

            this.player.getInventory().clear();
            this.plugin.get(ItemsManager.class).putAllItems(this.player, EditorItem.class);

            this.level.getLevelSettings().getParticleController().startSpawnParticles(this.player);
            return CompletableFuture.completedFuture(null);
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
    public int getFallHeight() {
        return this.getFallHeight(this.testingActivity == null);
    }

    @Override
    public void onPlayerFall() {
        if (this.testingActivity != null) {
            this.testingActivity.onPlayerFall();
        } else {
            TeleportUtils.teleportAsync(this.getPlugin(), this.player, this.level.getSpawn());
        }
    }

    @Override
    public @NonNull CompletableFuture<Void> endActivity() {
        CompletableFuture<Void> cf;
        if (this.testingActivity != null) cf = this.testingActivity.endActivity();
        else cf = CompletableFuture.completedFuture(null);

        CompletableFuture<Void> result = new CompletableFuture<>();
        cf.thenAccept(unused -> {
            this.player.setGameMode(GameMode.ADVENTURE);
            this.player.getInventory().clear();

            TeleportUtils.teleportAsync(this.plugin, this.player, Settings.getLobbySpawn())
            .thenAccept(success -> {
                if (!success) {
                    result.complete(null);
                    return;
                }

                this.level.getLevelSettings().getParticleController().stopSpawnParticles();
                this.level.setEditing(false);

                this.player.sendMessage(
                    "Редактор уровня \"" + this.level.getDisplayName() + "\" успешно остановлен");

                this.plugin.get(LevelsManager.class).saveLevelSettingsAndBlocks(this.level);
                result.complete(null);
            });
        });
        return result;
    }

    public void startTesting() {
        if (this.testingActivity != null) throw new IllegalArgumentException("Testing already started");

        PlayActivity.createAsync(this.plugin, this.player, this.level.getUniqueId(), true)
            .thenAccept(playActivity -> {
                if (playActivity == null) {
                    this.player.sendMessage("Не удалось войти в режим тестирования");
                    return;
                }

                this.creativeInventoryContents = this.player.getInventory().getContents();
                this.player.getInventory().clear();

                this.level.getLevelSettings().getParticleController().stopSpawnParticlesForPlayer(this.player);
                this.testingActivity = playActivity;
                this.testingActivity.startActivity();

                this.player.sendMessage("Вы вошли в режим тестирования");
            });
    }

    public void endTesting() {
        if (this.testingActivity == null) throw new IllegalArgumentException("Testing not started");

        TeleportUtils.teleportAsync(this.plugin, this.player, this.level.getSpawn())
            .thenAccept(success -> {
                if (!success) {
                    this.player.sendMessage("Не удалось покинуть режим тестирования");
                    return;
                }

                this.testingActivity.endActivity();
                this.testingActivity = null;
                this.startActivity();

                this.player.getInventory().setContents(this.creativeInventoryContents);

                this.player.sendMessage("Вы покинули режим тестирования");
            });
    }

    public boolean isTesting() {
        return this.testingActivity != null;
    }

    @NonNull
    private CompletableFuture<Boolean> start() {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        TeleportUtils.teleportAsync(this.plugin, this.player, this.level.getSpawn())
            .thenAccept(success -> {
                if (!success) {
                    result.complete(false);
                    return;
                }

                this.player.sendMessage("Редактор уровня \"" + this.level.getDisplayName() + "\" успешно запущен");

                this.level.getLevelSettings().updateParticleLocations();

                this.level.setEditing(true);
                result.complete(true);
            });
        return result;
    }
}
