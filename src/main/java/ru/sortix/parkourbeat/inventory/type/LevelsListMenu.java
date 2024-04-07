package ru.sortix.parkourbeat.inventory.type;

import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.activity.ActivityManager;
import ru.sortix.parkourbeat.activity.UserActivity;
import ru.sortix.parkourbeat.activity.type.EditActivity;
import ru.sortix.parkourbeat.activity.type.PlayActivity;
import ru.sortix.parkourbeat.inventory.PaginatedMenu;
import ru.sortix.parkourbeat.inventory.RegularItems;
import ru.sortix.parkourbeat.inventory.event.ClickEvent;
import ru.sortix.parkourbeat.item.ItemUtils;
import ru.sortix.parkourbeat.levels.Level;
import ru.sortix.parkourbeat.levels.LevelsManager;
import ru.sortix.parkourbeat.levels.settings.GameSettings;
import ru.sortix.parkourbeat.world.TeleportUtils;

import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.*;

public class LevelsListMenu extends PaginatedMenu<ParkourBeat, GameSettings> {
    private static final SimpleDateFormat LEVEL_CREATION_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    private final @NonNull Player viewer;
    private final @Nullable UUID ownerId;
    private final boolean displayTechInfo;
    private final boolean onlyOwnLevels;

    public LevelsListMenu(@NonNull ParkourBeat plugin, @NonNull Player viewer, @Nullable UUID ownerId) {
        super(plugin, 6, Component.text("Уровни"), 0, 5 * 9);
        this.viewer = viewer;
        this.ownerId = ownerId;
        this.displayTechInfo = viewer.hasPermission("parkourbeat.admin");
        this.onlyOwnLevels = ownerId != null;
        this.updateAllItems();
    }

    @Override
    @NonNull
    protected Collection<GameSettings> getAllItems() {
        List<GameSettings> settings =
            new ArrayList<>(this.plugin.get(LevelsManager.class).getAvailableLevelsSettings());
        if (this.ownerId != null) {
            settings.removeIf(gameSettings -> !gameSettings.isOwner(this.ownerId));
        }
        settings.sort(Comparator.comparingLong(GameSettings::getCreatedAtMills));
        return settings;
    }

    public static void startPlaying(
        @NonNull ParkourBeat plugin, @NonNull Player player, @NonNull GameSettings settings) {
        Level level = plugin.get(LevelsManager.class).getLoadedLevel(settings.getUniqueId());
        if (level != null && level.isEditing()) {
            player.sendMessage("Данный уровень недоступен для игры, т.к. он сейчас редактируется");
            return;
        }

        UserActivity previousActivity = plugin.get(ActivityManager.class).getActivity(player);
        if (previousActivity instanceof PlayActivity && previousActivity.getLevel() == level) {
            player.sendMessage("Вы уже на этом уровне!");
            return;
        }

        PlayActivity.createAsync(plugin, player, settings.getUniqueId(), false).thenAccept(playActivity -> {
            if (playActivity == null) {
                player.sendMessage("Не удалось подготовить игру");
                return;
            }

            plugin.get(ActivityManager.class).switchActivity(player, playActivity, playActivity.getLevel().getSpawn())
                .thenAccept(success -> {
                    if (!success) {
                        player.sendMessage("Не удалось телепортировать вас на уровень");
                    }
                });
        });
    }

    public static void startSpectating(
        @NonNull ParkourBeat plugin, @NonNull Player player, @NonNull GameSettings settings) {
        plugin.get(LevelsManager.class)
            .loadLevel(settings.getUniqueId(), settings)
            .thenAccept(level -> {
                if (level == null) {
                    player.sendMessage("Не удалось загрузить данные уровня");
                    return;
                }
                if (level.getWorld() == player.getWorld()) {
                    player.sendMessage("Вы уже в этом мире");
                    return;
                }
                TeleportUtils.teleportAsync(plugin, player, level.getSpawn());
            });
    }

    public static void startEditing(
        @NonNull ParkourBeat plugin, @NonNull Player player, @NonNull GameSettings settings) {
        if (!settings.isOwner(player, true, true)) {
            player.sendMessage("Вы не являетесь владельцем этого уровня");
            return;
        }

        plugin.get(LevelsManager.class)
            .loadLevel(settings.getUniqueId(), settings)
            .thenAccept(level -> {
                if (level == null) {
                    player.sendMessage("Не удалось загрузить данные уровня");
                    return;
                }

                if (level.isEditing()) {
                    player.sendMessage("Данный уровень уже редактируется");
                    return;
                }

                ActivityManager activityManager = plugin.get(ActivityManager.class);

                Collection<Player> playersOnLevel = activityManager.getPlayersOnTheLevel(level);
                playersOnLevel.removeIf(player1 -> settings.isOwner(player1, true, true));

                if (!playersOnLevel.isEmpty()) {
                    player.sendMessage("Нельзя редактировать уровень, на котором находятся игроки");
                    return;
                }

                EditActivity.createAsync(plugin, player, level).thenAccept(editActivity -> {
                    if (editActivity == null) {
                        player.sendMessage("Не удалось запустить редактор уровня");
                        return;
                    }
                    activityManager.switchActivity(player, editActivity, level.getSpawn()).thenAccept(success -> {
                        if (success) return;
                        player.sendMessage("Не удалось телепортироваться в мир уровня");
                    });
                });
            });
    }

