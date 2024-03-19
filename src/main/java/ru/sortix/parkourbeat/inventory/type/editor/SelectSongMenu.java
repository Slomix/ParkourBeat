package ru.sortix.parkourbeat.inventory.type.editor;

import lombok.NonNull;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.data.SongsManager;
import ru.sortix.parkourbeat.inventory.PaginatedMenu;
import ru.sortix.parkourbeat.inventory.RegularItems;
import ru.sortix.parkourbeat.item.ItemUtils;
import ru.sortix.parkourbeat.levels.Level;
import ru.sortix.parkourbeat.levels.settings.Song;
import ru.sortix.parkourbeat.utils.Heads;

public class SelectSongMenu extends PaginatedMenu<ParkourBeat, Song> {
    public static final ItemStack NOTE_HEAD =
            Heads.getHeadByHash("f22e40b4bfbcc0433044d86d67685f0567025904271d0a74996afbe3f9be2c0f");

    private final Level level;

    public SelectSongMenu(@NonNull ParkourBeat plugin, @NonNull Level level) {
        super(plugin, 6, "Выбрать музыку", 0, 5 * 9);
        this.level = level;
        this.setItems(plugin.get(SongsManager.class).getAllSongs());
    }

    @Override
    protected @NonNull ItemStack createItemDisplay(@NonNull Song song) {
        //noinspection deprecation
        return ItemUtils.modifyMeta(NOTE_HEAD.clone(), meta -> meta.setDisplayName(song.getSongName()));
    }

    @Override
    protected void onPageDisplayed() {
        this.setNextPageItem(6, 1);
        this.setItem(6, 5, RegularItems.closeInventory(), HumanEntity::closeInventory);
        this.setPreviousPageItem(6, 9);
    }

    @Override
    protected void onClick(@NonNull Player player, @NonNull Song song) {
        this.level.getLevelSettings().getGameSettings().setSong(song);
        player.sendMessage("Вы успешно установили песню: " + song.getSongName());
        player.closeInventory();
    }
}
