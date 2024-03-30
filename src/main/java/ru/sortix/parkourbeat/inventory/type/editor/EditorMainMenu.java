package ru.sortix.parkourbeat.inventory.type.editor;

import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.activity.type.EditActivity;
import ru.sortix.parkourbeat.commands.CommandDelete;
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.inventory.ParkourBeatInventory;
import ru.sortix.parkourbeat.inventory.event.ClickEvent;
import ru.sortix.parkourbeat.item.ItemUtils;
import ru.sortix.parkourbeat.item.editor.type.EditTrackPointsItem;
import ru.sortix.parkourbeat.levels.settings.GameSettings;
import ru.sortix.parkourbeat.levels.settings.LevelSettings;
import ru.sortix.parkourbeat.levels.settings.Song;
import ru.sortix.parkourbeat.player.input.PlayersInputManager;
import ru.sortix.parkourbeat.world.TeleportUtils;

import java.util.Arrays;
import java.util.List;

import static ru.sortix.parkourbeat.world.LocationUtils.isValidSpawnPoint;

public class EditorMainMenu extends ParkourBeatInventory {
    private final EditActivity activity;

    public EditorMainMenu(@NonNull ParkourBeat plugin, @NonNull EditActivity activity) {
        super(plugin, 5, Component.text("Параметры уровня"));
        this.activity = activity;
        this.setItem(
            1,
            5,
            ItemUtils.create(Material.REDSTONE_TORCH, (meta) -> {
                meta.displayName(Component.text("Покинуть редактор", NamedTextColor.GOLD));
                meta.lore(List.of(Component.text("Блоки и настройки будут сохранены", NamedTextColor.YELLOW)));
            }),
            this::leaveEditor);
        this.setItem(
            3,
            2,
            ItemUtils.create(Material.FIREWORK_STAR, (meta) -> {
                meta.displayName(Component.text("Цвет частиц", NamedTextColor.GOLD));
                meta.lore(Arrays.asList(
                    Component.text("Изменяет цвет частиц после", NamedTextColor.YELLOW),
                    Component.text("достижения определённой позиции.", NamedTextColor.YELLOW),
                    Component.text("Вам потребуется указать в чате", NamedTextColor.YELLOW),
                    Component.text("HEX-цвет. Например: #FFCC66", NamedTextColor.YELLOW)
                ));
            }),
            this::selectParticlesColor);
        this.setItem(
            3,
            4,
            ItemUtils.modifyMeta(SelectSongMenu.NOTE_HEAD.clone(), meta -> {
                meta.displayName(Component.text("Выбрать музыку", NamedTextColor.GOLD));
                Song song = activity.getLevel()
                    .getLevelSettings()
                    .getGameSettings()
                    .getSong();
                meta.lore(Arrays.asList(
                    Component.text("Трек, который будет запускаться", NamedTextColor.YELLOW),
                    Component.text("Текущая композиция:", NamedTextColor.YELLOW),
                    Component.text(song == null ? "отсутствует" : song.getSongName(), NamedTextColor.YELLOW)
                ));
            }),
            this::selectLevelSong);
        this.setItem(
            3,
            6,
            ItemUtils.create(Material.ENDER_PEARL, (meta) -> {
                meta.displayName(Component.text("Точка спауна", NamedTextColor.GOLD));
                meta.lore(Arrays.asList(
                    Component.text("Устанавливает точку спауна на уровне ваших ног.", NamedTextColor.YELLOW),
                    Component.text("Направление взгляда игроков будет точно таким же,", NamedTextColor.YELLOW),
                    Component.text("как при установке точки спауна", NamedTextColor.YELLOW)
                ));
            }),
            this::setSpawnPoint);
        this.setItem(
            3,
            8,
            ItemUtils.create(Material.WRITABLE_BOOK, (meta) -> {
                meta.displayName(Component.text("Переименовать уровень", NamedTextColor.GOLD));
                meta.lore(Arrays.asList(
                    Component.text("Вам будет необходимо отправить", NamedTextColor.YELLOW),
                    Component.text("новое название в чат", NamedTextColor.YELLOW)
                ));
            }),
            this::renameLevel);
        this.setItem(
            5,
            3,
            ItemUtils.create(Material.NETHER_STAR, (meta) -> {
                meta.displayName(Component.text("Сбросить все точки", NamedTextColor.GOLD));
                meta.lore(Arrays.asList(
                    Component.text("Все установленные точки трека", NamedTextColor.RED, TextDecoration.BOLD),
                    Component.text("будут БЕЗВОЗВРАТНО удалены", NamedTextColor.RED, TextDecoration.BOLD)
                ));
            }),
            this::resetAllTrackPoints);
        this.setItem(
            5,
            7,
            ItemUtils.create(Material.BARRIER, (meta) -> {
                meta.displayName(Component.text("Удалить уровень", NamedTextColor.GOLD));
                meta.lore(Arrays.asList(
                    Component.text("Уровень будет удалён", NamedTextColor.RED, TextDecoration.BOLD),
                    Component.text("БЕЗ возможности восстановления", NamedTextColor.RED, TextDecoration.BOLD)
                ));
            }),
            this::deleteLevel);
        this.setItem(
            5,
            5,
            ItemUtils.create(Material.SLIME_BLOCK, (meta) -> {
                meta.setDisplayName(ChatColor.GREEN + "Физика Блоков");
                meta.lore(Arrays.asList(
                    Component.text("У некоторых блоков есть уникальные", NamedTextColor.GRAY),
                    Component.text("физические свойства.", NamedTextColor.RED),
                    Component.empty(),
                    Component.text("От блоков ", NamedTextColor.GRAY)
                        .append(Component.text("Слизи,", NamedTextColor.GREEN))
                        .append(Component.text(" Голубого бетона", NamedTextColor.AQUA))
                        .append(Component.text(" игрок отскакивает", NamedTextColor.GRAY)),
                    Component.text("По стенам из ", NamedTextColor.GRAY)
                        .append(Component.text("Всех Вариаций Льда,", NamedTextColor.BLUE))
                        .append(Component.text(" Оранжевого бетона", NamedTextColor.GOLD))
                        .append(Component.text(" игрок скользит.", NamedTextColor.GRAY)),
                    Component.empty(),
                    activity.getLevel().getLevelSettings().getGameSettings().isCustomPhysicsEnabled()
                        ? Component.text("Нажмите, чтобы выключить!", NamedTextColor.RED)
                        : Component.text("Нажмите, чтобы включить!", NamedTextColor.GREEN)
                ));
            }),
            this::switchCustomBlockPhysics);
    }

