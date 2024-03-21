package ru.sortix.parkourbeat.inventory.type;

import java.text.SimpleDateFormat;
import java.util.*;
import javax.annotation.Nullable;
import lombok.NonNull;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.activity.ActivityManager;
import ru.sortix.parkourbeat.activity.UserActivity;
import ru.sortix.parkourbeat.activity.type.EditActivity;
import ru.sortix.parkourbeat.activity.type.PlayActivity;
import ru.sortix.parkourbeat.activity.type.SpectateActivity;
import ru.sortix.parkourbeat.inventory.PaginatedMenu;
import ru.sortix.parkourbeat.inventory.RegularItems;
import ru.sortix.parkourbeat.inventory.event.ClickEvent;
import ru.sortix.parkourbeat.item.ItemUtils;
import ru.sortix.parkourbeat.levels.Level;
import ru.sortix.parkourbeat.levels.LevelsManager;
import ru.sortix.parkourbeat.levels.settings.GameSettings;
import ru.sortix.parkourbeat.utils.ComponentUtils;
import ru.sortix.parkourbeat.utils.TeleportUtils;

public class LevelsListMenu extends PaginatedMenu<ParkourBeat, GameSettings> {
    private final Player viewer;
    private final boolean displayTechInfo;
    private final boolean editMenu;

    public LevelsListMenu(@NonNull ParkourBeat plugin, @NonNull Player viewer, @Nullable UUID ownerId) {
        super(plugin, 6, "Уровни", 0, 5 * 9);
        this.viewer = viewer;
        this.displayTechInfo = viewer.hasPermission("parkourbeat.admin");
        this.editMenu = ownerId != null;
        this.setItems(this.getAvailableLevels(ownerId));
    }

    @NonNull private Collection<GameSettings> getAvailableLevels(@Nullable UUID ownerId) {
        List<GameSettings> settings =
                new ArrayList<>(this.plugin.get(LevelsManager.class).getAvailableLevelsSettings());
        if (ownerId != null) {
            settings.removeIf(gameSettings -> !gameSettings.isOwner(ownerId));
        }
        settings.sort(Comparator.comparingLong(GameSettings::getCreatedAtMills));
        return settings;
    }

    private static final SimpleDateFormat LEVEL_CREATION_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

    @Override
    @SuppressWarnings("deprecation")
    protected @NonNull ItemStack createItemDisplay(@NonNull GameSettings gameSettings) {
        return ItemUtils.modifyMeta(new ItemStack(Material.PAPER), meta -> {
            meta.setDisplayName(ChatColor.GOLD + gameSettings.getDisplayName());

            List<String> lore = new ArrayList<>();
            if (this.displayTechInfo) {
                lore.add(ChatColor.YELLOW + "UUID: " + gameSettings.getUniqueId());
            }
            if (gameSettings.getUniqueName() == null) {
                lore.add(ChatColor.YELLOW + "Номер для команд: " + gameSettings.getUniqueNumber());
            } else {
                lore.add(ChatColor.YELLOW + "Название для команд: " + gameSettings.getUniqueName());
            }
            lore.add(ChatColor.YELLOW + "Создатель: " + gameSettings.getOwnerName());
            if (this.displayTechInfo) {
                lore.add(ChatColor.YELLOW + "UUID создателя: " + gameSettings.getOwnerId());
            }
            lore.add(ChatColor.YELLOW + "Дата создания: "
                    + LEVEL_CREATION_DATE_FORMAT.format(new Date(gameSettings.getCreatedAtMills())));
            lore.add(ChatColor.YELLOW + "Трек: "
                    + (gameSettings.getSong() == null
                            ? "отсутствует"
                            : gameSettings.getSong().getSongName()));
            lore.add(ChatColor.GOLD + "ЛКМ, чтобы играть");
            lore.add(ChatColor.GOLD + "ПКМ, чтобы наблюдать");
            if (gameSettings.isOwner(this.viewer, true, false)) {
                lore.add(ChatColor.GOLD + "Шифт + ЛКМ, чтобы редактировать");
            }
            if (this.displayTechInfo) {
                lore.add(ChatColor.GOLD + "Шифт + ПКМ, чтобы скопировать UUID");
            }
            meta.setLore(lore);
        });
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onPageDisplayed() {
        this.setNextPageItem(6, 3);
        this.setItem(
                6, 5, RegularItems.closeInventory(), event -> event.getPlayer().closeInventory());
        this.setPreviousPageItem(6, 7);
        if (this.editMenu) {
            this.setItem(
                    6,
                    1,
                    ItemUtils.create(
                            Material.WRITABLE_BOOK, meta -> meta.setDisplayName(ChatColor.GOLD + "Создать уровень")),
                    event -> new CreateLevelMenu(this.plugin).open(event.getPlayer()));
        }
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
                    new LevelsListMenu(this.plugin, this.viewer, this.editMenu ? null : player.getUniqueId())
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
                    //noinspection deprecation
                    this.viewer.sendMessage(ComponentUtils.createCopyTextComponent(
                            ChatColor.YELLOW + "> Скопировать UUID уровня <",
                            ChatColor.GOLD + "Нажмите для копирования",
                            settings.getUniqueId().toString()));
                }
            } else {
                startSpectating(this.plugin, event.getPlayer(), settings);
            }
        }
    }

    public static void startPlaying(
            @NonNull ParkourBeat plugin, @NonNull Player player, @NonNull GameSettings settings) {
        Level level = plugin.get(LevelsManager.class).getLoadedLevel(settings.getUniqueId());
        if (level != null && level.isEditing()) {
            player.sendMessage("Данный уровень недоступен для игры, т.к. он сейчас редактируется");
            return;
        }

        UserActivity activity = plugin.get(ActivityManager.class).getActivity(player);
        if (activity instanceof PlayActivity && activity.getLevel() == level) {
            player.sendMessage("Вы уже на этом уровне!");
            return;
        }

        PlayActivity.createAsync(plugin, player, settings.getUniqueId(), false).thenAccept(playActivity -> {
            if (playActivity == null) {
                player.sendMessage("Не удалось запустить игру");
                return;
            }

            plugin.get(ActivityManager.class).setActivity(player, playActivity);
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

                    SpectateActivity.createAsync(plugin, player, level).thenAccept(spectateActivity -> {
                        TeleportUtils.teleportAsync(plugin, player, level.getSpawn())
                                .thenAccept(success -> {
                                    if (!success) return;
                                    plugin.get(ActivityManager.class).setActivity(player, spectateActivity);
                                });
                    });
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
                        activityManager.setActivity(player, editActivity);
                    });
                });
    }
}
