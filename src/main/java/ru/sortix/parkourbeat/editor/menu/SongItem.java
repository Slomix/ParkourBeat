package ru.sortix.parkourbeat.editor.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import ru.sortix.parkourbeat.levels.settings.GameSettings;
import ru.sortix.parkourbeat.utils.Heads;

public class SongItem extends SongMenuItem {

    private static final ItemStack dummy =
            Heads.getHeadByHash("f22e40b4bfbcc0433044d86d67685f0567025904271d0a74996afbe3f9be2c0f");

    private final String name;
    private final String playlist;
    private final GameSettings gameSettings;
    private final Player player;
    private final ItemStack item;

    public SongItem(int slot, String playlist, String name, Player player, GameSettings gameSettings) {
        super(slot);
        this.name = name;
        this.playlist = playlist;
        this.gameSettings = gameSettings;
        this.player = player;
        this.item = createSongItem(name);
    }

    private static ItemStack createSongItem(String name) {
        ItemStack item = dummy.clone();
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public ItemStack getItemStack() {
        return item;
    }

    @Override
    public void onClick() {
        gameSettings.setSong(playlist, name);
        player.sendMessage("Вы успешно установили песню: " + name);
    }
}
