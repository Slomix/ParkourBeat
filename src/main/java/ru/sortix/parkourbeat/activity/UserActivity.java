package ru.sortix.parkourbeat.activity;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.*;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.levels.Level;

@Getter
@RequiredArgsConstructor
public abstract class UserActivity {
    protected final @NonNull ParkourBeat plugin;
    protected final @NonNull Player player;
    protected final @NonNull Level level;

    public boolean isValidWorld(@NonNull World world) {
        return this.getLevel().getWorld() == world;
    }

    public abstract void startActivity();

    public abstract void on(@NonNull PlayerResourcePackStatusEvent event);

    public abstract void on(@NonNull PlayerMoveEvent event);

    public abstract void onTick();

    public abstract void on(@NonNull PlayerToggleSprintEvent event);

    public abstract void on(@NonNull PlayerToggleSneakEvent event);

    public abstract int getFallHeight();

    public abstract void onPlayerFall();

    public abstract void endActivity();

    protected int getFallHeight(boolean isEditing) {
        if (isEditing) return -5;
        return this.level.getLevelSettings().getWorldSettings().getMinWorldHeight() - 1;
    }
}
