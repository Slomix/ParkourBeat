package ru.sortix.parkourbeat.inventory.levels;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.commands.CommandPlay;
import ru.sortix.parkourbeat.inventory.ParkourBeatInventory;
import ru.sortix.parkourbeat.levels.LevelsManager;
import ru.sortix.parkourbeat.levels.settings.GameSettings;

public class LevelsListMenu extends ParkourBeatInventory {
    private final @NonNull Inventory inventory;
    private final @NonNull Player viewer;
    private final @NonNull Map<Integer, GameSettings> settingsBySlot = new HashMap<>();

    public LevelsListMenu(@NonNull ParkourBeat plugin, @NonNull Player viewer) {
        super(plugin);
        this.inventory = this.plugin.getServer().createInventory(this, 6 * 9, "Уровни");
        this.viewer = viewer;
        int slot = 0;
        for (GameSettings gameSettings : this.plugin.get(LevelsManager.class).getAvailableLevels()) {
            this.settingsBySlot.put(slot, gameSettings);
            this.inventory.setItem(slot++, this.createLevelStack(gameSettings));
        }
    }

    @NonNull private ItemStack createLevelStack(@NonNull GameSettings gameSettings) {
        ItemStack stack = new ItemStack(Material.PAPER);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + gameSettings.getLevelName());
        meta.setLore(Arrays.asList(
                ChatColor.YELLOW + "Создатель: " + gameSettings.getOwnerName(),
                ChatColor.YELLOW + "ID: " + gameSettings.getLevelId(),
                ChatColor.YELLOW + "Трек: "
                        + (gameSettings.getSong() == null
                                ? "отсутствует"
                                : gameSettings.getSong().getSongName())));
        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public void onClick(@NonNull Player player, int slot) {
        GameSettings gameSettings = this.settingsBySlot.get(slot);
        if (gameSettings == null) return;
        CommandPlay.startPlaying(this.plugin, player, gameSettings.getLevelId());
    }

    @Override
    public void onClose(@NonNull Player player) {}

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
