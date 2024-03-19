package ru.sortix.parkourbeat.inventory.type;

import java.util.Arrays;
import lombok.NonNull;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.commands.CommandPlay;
import ru.sortix.parkourbeat.inventory.PaginatedMenu;
import ru.sortix.parkourbeat.inventory.RegularItems;
import ru.sortix.parkourbeat.item.ItemUtils;
import ru.sortix.parkourbeat.levels.LevelsManager;
import ru.sortix.parkourbeat.levels.settings.GameSettings;

public class LevelsListMenu extends PaginatedMenu<ParkourBeat, GameSettings> {
    public LevelsListMenu(@NonNull ParkourBeat plugin) {
        super(plugin, 6, "Уровни", 0, 5 * 9);
        this.setItems(this.plugin.get(LevelsManager.class).getAvailableLevelsSettings());
    }

    @Override
    @SuppressWarnings("deprecation")
    protected @NonNull ItemStack createItemDisplay(@NonNull GameSettings gameSettings) {
        return ItemUtils.modifyMeta(new ItemStack(Material.PAPER), meta -> {
            meta.setDisplayName(ChatColor.GOLD + gameSettings.getLevelName());
            meta.setLore(Arrays.asList(
                    ChatColor.YELLOW + "Создатель: " + gameSettings.getOwnerName(),
                    ChatColor.YELLOW + "ID: " + gameSettings.getLevelId(),
                    ChatColor.YELLOW + "Трек: "
                            + (gameSettings.getSong() == null
                                    ? "отсутствует"
                                    : gameSettings.getSong().getSongName())));
        });
    }

    @Override
    protected void onPageDisplayed() {
        this.setNextPageItem(6, 1);
        this.setItem(6, 5, RegularItems.closeInventory(), HumanEntity::closeInventory);
        this.setPreviousPageItem(6, 9);
    }

    @Override
    protected void onClick(@NonNull Player player, @NonNull GameSettings gameSettings) {
        CommandPlay.startPlaying(this.plugin, player, gameSettings.getLevelId());
    }
}
