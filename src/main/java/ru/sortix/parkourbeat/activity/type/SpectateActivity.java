package ru.sortix.parkourbeat.activity.type;

import lombok.NonNull;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.activity.UserActivity;
import ru.sortix.parkourbeat.levels.Level;
import ru.sortix.parkourbeat.world.TeleportUtils;

import java.util.concurrent.CompletableFuture;

public class SpectateActivity extends UserActivity {
    private SpectateActivity(@NonNull ParkourBeat plugin, @NonNull Player player, @NonNull Level level) {
        super(plugin, player, level);
    }

    public static CompletableFuture<SpectateActivity> createAsync(
        @NonNull ParkourBeat plugin, @NonNull Player player, @NonNull Level level) {
        return CompletableFuture.completedFuture(new SpectateActivity(plugin, player, level));
    }

    @Override
    public @NonNull CompletableFuture<Void> startActivity() {
        this.player.setGameMode(GameMode.SPECTATOR);
        this.player.sendMessage("Вы наблюдаете за уровнем \"" + this.level.getDisplayName() + "\"");
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void on(@NonNull PlayerResourcePackStatusEvent event) {
    }

    @Override
    public void on(@NonNull PlayerMoveEvent event) {
    }

    @Override
    public void onTick() {
    }

    @Override
    public void on(@NonNull PlayerToggleSprintEvent event) {
    }

    @Override
    public void on(@NonNull PlayerToggleSneakEvent event) {
    }

    @Override
    public int getFallHeight() {
        return this.getFallHeight(false);
    }

    @Override
    public void onPlayerFall() {
        TeleportUtils.teleportAsync(this.getPlugin(), this.player, this.level.getSpawn());
    }

    @Override
    public @NonNull CompletableFuture<Void> endActivity() {
        this.player.setGameMode(GameMode.ADVENTURE);
        return CompletableFuture.completedFuture(null);
    }
}
