package ru.sortix.parkourbeat.inventory.type;

import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.activity.ActivityManager;
import ru.sortix.parkourbeat.activity.type.EditActivity;
import ru.sortix.parkourbeat.inventory.ParkourBeatInventory;
import ru.sortix.parkourbeat.item.ItemUtils;
import ru.sortix.parkourbeat.levels.LevelsManager;

public class CreateLevelMenu extends ParkourBeatInventory {
    // TODO Support for NETHER and THE_END environments
    public static final boolean DISPLAY_NON_DEFAULT_WORLD_TYPES = false;

    @SuppressWarnings("deprecation")
    public CreateLevelMenu(@NonNull ParkourBeat plugin) {
        super(plugin, 3, "Выберите небо");
        if (DISPLAY_NON_DEFAULT_WORLD_TYPES) {
            this.setItem(
                2,
                3,
                ItemUtils.create(
                    Material.NETHERRACK, meta -> meta.setDisplayName(ChatColor.RED + "Небо Нижнего мира")),
                event -> this.createLevel(event.getPlayer(), World.Environment.NETHER));
        }
        this.setItem(
            2,
            5,
            ItemUtils.create(Material.GRASS_BLOCK, meta -> meta.setDisplayName(ChatColor.AQUA + "Обычное небо")),
            event -> this.createLevel(event.getPlayer(), World.Environment.NORMAL));
        if (DISPLAY_NON_DEFAULT_WORLD_TYPES) {
            this.setItem(
                2,
                7,
                ItemUtils.create(
                    Material.END_STONE, meta -> meta.setDisplayName(ChatColor.DARK_PURPLE + "Небо Края")),
                event -> this.createLevel(event.getPlayer(), World.Environment.THE_END));
        }
        this.setItem(
            3,
            9,
            ItemUtils.create(Material.BARRIER, meta -> meta.setDisplayName(ChatColor.RED + "Отмена")),
            event -> event.getPlayer().closeInventory());
    }

    @Override
    public void open(@NonNull Player player) {
        if (!player.hasPermission("parkourbeat.level.create")) {
            player.sendMessage("Недостаточно прав для создания уровня");
            return;
        }
        super.open(player);
    }

    private void createLevel(@NonNull Player owner, @NonNull World.Environment environment) {
        this.plugin.get(ActivityManager.class).setActivity(owner, null);
        this.plugin
            .get(LevelsManager.class)
            .createLevel(environment, owner.getUniqueId(), owner.getName())
            .thenAccept(level -> {
                if (level == null) {
                    owner.sendMessage("Не удалось создать уровень");
                    return;
                }
                EditActivity.createAsync(this.plugin, owner, level).thenAccept(editActivity -> {
                    if (editActivity == null) {
                        owner.sendMessage("Уровень \"" + level.getDisplayName() + "\""
                            + " создан, однако не удалось запустить редактор уровня");
                        return;
                    }
                    this.plugin.get(ActivityManager.class).setActivity(owner, editActivity);
                    owner.sendMessage("Уровень \"" + level.getDisplayName() + "\" создан");
                });
            });
    }
}
