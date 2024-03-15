package ru.sortix.parkourbeat.inventory.editor;

import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import ru.sortix.parkourbeat.levels.settings.GameSettings;
import ru.sortix.parkourbeat.levels.settings.Song;
import ru.sortix.parkourbeat.utils.Heads;

public class SongItem extends SongMenuItem {

    private static final ItemStack dummy =
            Heads.getHeadByHash("f22e40b4bfbcc0433044d86d67685f0567025904271d0a74996afbe3f9be2c0f");

    private final Song song;
    private final GameSettings gameSettings;
    private final ItemStack item;

    public SongItem(int slot, @NonNull Song song, GameSettings gameSettings) {
        super(slot);
        this.song = song;
        this.gameSettings = gameSettings;
        this.item = createSongItem(song);
    }

    @NonNull private static ItemStack createSongItem(@NonNull Song song) {
        ItemStack item = dummy.clone();
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setDisplayName(song.getSongName());
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public ItemStack getItemStack() {
        return item;
    }

    @Override
    public void onClick(@NonNull Player player) {
        this.gameSettings.setSong(this.song);
        player.sendMessage("Вы успешно установили песню: " + this.song.getSongName());
    }
}
