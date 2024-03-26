package ru.sortix.parkourbeat.inventory.type.editor;

import lombok.NonNull;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.activity.ActivityManager;
import ru.sortix.parkourbeat.activity.type.EditActivity;
import ru.sortix.parkourbeat.commands.CommandDelete;
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.inventory.ParkourBeatInventory;
import ru.sortix.parkourbeat.inventory.event.ClickEvent;
import ru.sortix.parkourbeat.item.ItemUtils;
import ru.sortix.parkourbeat.item.editor.type.EditTrackPointsItem;
import ru.sortix.parkourbeat.levels.settings.LevelSettings;
import ru.sortix.parkourbeat.levels.settings.Song;
import ru.sortix.parkourbeat.player.input.PlayersInputManager;
import ru.sortix.parkourbeat.world.TeleportUtils;

import java.util.Arrays;
import java.util.List;

import static ru.sortix.parkourbeat.world.LocationUtils.isValidSpawnPoint;

public class EditorMainMenu extends ParkourBeatInventory {
    private final EditActivity activity;

    @SuppressWarnings("deprecation")
    public EditorMainMenu(@NonNull ParkourBeat plugin, @NonNull EditActivity activity) {
        super(plugin, 5, "Параметры уровня");
        this.activity = activity;
        this.setItem(
            1,
            5,
            ItemUtils.create(Material.REDSTONE_TORCH, (meta) -> {
                meta.setDisplayName(ChatColor.GOLD + "Покинуть редактор");
                meta.setLore(List.of(ChatColor.YELLOW + "Блоки и настройки будут сохранены"));
            }),
            this::leaveEditor);
        this.setItem(
            3,
            2,
            ItemUtils.create(Material.FIREWORK_STAR, (meta) -> {
                meta.setDisplayName(ChatColor.GOLD + "Цвет частиц");
                meta.setLore(Arrays.asList(
                    ChatColor.YELLOW + "Изменяет цвет частиц после",
                    ChatColor.YELLOW + "достижения определённой позиции.",
                    ChatColor.YELLOW + "Вам потребуется указать в чате",
                    ChatColor.YELLOW + "HEX-цвет. Например: #FFCC66"));
            }),
            this::selectParticlesColor);
        this.setItem(
            3,
            4,
            ItemUtils.modifyMeta(SelectSongMenu.NOTE_HEAD.clone(), meta -> {
                meta.setDisplayName(ChatColor.GOLD + "Выбрать музыку");
                meta.setLore(List.of());
                Song song = activity.getLevel()
                    .getLevelSettings()
                    .getGameSettings()
                    .getSong();
                meta.setLore(Arrays.asList(
                    ChatColor.YELLOW + "Трек, который будет запускаться",
                    ChatColor.YELLOW + "Текущая композиция:",
                    ChatColor.YELLOW + (song == null ? "отсутствует" : song.getSongName())));
            }),
            this::selectLevelSong);
        this.setItem(
            3,
            6,
            ItemUtils.create(Material.ENDER_PEARL, (meta) -> {
                meta.setDisplayName(ChatColor.GOLD + "Точка спауна");
                meta.setLore(Arrays.asList(
                    ChatColor.YELLOW + "Устанавливает точку спауна на уровне ваших ног.",
                    ChatColor.YELLOW + "Направление взгляда игроков будет точно таким же,",
                    ChatColor.YELLOW + "как при установке точки спауна"));
            }),
            this::setSpawnPoint);
        this.setItem(
            3,
            8,
            ItemUtils.create(Material.WRITABLE_BOOK, (meta) -> {
                meta.setDisplayName(ChatColor.GOLD + "Переименовать уровень");
                meta.setLore(Arrays.asList(
                    ChatColor.YELLOW + "Вам будет необходимо отправить",
                    ChatColor.YELLOW + "новое название в чат"));
            }),
            this::renameLevel);
        this.setItem(
            5,
            3,
            ItemUtils.create(Material.NETHER_STAR, (meta) -> {
                meta.setDisplayName(ChatColor.GOLD + "Сбросить все точки");
                meta.setLore(Arrays.asList(
                    ChatColor.RED + "" + ChatColor.BOLD + "Все установленные точки трека",
                    ChatColor.RED + "" + ChatColor.BOLD + "будут БЕЗВОЗВРАТНО удалены"));
            }),
            this::resetAllTrackPoints);
        this.setItem(
            5,
            7,
            ItemUtils.create(Material.BARRIER, (meta) -> {
                meta.setDisplayName(ChatColor.GOLD + "Удалить уровень");
                meta.setLore(Arrays.asList(
                    ChatColor.RED + "" + ChatColor.BOLD + "Уровень будет удалён",
                    ChatColor.RED + "" + ChatColor.BOLD + "БЕЗ возможности восстановления"));
            }),
            this::deleteLevel);
    }

    private void leaveEditor(@NonNull ClickEvent event) {
        Player player = event.getPlayer();

        TeleportUtils.teleportAsync(this.plugin, player, Settings.getLobbySpawn())
            .thenAccept(success -> {
                if (!success) return;
                this.plugin.get(ActivityManager.class).setActivity(player, null);
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
        TextComponent msg1 = new TextComponent(
            "Подобрать цвет можно " + ChatColor.UNDERLINE + "тут" + ChatColor.RESET + " (кликабельно)");
        msg1.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(
            net.md_5.bungee.api.chat.ClickEvent.Action.OPEN_URL, "https://google.com/search?q=hex+палитра"));
        //noinspection deprecation
        player.sendMessage(msg1);

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

            player.sendMessage("Выбранный цвет:");

            TextComponent msg2 = new TextComponent("#" + hex);
            msg2.setColor(ChatColor.of("#" + hex));
            //noinspection deprecation
            player.sendMessage(msg2);
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
        manager.requestChatInput(player, 20 * 30).thenAccept(newName -> {
            if (newName == null) {
                player.sendMessage("Новое название не введено");
                return;
            }

            newName = ChatColor.translateAlternateColorCodes('&', newName);
            String plainName = ChatColor.stripColor(newName);

            if (plainName.length() < 3) {
                player.sendMessage("Название должно содержать от 3 до 30 символов");
                return;
            }

            if (plainName.length() > 30) {
                player.sendMessage("Название должно содержать от 5 до 30 символов");
                return;
            }

            String oldName;

            oldName = this.activity.getLevel().getDisplayName();
            this.activity.getLevel().getLevelSettings().getGameSettings().setDisplayName(newName);
            newName = this.activity.getLevel().getDisplayName();

            player.sendMessage("Название изменено с \"" + oldName + "\" на \"" + newName + "\"");
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
}
