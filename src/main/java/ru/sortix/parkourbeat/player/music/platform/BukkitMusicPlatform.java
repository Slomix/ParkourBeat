package ru.sortix.parkourbeat.player.music.platform;

import lombok.NonNull;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.player.music.MusicTrack;

import javax.annotation.Nullable;
import java.util.List;

public class BukkitMusicPlatform extends MusicPlatform {
    private static final String TRACK_FULL_SOUND_NAME = "parkourbeat.full";
    private static final String TRACK_PIECE_SOUND_NAME_PREFIX = "parkourbeat.piece";

    @Override
    protected @NonNull List<MusicTrack> loadAllTracksFromStorage() {
        throw new UnsupportedOperationException("Not implemented yet"); // TODO
    }

    @Override
    protected @NonNull MusicTrack loadTrackFromStorage(@NonNull String trackId) {
        throw new UnsupportedOperationException("Not implemented yet"); // TODO
    }

    @Override
    protected void loadOrUpdateResourcepackFile(@NonNull MusicTrack track) {
        throw new UnsupportedOperationException("Not implemented yet"); // TODO
    }

    @Override
    public void setResourcepackTrack(@NonNull Player player, @NonNull MusicTrack track) {
        throw new UnsupportedOperationException("Not implemented yet"); // TODO
    }

    @Nullable
    @Override
    public MusicTrack getResourcepackTrack(@NonNull Player player) {
        throw new UnsupportedOperationException("Not implemented yet"); // TODO
    }

    @Override
    public void disableRepeatMode(@NonNull Player player) {
    }

    @Override
    public void startPlayingTrackFull(@NonNull Player player) {
        player.playSound(player.getLocation(),
            TRACK_FULL_SOUND_NAME,
            SoundCategory.MUSIC, 1.0E9f, 1.0f);
    }

    @Override
    public void stopPlayingTrackFull(@NonNull Player player) {
        player.stopSound(TRACK_FULL_SOUND_NAME);
    }

    @Override
    public void startPlayingTrackPiece(@NonNull Player player, int trackPieceNumber) {
        player.playSound(player.getLocation(),
            TRACK_PIECE_SOUND_NAME_PREFIX + trackPieceNumber,
            SoundCategory.MUSIC, 1.0E9f, 1.0f);
    }

    @Override
    public void stopPlayingTrackPiece(@NonNull Player player, int trackPieceNumber) {
        player.stopSound(TRACK_PIECE_SOUND_NAME_PREFIX + trackPieceNumber);
    }
}
