package ru.sortix.parkourbeat.inventory.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.NonNull;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.commands.CommandEdit;
import ru.sortix.parkourbeat.commands.CommandPlay;
import ru.sortix.parkourbeat.inventory.PaginatedMenu;
import ru.sortix.parkourbeat.inventory.RegularItems;
import ru.sortix.parkourbeat.item.ItemUtils;
import ru.sortix.parkourbeat.levels.LevelsManager;
import ru.sortix.parkourbeat.levels.settings.GameSettings;

public class LevelsListMenu extends PaginatedMenu<ParkourBeat, GameSettings> {
    private final boolean editMenu;
    private final boolean displayTechInfo;

    public LevelsListMenu(
            @NonNull ParkourBeat plugin, @Nullable UUID ownerId, boolean bypassForAdmins, boolean displayTechInfo) {
        super(plugin, 6, "Уровни", 0, 5 * 9);
        this.editMenu = ownerId != null;
        this.displayTechInfo = displayTechInfo;
        this.setItems(this.getAvailableLevels(ownerId, bypassForAdmins));
    }

    @NonNull private Collection<GameSettings> getAvailableLevels(@Nullable UUID ownerId, boolean bypassForAdmins) {
        Collection<GameSettings> settings = this.plugin.get(LevelsManager.class).getAvailableLevelsSettings();
        if (ownerId == null) return settings;

        settings = new ArrayList<>(settings);
        Player owner = this.plugin.getServer().getPlayer(ownerId);
        if (owner == null) {
            settings.removeIf(gameSettings -> !gameSettings.isOwner(ownerId));
        } else {
            settings.removeIf(gameSettings -> !gameSettings.isOwner(owner, bypassForAdmins, false));
        }
        return settings;
    }

    @Override
    @SuppressWarnings("deprecation")
    protected @NonNull ItemStack createItemDisplay(@NonNull GameSettings gameSettings) {
        return ItemUtils.modifyMeta(new ItemStack(Material.PAPER), meta -> {
            meta.setDisplayName(ChatColor.GOLD + gameSettings.getLevelName());

            List<String> lore = new ArrayList<>();
            if (this.displayTechInfo) {
                lore.add(ChatColor.YELLOW + "ID: " + gameSettings.getLevelId());
            }
            lore.add(ChatColor.YELLOW + "Создатель: " + gameSettings.getOwnerName());
            lore.add(ChatColor.YELLOW + "Трек: "
                    + (gameSettings.getSong() == null
                            ? "отсутствует"
                            : gameSettings.getSong().getSongName()));
            lore.add(ChatColor.GOLD + "Нажмите, чтобы " + (this.editMenu ? "редактировать" : "начать игру"));
            meta.setLore(lore);
        });
    }

    @Override
    protected void onPageDisplayed() {
        this.setNextPageItem(6, 3);
        this.setItem(
                6, 5, RegularItems.closeInventory(), event -> event.getPlayer().closeInventory());
        this.setPreviousPageItem(6, 7);
        this.setItem(
                6,
                9,
                ItemUtils.create(Material.BOOK, meta -> {
                    if (this.editMenu) {
                        meta.setDisplayName(ChatColor.GOLD + "Все уровни");
                    } else {
                        meta.setDisplayName(ChatColor.GOLD + "Собственные уровни");
                    }
                }),
                event -> {
                    Player player = event.getPlayer();
                    if (this.editMenu) {
                        new LevelsListMenu(this.plugin, null, false, this.displayTechInfo).open(player);
                    } else {
                        new LevelsListMenu(this.plugin, player.getUniqueId(), false, this.displayTechInfo).open(player);
                    }
                });
    }

    @Override
    protected void onClick(@NonNull Player player, @NonNull GameSettings settings) {
        if (this.editMenu) {
            CommandEdit.startEditing(this.plugin, player, settings);
        } else {
            CommandPlay.startPlaying(this.plugin, player, settings);
        }
    }
}