    @Override
    protected @NonNull ItemStack createItemDisplay(@NonNull GameSettings gameSettings) {
        return ItemUtils.modifyMeta(new ItemStack(Material.PAPER), meta -> {
            meta.displayName(gameSettings.getDisplayName());

            List<Component> lore = new ArrayList<>();
            if (this.displayTechInfo) {
                lore.add(Component.text("UUID: " + gameSettings.getUniqueId(), NamedTextColor.YELLOW));
            }
            if (gameSettings.getUniqueName() == null) {
                lore.add(Component.text("Номер для команд: " + gameSettings.getUniqueNumber(), NamedTextColor.YELLOW));
            } else {
                lore.add(Component.text("Название для команд: " + gameSettings.getUniqueName(), NamedTextColor.YELLOW));
            }
            lore.add(Component.text("Создатель: " + gameSettings.getOwnerName(), NamedTextColor.YELLOW));
            if (this.displayTechInfo) {
                lore.add(Component.text("UUID создателя: " + gameSettings.getOwnerId(), NamedTextColor.YELLOW));
            }
            lore.add(Component.text("Дата создания: "
                + LEVEL_CREATION_DATE_FORMAT.format(new Date(gameSettings.getCreatedAtMills())), NamedTextColor.YELLOW));
            lore.add(Component.text("Трек: "
                + (gameSettings.getMusicTrack() == null
                ? "отсутствует"
                : gameSettings.getMusicTrack().getName()), NamedTextColor.YELLOW));
            lore.add(Component.text("ЛКМ, чтобы играть", NamedTextColor.GOLD));
            lore.add(Component.text("ПКМ, чтобы наблюдать", NamedTextColor.GOLD));
            if (gameSettings.isOwner(this.viewer, true, false)) {
                lore.add(Component.text("Шифт + ЛКМ, чтобы редактировать", NamedTextColor.GOLD));
            }
            if (this.displayTechInfo) {
                lore.add(Component.text("Шифт + ПКМ, чтобы скопировать UUID", NamedTextColor.GOLD));
            }
            meta.lore(lore);
        });
    }

    @Override
    protected void onPageDisplayed() {
        this.setNextPageItem(6, 3);
        this.setItem(
            6, 5, RegularItems.closeInventory(), event -> event.getPlayer().closeInventory());
        this.setPreviousPageItem(6, 7);
        if (this.onlyOwnLevels) {
            this.setItem(
                6,
                1,
                ItemUtils.create(
                    Material.WRITABLE_BOOK, meta -> meta.displayName(Component.text("Создать уровень", NamedTextColor.GOLD))),
                event -> new CreateLevelMenu(this.plugin).open(event.getPlayer()));
        }
        this.setItem(
            6,
            9,
            ItemUtils.create(Material.BOOK, meta -> {
                meta.displayName(Component.text(this.onlyOwnLevels ? "Все уровни" : "Собственные уровни", NamedTextColor.GOLD));
            }),
            event -> {
                Player player = event.getPlayer();
                new LevelsListMenu(this.plugin, this.viewer, this.onlyOwnLevels ? null : player.getUniqueId())
                    .open(player);
            });
    }

    @Override
    protected void onClick(@NonNull ClickEvent event, @NonNull GameSettings settings) {
        if (event.isLeft()) {
            if (event.isShift()) {
                startEditing(this.plugin, event.getPlayer(), settings);
            } else {
                startPlaying(this.plugin, event.getPlayer(), settings);
            }
        } else {
            if (event.isShift()) {
                if (this.displayTechInfo) {
                    event.getPlayer().closeInventory();
                    this.viewer.sendMessage(Component.text("> Скопировать UUID уровня <", NamedTextColor.YELLOW)
                        .hoverEvent(HoverEvent.showText(Component.text("Нажмите для копирования", NamedTextColor.GOLD)))
                        .clickEvent(net.kyori.adventure.text.event.ClickEvent.copyToClipboard(settings.getUniqueId().toString()))
                    );
                }
            } else {
                startSpectating(this.plugin, event.getPlayer(), settings);
            }
        }
    }
}
