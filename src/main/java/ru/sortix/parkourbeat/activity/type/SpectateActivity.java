package ru.sortix.parkourbeat.activity.type;

import java.util.concurrent.CompletableFuture;
import lombok.NonNull;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.*;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.activity.UserActivity;
import ru.sortix.parkourbeat.levels.Level;
import ru.sortix.parkourbeat.utils.TeleportUtils;

public class SpectateActivity extends UserActivity {
    public static CompletableFuture<SpectateActivity> createAsync(
            @NonNull ParkourBeat plugin, @NonNull Player player, @NonNull Level level) {
        return CompletableFuture.completedFuture(new SpectateActivity(plugin, player, level));
    }

    private SpectateActivity(@NonNull ParkourBeat plugin, @NonNull Player player, @NonNull Level level) {
        super(plugin, player, level);
    }

    @Override
    public void startActivity() {
        this.player.setGameMode(GameMode.SPECTATOR);
        this.player.sendMessage("Вы наблюдаете за уровнем \"" + this.level.getDisplayName() + "\"");
    }

    @Override
    public void on(@NonNull PlayerResourcePackStatusEvent event) {}

    @Override
    public void on(@NonNull PlayerMoveEvent event) {}

    @Override
    public void onTick() {}

    @Override
    public void on(@NonNull PlayerToggleSprintEvent event) {}

    @Override
    public void on(@NonNull PlayerToggleSneakEvent event) {}

    @Override
    public void on(@NonNull PlayerInteractEvent event) {
        event.setUseInteractedBlock(Event.Result.DENY);
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
    public void endActivity() {
        this.player.setGameMode(GameMode.ADVENTURE);
    }
}
