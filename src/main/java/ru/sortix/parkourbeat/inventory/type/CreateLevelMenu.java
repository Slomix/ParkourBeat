package ru.sortix.parkourbeat.inventory.type;

import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

    public CreateLevelMenu(@NonNull ParkourBeat plugin) {
        super(plugin, 3, Component.text("Выберите небо"));
        if (DISPLAY_NON_DEFAULT_WORLD_TYPES) {
            this.setItem(
                2,
                3,
                ItemUtils.create(
                    Material.NETHERRACK, meta -> meta.displayName(Component.text("Небо Нижнего мира", NamedTextColor.RED))),
                event -> this.createLevel(event.getPlayer(), World.Environment.NETHER));
        }
        this.setItem(
            2,
            5,
            ItemUtils.create(Material.GRASS_BLOCK, meta -> meta.displayName(Component.text("Обычное небо", NamedTextColor.AQUA))),
            event -> this.createLevel(event.getPlayer(), World.Environment.NORMAL));
        if (DISPLAY_NON_DEFAULT_WORLD_TYPES) {
            this.setItem(
                2,
                7,
                ItemUtils.create(
                    Material.END_STONE, meta -> meta.displayName(Component.text("Небо Края", NamedTextColor.DARK_PURPLE))),
                event -> this.createLevel(event.getPlayer(), World.Environment.THE_END));
        }
        this.setItem(
            3,
            9,
            ItemUtils.create(Material.BARRIER, meta -> meta.displayName(Component.text("Отмена", NamedTextColor.RED))),
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
                        owner.sendMessage(Component.text("Уровень \"", NamedTextColor.WHITE)
                            .append(level.getDisplayName())
                            .append(Component.text("\" создан, однако не удалось запустить редактор уровня", NamedTextColor.WHITE))
                        );
                        return;
                    }
                    this.plugin.get(ActivityManager.class).switchActivity(owner, editActivity, level.getSpawn()).thenAccept(success -> {
                        if (success) {
                            owner.sendMessage(Component.text("Уровень \"", NamedTextColor.WHITE)
                                .append(level.getDisplayName())
                                .append(Component.text("\" создан", NamedTextColor.WHITE))
                            );
                        } else {
                            owner.sendMessage(Component.text("Не удалось телепортировать вас на новый уровень"));
                        }
                    });
                });
            });
    }
}