    private void leaveEditor(@NonNull ClickEvent event) {
        Player player = event.getPlayer();

        TeleportUtils.teleportAsync(this.plugin, player, Settings.getLobbySpawn()).thenAccept(success -> {
            if (success) return;
            player.sendMessage("Телепортация на спаун отменена");
        });
    }

    private void selectParticlesColor(@NonNull ClickEvent event) {
        Player player = event.getPlayer();
        player.closeInventory();

        PlayersInputManager manager = this.plugin.get(PlayersInputManager.class);
        if (manager.isInputRequested(player)) {
            player.sendMessage("Функция недоступна в данный момент");
            return;
        }

        player.sendMessage("У вас есть 30 сек, чтобы указать в чате HEX-цвет. Например: #FFCC66");
        player.sendMessage(Component.text("Подобрать цвет можно ")
            .append(Component.text("тут", NamedTextColor.WHITE, TextDecoration.UNDERLINED)
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.openUrl("https://google.com/search?q=hex+палитра")))
            .append(Component.text(" (кликабельно)")));

        manager.requestChatInput(player, 20 * 30).thenAccept(message -> {
            if (message == null) {
                player.sendMessage("Цвет не выбран");
                return;
            }

            String hex = message.startsWith("#") ? message.substring(1) : message;
            Color color;
            try {
                color = Color.fromRGB(Integer.valueOf(hex, 16));
            } catch (IllegalArgumentException e) {
                player.sendMessage("Ошибка. Пожалуйста, убедитесь, что вы ввели правильный HEX-код");
                return;
            }
            this.activity.setCurrentColor(color);

            player.sendMessage(Component.text("Выбранный цвет:"));
            player.sendMessage(Component.text("#" + hex, TextColor.color(color.asRGB())));
        });
    }

    private void selectLevelSong(@NonNull ClickEvent event) {
        Player player = event.getPlayer();

        new SelectSongMenu(this.plugin, this.activity.getLevel()).open(player);
    }

    private void setSpawnPoint(@NonNull ClickEvent event) {
        Player player = event.getPlayer();
        player.closeInventory();

        LevelSettings levelSettings = this.activity.getLevel().getLevelSettings();
        Location playerLocation = player.getLocation();

        if (!isValidSpawnPoint(playerLocation, levelSettings)) {
            player.sendMessage("Точка спауна не может быть установлена здесь.");
            return;
        }

        levelSettings.getWorldSettings().setSpawn(playerLocation);

        player.sendMessage("Точка спауна установлена на уровне ваших ног. "
            + "Убедитесь, что направление взгляда выбрано корректно! "
            + "Именно в эту сторону будут повёрнуты игроки при телепортации");
    }

    private void renameLevel(@NonNull ClickEvent event) {
        Player player = event.getPlayer();
        player.closeInventory();

        PlayersInputManager manager = this.plugin.get(PlayersInputManager.class);
        if (manager.isInputRequested(player)) {
            player.sendMessage("Функция недоступна в данный момент");
            return;
        }

        player.sendMessage("У вас есть 30 сек, чтобы указать в чате новое название уровня");
        manager.requestChatInput(player, 20 * 30).thenAccept(newNameLegacy -> {
            if (newNameLegacy == null) {
                player.sendMessage("Новое название не введено");
                return;
            }

            Component newName = LegacyComponentSerializer.legacyAmpersand().deserialize(newNameLegacy);
            int nameLength = PlainComponentSerializer.plain().serialize(newName).length();

            if (nameLength < 3) {
                player.sendMessage("Название должно содержать от 3 до 30 символов");
                return;
            }

            if (nameLength > 30) {
                player.sendMessage("Название должно содержать от 5 до 30 символов");
                return;
            }

            Component oldName;
            oldName = this.activity.getLevel().getDisplayName();
            this.activity.getLevel().getLevelSettings().getGameSettings().setDisplayName(newName);
            newName = this.activity.getLevel().getDisplayName();

            player.sendMessage(Component.text()
                .append(Component.text("Название изменено с \"", NamedTextColor.WHITE))
                .append(oldName)
                .append(Component.text("\" на \"", NamedTextColor.WHITE))
                .append(newName)
                .append(Component.text("\"", NamedTextColor.WHITE))
            );
        });
    }

    private void resetAllTrackPoints(@NonNull ClickEvent event) {
        Player player = event.getPlayer();
        player.closeInventory();

        EditTrackPointsItem.clearAllPoints(this.activity.getLevel());
        player.sendMessage("Все точки сброшены");
    }

    private void deleteLevel(@NonNull ClickEvent event) {
        Player player = event.getPlayer();
        player.closeInventory();

        CommandDelete.deleteLevel(
            this.plugin, player, this.activity.getLevel().getLevelSettings().getGameSettings());
    }

    private void switchCustomBlockPhysics(@NonNull ClickEvent event) {
        Player player = event.getPlayer();
        player.closeInventory();

        GameSettings settings = this.activity.getLevel().getLevelSettings().getGameSettings();
        boolean inverted = !settings.isCustomPhysicsEnabled();
        settings.setCustomPhysicsEnabled(inverted);

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_SNARE, 1f, 1f);
        player.sendMessage(inverted
            ? Component.text("Вы включили физику блоков!", NamedTextColor.GREEN)
            : Component.text("Вы выключили физику блоков!", NamedTextColor.RED));
    }

}
