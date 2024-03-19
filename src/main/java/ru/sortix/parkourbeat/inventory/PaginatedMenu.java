package ru.sortix.parkourbeat.inventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class PaginatedMenu<P extends JavaPlugin, Item> extends PluginInventory<P> {
    private final int itemsMinSlotIndex;
    private final int itemsAmountOnPage;
    private final List<Item> allItems;
    private final @Getter int minPageNumber = 1;
    private @Getter int maxPageNumber = -1;
    private int currentPageNumber = -1;

    public PaginatedMenu(
            @NonNull P plugin, int rows, @NonNull String title, int itemsMinSlotIndex, int itemsAmountOnPage) {
        super(plugin, rows, title);
        this.itemsMinSlotIndex = itemsMinSlotIndex;
        this.itemsAmountOnPage = itemsAmountOnPage;
        this.allItems = new ArrayList<>();
    }

    protected void setItems(@NonNull Collection<Item> items) {
        this.allItems.clear();
        this.allItems.addAll(items);
        this.maxPageNumber = ((this.allItems.size() - 1) / this.itemsAmountOnPage) + 1;
        this.displayPage(1);
    }

    private void displayPage(int pageNumber) {
        if (pageNumber < this.minPageNumber || pageNumber > this.maxPageNumber) {
            throw new IllegalArgumentException("Wrong page number: " + pageNumber + ". Allowed: " + this.minPageNumber
                    + "-" + this.maxPageNumber + " (including)");
        }

        if (this.currentPageNumber == pageNumber) return;
        this.currentPageNumber = pageNumber;

        this.clearInventory();

        int firstItemIndex = (pageNumber - 1) * this.itemsAmountOnPage;
        int lastItemIndex = (pageNumber) * this.itemsAmountOnPage - 1;
        lastItemIndex = Math.min(lastItemIndex, this.allItems.size() - 1);

        int slotIndex = this.itemsMinSlotIndex;
        for (int itemIndex = firstItemIndex; itemIndex <= lastItemIndex; itemIndex++) {
            Item item = this.allItems.get(itemIndex);
            this.setItem(slotIndex++, this.createItemDisplay(item), player -> this.onClick(player, item));
        }

        this.onPageDisplayed();
    }

    protected void setPreviousPageItem(int row, int column) {
        if (this.currentPageNumber < this.maxPageNumber) {
            this.setItem(row, column, RegularItems.nextPage(), player -> this.displayPage(this.currentPageNumber + 1));
        }
    }

    protected void setNextPageItem(int row, int column) {
        if (this.currentPageNumber > this.minPageNumber) {
            this.setItem(
                    row, column, RegularItems.previousPage(), player -> this.displayPage(this.currentPageNumber - 1));
        }
    }

    @NonNull protected abstract ItemStack createItemDisplay(@NonNull Item item);

    protected abstract void onPageDisplayed();

    protected abstract void onClick(@NonNull Player player, @NonNull Item item);
}
