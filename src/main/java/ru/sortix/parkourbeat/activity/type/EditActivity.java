package ru.sortix.parkourbeat.activity.type;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.ItemStack;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.activity.ActivityManager;
import ru.sortix.parkourbeat.activity.UserActivity;
import ru.sortix.parkourbeat.game.Game;
import ru.sortix.parkourbeat.item.ItemsManager;
import ru.sortix.parkourbeat.item.editor.EditorItem;
import ru.sortix.parkourbeat.item.editor.type.EditTrackPointsItem;
import ru.sortix.parkourbeat.levels.Level;
import ru.sortix.parkourbeat.levels.LevelsManager;
import ru.sortix.parkourbeat.physics.CustomPhysicsManager;
import ru.sortix.parkourbeat.world.TeleportUtils;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class EditActivity extends UserActivity {
    @Getter
    @Setter
    private @NonNull Color currentColor = EditTrackPointsItem.DEFAULT_PARTICLES_COLOR;
    @Getter
    @Setter
    private double currentHeight = 0;
    private @Nullable PlayActivity testingActivity = null;
    private @Nullable Location creativePosition = null;
    private @Nullable ItemStack[] creativeInventoryContents = null;
    private final CustomPhysicsManager physicsManager;

    private EditActivity(@NonNull ParkourBeat plugin, @NonNull Player player, @NonNull Level level) {
        super(plugin, player, level);
        this.player.sendMessage(Component.text("Редактор уровня \"", NamedTextColor.WHITE)
            .append(this.level.getDisplayName())
            .append(Component.text("\" успешно запущен", NamedTextColor.WHITE))
        );
        this.level.getLevelSettings().updateParticleLocations();
        this.level.setEditing(true);
        this.physicsManager = plugin.get(CustomPhysicsManager.class);
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
            result.complete(new EditActivity(plugin, player, level));
        });
        return result;
    }

    @Override
    public void startActivity() {
        physicsManager.addPlayer(player, level);
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
    public void endActivity() {
        physicsManager.purgePlayer(player);
        if (this.testingActivity != null) this.testingActivity.endActivity();

        this.player.setGameMode(GameMode.ADVENTURE);
        this.player.getInventory().clear();

        this.level.getLevelSettings().getParticleController().stopSpawnParticles();

        this.player.sendMessage(Component.text("Редактор уровня \"", NamedTextColor.WHITE)
            .append(this.level.getDisplayName())
            .append(Component.text("\" успешно остановлен", NamedTextColor.WHITE))
        );

        if (this.level.isEditing()) { // Prevent double saving after LevelsManager disabling
            this.level.setEditing(false);
            this.plugin.get(LevelsManager.class).saveLevelSettingsAndBlocks(this.level);
        }
    }

    public void startTesting() {
        if (this.testingActivity != null) throw new IllegalArgumentException("Testing already started");

        PlayActivity.createAsync(this.plugin, this.player, this.level.getUniqueId(), true)
            .thenAccept(playActivity -> {
                if (playActivity == null) {
                    this.player.sendMessage("Не удалось войти в режим тестирования");
                    return;
                }

                this.creativePosition = this.player.getLocation();
                TeleportUtils.teleportAsync(this.plugin, this.player, this.level.getSpawn()).thenAccept(success -> {
                    if (!success) {
                        this.player.sendMessage(Component.text("Не удалось войти в режим тестирования"));
                        return;
                    }

                    this.creativeInventoryContents = this.player.getInventory().getContents();
                    this.player.getInventory().clear();

                    this.level.getLevelSettings().getParticleController().stopSpawnParticlesForPlayer(this.player);

                    this.testingActivity = playActivity;
                    this.testingActivity.startActivity();

                    this.player.sendActionBar(Component.text("Вы вошли в режим тестирования"));
                });
            });
    }

    public void endTesting() {
        if (this.testingActivity == null) throw new IllegalArgumentException("Testing not started");

        TeleportUtils.teleportAsync(
            this.plugin,
            this.player,
            this.creativePosition == null ? this.level.getSpawn() : this.creativePosition
        ).thenAccept(success -> {
            this.creativePosition = null;

            if (!success) {
                this.player.sendMessage(Component.text("Не удалось покинуть режим тестирования"));
                return;
            }

            this.testingActivity.endActivity();
            this.testingActivity = null;
            this.startActivity();

            this.player.getInventory().setContents(this.creativeInventoryContents);
            this.creativeInventoryContents = null;

            this.player.sendActionBar(Component.text("Вы покинули режим тестирования"));
        });
    }

    public boolean isTesting() {
        return this.testingActivity != null;
    }
}
