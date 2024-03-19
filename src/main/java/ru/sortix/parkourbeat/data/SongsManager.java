package ru.sortix.parkourbeat.data;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import lombok.NonNull;
import me.bomb.amusic.AMusic;
import org.bukkit.plugin.java.JavaPlugin;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.levels.settings.Song;
import ru.sortix.parkourbeat.lifecycle.PluginManager;

public class SongsManager implements PluginManager {
    private final Logger logger;
    private final Map<String, Song> allSongs;
    private final Path path;

    public SongsManager(@NonNull ParkourBeat plugin) {
        this.logger = plugin.getLogger();
        this.allSongs = new LinkedHashMap<>();
        this.path = new File(JavaPlugin.getPlugin(AMusic.class).getDataFolder(), "Music").toPath();
        this.reloadSongs();
    }

    public void reloadSongs() {
        this.allSongs.clear();
        try (Stream<Path> paths = Files.walk(this.path)) {
            paths.filter(Files::isRegularFile).forEach(file -> {
                String songPlaylist = file.getParent().getFileName().toString();
                String songName = file.getFileName().toString();
                int dotIndex = songName.lastIndexOf(".");
                if (dotIndex > 0) {
                    songName = songName.substring(0, dotIndex);
                }
                this.allSongs.put(songName, new Song(songPlaylist, songName));
            });
        } catch (Exception e) {
            this.logger.log(Level.SEVERE, "Unable to reload songs", e);
        }
    }

    @NonNull public Collection<Song> getAllSongs() {
        return this.allSongs.values();
    }

    @Override
    public void disable() {}
}
