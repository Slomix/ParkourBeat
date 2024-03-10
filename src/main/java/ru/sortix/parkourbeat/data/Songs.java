package ru.sortix.parkourbeat.data;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import lombok.NonNull;
import me.bomb.amusic.AMusic;
import org.bukkit.plugin.java.JavaPlugin;

public class Songs {
    private final Logger logger;
    private final Map<String, String> allSongs;
    private final Path path;

    public Songs(@NonNull Logger logger) {
        this.logger = logger;
        this.allSongs = new TreeMap<>();
        this.path = new File(JavaPlugin.getPlugin(AMusic.class).getDataFolder(), "Music").toPath();
        reload();
    }

    public void reload() {
        try (Stream<Path> paths = Files.walk(path)) {
            paths.filter(Files::isRegularFile).forEach(file -> {
                String parent = file.getParent().getFileName().toString();
                String filename = file.getFileName().toString();
                int dotIndex = filename.lastIndexOf(".");
                if (dotIndex > 0) {
                    filename = filename.substring(0, dotIndex);
                }
                this.allSongs.put(filename, parent);
            });
        } catch (Exception e) {
            this.logger.log(Level.SEVERE, "Unable to reload songs", e);
        }
    }

    public Set<String> getAllSongs() {
        return this.allSongs.keySet();
    }

    public String getSongPlaylist(String name) {
        return this.allSongs.get(name);
    }
}
