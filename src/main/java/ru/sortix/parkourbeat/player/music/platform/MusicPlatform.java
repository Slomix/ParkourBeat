package ru.sortix.parkourbeat.player.music.platform;

import lombok.NonNull;
import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.player.music.MusicTrack;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class MusicPlatform {
    private final Map<String, MusicTrack> tracksById = new HashMap<>();

    public void reloadAllTracksList() throws Exception {
        this.tracksById.clear();
        for (MusicTrack musicTrack : this.loadAllTracksFromStorage()) {
            this.tracksById.put(musicTrack.getId(), musicTrack);
        }
    }

    public final @NonNull List<MusicTrack> getAllTracks() {
        return List.copyOf(this.tracksById.values());
    }

    @Nullable
    public final MusicTrack getTrackById(@NonNull String trackId) {
        return this.tracksById.get(trackId);
    }

    @Nullable
    public final MusicTrack tryToLoadOrUpdateResourcepackFile(@NonNull String trackId) throws Exception {
        MusicTrack track = this.loadTrackFromStorage(trackId);
        if (track == null) return null;
        this.loadOrUpdateResourcepackFile(track);
        this.tracksById.put(track.getId(), track);
        return track;
    }

    @NonNull
    protected abstract List<MusicTrack> loadAllTracksFromStorage() throws Exception;

    @Nullable
    protected abstract MusicTrack loadTrackFromStorage(@NonNull String trackId) throws Exception;

    protected abstract void loadOrUpdateResourcepackFile(@NonNull MusicTrack track) throws Exception;

    public abstract void setResourcepackTrack(@NonNull Player player, @NonNull MusicTrack track) throws Exception;

    @Nullable
    public abstract MusicTrack getResourcepackTrack(@NonNull Player player);

    public abstract void disableRepeatMode(@NonNull Player player);

    public abstract void startPlayingTrackFull(@NonNull Player player);

    public abstract void stopPlayingTrackFull(@NonNull Player player);

    public abstract void startPlayingTrackPiece(@NonNull Player player, int trackPieceNumber);

    public abstract void stopPlayingTrackPiece(@NonNull Player player, int trackPieceNumber);
}
