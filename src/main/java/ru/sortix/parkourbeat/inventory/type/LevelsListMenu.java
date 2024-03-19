package ru.sortix.parkourbeat.inventory.type;

import java.util.Arrays;
import lombok.NonNull;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.commands.CommandPlay;
import ru.sortix.parkourbeat.inventory.ParkourBeatInventory;
import ru.sortix.parkourbeat.item.ItemUtils;
import ru.sortix.parkourbeat.levels.LevelsManager;
import ru.sortix.parkourbeat.levels.settings.GameSettings;

public class LevelsListMenu extends ParkourBeatInventory {
    @SuppressWarnings("deprecation")
    public LevelsListMenu(@NonNull ParkourBeat plugin) {
        super(plugin, 6, "Уровни");
        int slot = 0;
        for (GameSettings gameSettings : this.plugin.get(LevelsManager.class).getAvailableLevelsSettings()) {
            this.setItem(
                    slot++,
                    ItemUtils.modifyMeta(new ItemStack(Material.PAPER), meta -> {
                        meta.setDisplayName(ChatColor.GOLD + gameSettings.getLevelName());
                        meta.setLore(Arrays.asList(
                                ChatColor.YELLOW + "Создатель: " + gameSettings.getOwnerName(),
                                ChatColor.YELLOW + "ID: " + gameSettings.getLevelId(),
                                ChatColor.YELLOW + "Трек: "
                                        + (gameSettings.getSong() == null
                                                ? "отсутствует"
                                                : gameSettings.getSong().getSongName())));
                    }),
                    player -> CommandPlay.startPlaying(this.plugin, player, gameSettings.getLevelId()));
        }
    }
}
