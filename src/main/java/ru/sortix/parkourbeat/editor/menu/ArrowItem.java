package ru.sortix.parkourbeat.editor.menu;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import ru.sortix.parkourbeat.utils.Heads;

public class ArrowItem extends SongMenuItem {

    private static final ItemStack rightArrow =
            Heads.getHeadByHash("e3fc52264d8ad9e654f415bef01a23947edbccccf649373289bea4d149541f70");
    private static final ItemStack leftArrow =
            Heads.getHeadByHash("5f133e91919db0acefdc272d67fd87b4be88dc44a958958824474e21e06d53e6");

    static {
        SkullMeta arrowMeta = (SkullMeta) rightArrow.getItemMeta();
        arrowMeta.setDisplayName("Следующая страница");
        rightArrow.setItemMeta(arrowMeta);

        arrowMeta = (SkullMeta) leftArrow.getItemMeta();
        arrowMeta.setDisplayName("Предыдущая страница");
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
