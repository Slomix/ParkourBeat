package ru.sortix.parkourbeat.inventory;

import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import ru.sortix.parkourbeat.item.ItemUtils;

@SuppressWarnings("deprecation")
public class RegularItems {
    private static final ItemStack closeInventory =
        ItemUtils.modifyMeta(new ItemStack(Material.BARRIER), meta -> meta.setDisplayName("Закрыть инвентарь"));
    private static final ItemStack previousPage = ItemUtils.modifyMeta(
        Heads.getHeadByHash("5f133e91919db0acefdc272d67fd87b4be88dc44a958958824474e21e06d53e6"),
        meta -> meta.setDisplayName("Предыдущая страница"));
    private static final ItemStack nextPage = ItemUtils.modifyMeta(
        Heads.getHeadByHash("e3fc52264d8ad9e654f415bef01a23947edbccccf649373289bea4d149541f70"),
        meta -> meta.setDisplayName("Следующая страница"));

    public static @NonNull ItemStack closeInventory() {
        return closeInventory.clone();
    }

    public static @NonNull ItemStack previousPage() {
        return previousPage.clone();
    }

    public static @NonNull ItemStack nextPage() {
        return nextPage.clone();
    }
}
