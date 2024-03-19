package ru.sortix.parkourbeat.inventory.type.editor;

import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.data.SongsManager;
import ru.sortix.parkourbeat.inventory.ParkourBeatInventory;
import ru.sortix.parkourbeat.inventory.RegularItems;
import ru.sortix.parkourbeat.item.ItemUtils;
import ru.sortix.parkourbeat.levels.Level;
import ru.sortix.parkourbeat.levels.settings.GameSettings;
import ru.sortix.parkourbeat.levels.settings.Song;
import ru.sortix.parkourbeat.utils.Heads;

public class SelectSongMenu extends ParkourBeatInventory {
    private static final ItemStack NOTE_HEAD =
            Heads.getHeadByHash("f22e40b4bfbcc0433044d86d67685f0567025904271d0a74996afbe3f9be2c0f");

    private final int maxPage;
    private final SongsManager songsManager;
    private final GameSettings gameSettings;
    private int page = 0;

    public SelectSongMenu(@NonNull ParkourBeat plugin, @NonNull Level level) {
        super(plugin, 6, "Список песен");
        this.songsManager = plugin.get(SongsManager.class);
        this.maxPage = (this.songsManager.getAllSongs().size() + 1) / 45;
        this.gameSettings = level.getLevelSettings().getGameSettings();
        this.initPage(0);
    }

    private void initPage(int page) {
        this.clearInventory();

        List<String> songs = new ArrayList<>(this.songsManager.getAllSongs());
        for (int slot = 0; slot < 45; slot++) {
            int songID = slot + 45 * page;
            if (songID >= songs.size()) continue;

            String songName = songs.get(songID);
            String songPlaylist = this.songsManager.getSongPlaylist(songName);
            Song song = new Song(songPlaylist, songName);

            //noinspection deprecation
            this.setItem(
                    slot,
                    ItemUtils.modifyMeta(NOTE_HEAD.clone(), meta -> meta.setDisplayName(song.getSongName())),
                    player -> {
                        this.gameSettings.setSong(song);
                        player.sendMessage("Вы успешно установили песню: " + song.getSongName());
                    });
        }

        if (page > 0) {
            this.setItem(6, 1, RegularItems.previousPage(), player -> this.previous());
        }

        this.setItem(6, 5, RegularItems.closeInventory(), HumanEntity::closeInventory);

        if (page < this.maxPage) {
            this.setItem(6, 9, RegularItems.nextPage(), player -> this.next());
        }
    }

    public void next() {
        if (this.page < this.maxPage) {
            this.page++;
            this.initPage(this.page);
        }
    }

    public void previous() {
        if (this.page > 0) {
            this.page--;
            this.initPage(this.page);
        }
    }
}
