package ru.sortix.parkourbeat.editor.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import ru.sortix.parkourbeat.levels.settings.GameSettings;
import ru.sortix.parkourbeat.utils.SkullTextures;

public class SongItem extends SongMenuItem {

    private static final ItemStack dummy;

    static {
        dummy = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta dummyMeta = (SkullMeta) dummy.getItemMeta();
        SkullTextures.setSkullTexture(dummyMeta, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjIyZTQwYjRiZmJjYzA0MzMwNDRkODZkNjc2ODVmMDU2NzAyNTkwNDI3MWQwYTc0OTk2YWZiZTNmOWJlMmMwZiJ9fX0=");
        dummy.setItemMeta(dummyMeta);
    }

    private final String name;
    private final String playlist;
    private final GameSettings gameSettings;
    private ItemStack item;
    private final Player player;

    public SongItem(int slot, String playlist, String name, Player player, GameSettings gameSettings) {
        super(slot);
        this.name = name;
        this.playlist = playlist;
        this.gameSettings = gameSettings;
        this.player = player;
        this.item = createSongItem(name);
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

    private static ItemStack createSongItem(String name) {
        ItemStack item = dummy.clone();
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }
}
