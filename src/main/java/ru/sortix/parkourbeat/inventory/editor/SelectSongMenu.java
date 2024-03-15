package ru.sortix.parkourbeat.inventory.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.data.SongsManager;
import ru.sortix.parkourbeat.inventory.ParkourBeatInventory;
import ru.sortix.parkourbeat.levels.Level;
import ru.sortix.parkourbeat.levels.settings.GameSettings;
import ru.sortix.parkourbeat.levels.settings.Song;

public class SelectSongMenu extends ParkourBeatInventory {
    private final int maxPage;
    private final SongsManager songsManager;
    private final Map<Integer, SongMenuItem> items;
    private final GameSettings gameSettings;
    private final Inventory inventory;
    private int page = 0;

    public SelectSongMenu(@NonNull ParkourBeat plugin, @NonNull Level level) {
        super(plugin);
        this.inventory = Bukkit.createInventory(this, 54, "Список песен");
        this.songsManager = plugin.get(SongsManager.class);
        this.maxPage = (this.songsManager.getAllSongs().size() + 1) / 45;
        this.items = new HashMap<>();
        this.gameSettings = level.getLevelSettings().getGameSettings();
        this.initPage(0);
    }

    private void initPage(int page) {
        List<String> songs = new ArrayList<>(this.songsManager.getAllSongs());
        for (int slot = 0; slot < 45; slot++) {
            int songID = slot + 45 * page;
            if (songID < songs.size()) {

                String songName = songs.get(songID);
                String songPlaylist = this.songsManager.getSongPlaylist(songName);
                Song song = new Song(songPlaylist, songName);

                SongMenuItem item = new SongItem(slot, song, this.gameSettings);
                inventory.setItem(slot, item.getItemStack());
                items.put(slot, item);
            }
        }

        if (page > 0) {
            SongMenuItem item = new ArrowItem(45, this, false);
            inventory.setItem(45, item.getItemStack());
            items.put(45, item);
        }
        if (page < maxPage) {
            SongMenuItem item = new ArrowItem(45, this, true);
            inventory.setItem(53, item.getItemStack());
            items.put(53, item);
        }
        SongMenuItem exitItem = new ExitItem(49);
        inventory.setItem(49, exitItem.getItemStack());
        items.put(49, exitItem);
    }

    public void onClick(@NonNull Player player, int slot) {
        SongMenuItem item = items.get(slot);
        if (item != null) {
            item.onClick(player);
        }
    }

    @Override
    public void onClose(@NonNull Player player) {}

    public void next() {
        if (page < maxPage) {
            page++;
            initPage(page);
        }
    }

    public void previous() {
        if (page > 0) {
            page--;
            initPage(page);
        }
    }

    @Override
    @NonNull public Inventory getInventory() {
        return this.inventory;
    }
}
