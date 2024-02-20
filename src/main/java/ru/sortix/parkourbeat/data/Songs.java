package ru.sortix.parkourbeat.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

public class Songs {

    private final Map<String, String> allSongs;
    private final Path path;

    public Songs(Path path) {
        allSongs = new TreeMap<>();
        this.path = path;
        reload();
    }

    public void reload() {
        try (Stream<Path> paths = Files.walk(path)) {
            paths
                    .filter(Files::isRegularFile)
                    .forEach(
                            file -> {
                                String parent = file.getParent().getFileName().toString();
                                String filename = file.getFileName().toString();
                                int dotIndex = filename.lastIndexOf(".");
                                if (dotIndex > 0) {
                                    filename = filename.substring(0, dotIndex);
                                }
                                allSongs.put(filename, parent);
                            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Set<String> getAllSongs() {
        return allSongs.keySet();
    }

    public String getSongPlaylist(String name) {
        return allSongs.get(name);
    }
}
