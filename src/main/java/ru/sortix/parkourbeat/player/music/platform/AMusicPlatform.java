package ru.sortix.parkourbeat.player.music.platform;

import lombok.NonNull;
import me.bomb.amusic.AMusic;
import me.bomb.amusic.bukkit.AMusicBukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.sortix.parkourbeat.player.music.MusicTrack;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class AMusicPlatform extends MusicPlatform {
    private final AMusic aMusic = AMusic.API();
    private final Path allTracksPath = new File(JavaPlugin.getPlugin(AMusicBukkit.class).getDataFolder(), "Music").toPath();

    @NonNull
    @Override
    protected List<MusicTrack> loadAllTracksFromStorage() throws Exception {
        List<MusicTrack> result = new ArrayList<>();
        if (false) { // Valid non-cached tracks not present in AMusic.getPlaylists() in AMusic v0.12
            for (String trackIdAndName : this.aMusic.getPlaylists()) {
                result.add(new MusicTrack(this, trackIdAndName, trackIdAndName));
            }
        } else {
            try (Stream<Path> paths = Files.list(this.allTracksPath)) {
                paths.filter(Files::isDirectory).forEach(file -> {
                    String trackIdAndName = file.getFileName().toString();
                    result.add(new MusicTrack(this, trackIdAndName, trackIdAndName));
                });
            }
        }
        return result;
    }

    @Override
    protected @Nullable MusicTrack loadTrackFromStorage(@NonNull String trackId) throws Exception {
        if (false) { // Valid non-cached tracks not present in AMusic.getPlaylists() in AMusic v0.12
            for (String trackIdAndName : this.aMusic.getPlaylists()) {
                if (!trackIdAndName.equals(trackId)) continue;
                return new MusicTrack(this, trackIdAndName, trackIdAndName);
            }
        } else {
            Path trackPath = this.allTracksPath.resolve(trackId);
            if (Files.isDirectory(trackPath)) {
                return new MusicTrack(this, trackId, trackId);
            }
        }
        return null;
    }

    @Override
    protected void loadOrUpdateResourcepackFile(@NonNull MusicTrack track) throws Exception {
        this.aMusic.loadPack(null, track.getId(), true);
    }

    @Override
    public void setResourcepackTrack(@NonNull Player player, @NonNull MusicTrack track) throws Exception {
        this.aMusic.loadPack(player.getUniqueId(), track.getId(), false);
    }

    @Nullable
    @Override
    public MusicTrack getResourcepackTrack(@NonNull Player player) {
        String trackId = this.aMusic.getPackName(player.getUniqueId());
        if (trackId == null) return null;
        return this.getTrackById(trackId);
    }

    @Override
    public void disableRepeatMode(@NonNull Player player) {
        this.aMusic.setRepeatMode(player.getUniqueId(), null);
    }

    @Override
    public void startPlayingTrackFull(@NonNull Player player) {
        this.aMusic.playSound(player.getUniqueId(), "track");
    }

    @Override
    public void stopPlayingTrackFull(@NonNull Player player) {
        this.aMusic.stopSound(player.getUniqueId());
    }

    @Override
    public void startPlayingTrackPiece(@NonNull Player player, int trackPieceNumber) {
        this.aMusic.playSound(player.getUniqueId(), String.valueOf(trackPieceNumber));
    }

    @Override
    public void stopPlayingTrackPiece(@NonNull Player player, int trackPieceNumber) {
        this.aMusic.stopSound(player.getUniqueId());
    }
}
