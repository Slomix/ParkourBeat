package ru.sortix.parkourbeat.inventory.type.editor;

import lombok.NonNull;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.inventory.Heads;
import ru.sortix.parkourbeat.inventory.PaginatedMenu;
import ru.sortix.parkourbeat.inventory.RegularItems;
import ru.sortix.parkourbeat.inventory.event.ClickEvent;
import ru.sortix.parkourbeat.item.ItemUtils;
import ru.sortix.parkourbeat.levels.Level;
import ru.sortix.parkourbeat.player.music.MusicTrack;
import ru.sortix.parkourbeat.player.music.MusicTracksManager;

public class SelectSongMenu extends PaginatedMenu<ParkourBeat, MusicTrack> {
    public static final ItemStack NOTE_HEAD =
        Heads.getHeadByHash("f22e40b4bfbcc0433044d86d67685f0567025904271d0a74996afbe3f9be2c0f");

    private final Level level;

    public SelectSongMenu(@NonNull ParkourBeat plugin, @NonNull Level level) {
        super(plugin, 6, Component.text("Выбрать музыку"), 0, 5 * 9);
        this.level = level;
        this.updateItems();
    }

    public void updateItems() {
        this.setItems(this.plugin.get(MusicTracksManager.class).getAllTracksModern());
    }

    @Override
    protected @NonNull ItemStack createItemDisplay(@NonNull MusicTrack musicTrack) {
        return ItemUtils.modifyMeta(NOTE_HEAD.clone(), meta -> meta.displayName(Component.text(musicTrack.getName())));
    }

    @Override
    protected void onPageDisplayed() {
        this.setNextPageItem(6, 3);
        this.setItem(6, 5, RegularItems.closeInventory(), clickEvent -> clickEvent
            .getPlayer()
            .closeInventory());
        this.setPreviousPageItem(6, 7);
    }

    @Override
    protected void onClick(@NonNull ClickEvent event, @NonNull MusicTrack musicTrack) {
        this.level.getLevelSettings().getGameSettings().setMusicTrack(musicTrack);
        event.getPlayer().sendMessage("Вы успешно установили песню: " + musicTrack.getName());
        event.getPlayer().closeInventory();
    }
}
