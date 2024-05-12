package ru.sortix.parkourbeat.player.music;

import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import ru.sortix.parkourbeat.player.music.platform.MusicPlatform;

import java.util.logging.Level;

public class MusicTrack {

    private final @NonNull MusicPlatform platform;
    private final @NonNull String trackId;
    private final @NonNull String trackName;

    public MusicTrack(@NonNull MusicPlatform platform, @NonNull String trackId, @NonNull String trackName) {
        this.platform = platform;
        this.trackId = trackId;
        this.trackName = trackName;
    }

    @NonNull
    public String getId() {
        return this.trackId;
    }

    @NonNull
    public String getName() {
        return this.trackName;
    }

    public boolean isStillAvailable() {
        return this.platform.getTrackById(this.getId()) != null;
    }

    public boolean isResourcepackCurrentlySet(@NonNull Player player) {
        MusicTrack currantTrack = this.platform.getResourcepackTrack(player);
        return currantTrack != null && this.trackId.equals(currantTrack.trackId);
    }

    public boolean setResourcepackAsync(@NonNull Plugin plugin, @NonNull Player player) {
        if (!this.isStillAvailable()) return false;

        try {
            this.platform.setResourcepackTrack(player, this);
            return true;
        } catch (Throwable t) {
            plugin.getLogger().log(Level.SEVERE,
                "Не удалось запустить песню \"" + this.getName() + "\" (" + this.getId() + ") игроку " + player.getName(), t);
            return false;
        }
    }
}
