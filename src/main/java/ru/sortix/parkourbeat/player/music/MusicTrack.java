package ru.sortix.parkourbeat.player.music;

import lombok.NonNull;
import me.bomb.amusic.AMusic;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

public class MusicTrack {

    private final @NonNull String playlist;

    MusicTrack(@NonNull String playlist) {
        this.playlist = playlist;
    }

    @NonNull
    public String getUniqueId() {
        return this.playlist;
    }

    @NonNull
    public String getName() {
        return this.playlist;
    }

    public boolean isAvailable() {
        if (MusicTracksManager.LEGACY_MODE) return true;
        return AMusic.getPlaylists().contains(this.playlist);
    }

    public boolean isResourcepackCurrentlySet(@NonNull Player player) {
        String currentPlayList = AMusic.getPackName(player); // nullable
        return this.playlist.equals(currentPlayList);
    }

    public boolean setResourcepackAsync(@NonNull Plugin plugin, @NonNull Player player) {
        if (!this.isAvailable()) return false;

        try {
            AMusic.loadPack(player, this.playlist, false);
            return true;
        } catch (Throwable t) {
            plugin.getLogger().log(Level.SEVERE,
                "Не удалось запустить песню " + this.getName() + " игроку " + player.getName(), t);
            return false;
        }
    }
}
