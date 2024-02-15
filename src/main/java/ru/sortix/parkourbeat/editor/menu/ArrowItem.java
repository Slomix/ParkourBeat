package ru.sortix.parkourbeat.editor.menu;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import ru.sortix.parkourbeat.utils.SkullTextures;

public class ArrowItem extends SongMenuItem {

    private static ItemStack rightArrow;
    private static ItemStack leftArrow;

    static {
        rightArrow = new ItemStack(Material.SKULL, 1);
        SkullMeta arrowMeta = (SkullMeta) rightArrow.getItemMeta();
        arrowMeta.setDisplayName("Следующая страница");
        SkullTextures.setSkullTexture(arrowMeta, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTNmYzUyMjY0ZDhhZDllNjU0ZjQxNWJlZjAxYTIzOTQ3ZWRiY2NjY2Y2NDkzNzMyODliZWE0ZDE0OTU0MWY3MCJ9fX0=");
        rightArrow.setItemMeta(arrowMeta);
        leftArrow = new ItemStack(Material.SKULL, 1);
        arrowMeta = (SkullMeta) leftArrow.getItemMeta();
        arrowMeta.setDisplayName("Предыдущая страница");
        SkullTextures.setSkullTexture(arrowMeta, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWYxMzNlOTE5MTlkYjBhY2VmZGMyNzJkNjdmZDg3YjRiZTg4ZGM0NGE5NTg5NTg4MjQ0NzRlMjFlMDZkNTNlNiJ9fX0=");
        leftArrow.setItemMeta(arrowMeta);
    }

    private final boolean right;
    private final SongMenu menu;

    public ArrowItem(int slot, SongMenu menu, boolean right) {
        super(slot);
        this.right = right;
        this.menu = menu;
    }

    @Override
    public ItemStack getItemStack() {
        if (right) {
            return rightArrow;
        } else {
            return leftArrow;
        }
    }

    @Override
    public void onClick() {
        if (right) {
            menu.next();
        } else {
            menu.previous();
        }
    }

}
