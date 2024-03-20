package ru.sortix.parkourbeat.inventory.type.editor;

import static ru.sortix.parkourbeat.utils.LocationUtils.isValidSpawnPoint;

import java.util.Arrays;
import java.util.List;
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
import ru.sortix.parkourbeat.item.ItemUtils;
import ru.sortix.parkourbeat.item.editor.type.EditTrackPointsItem;
import ru.sortix.parkourbeat.levels.settings.LevelSettings;
import ru.sortix.parkourbeat.levels.settings.Song;
import ru.sortix.parkourbeat.player.input.PlayersInputManager;
import ru.sortix.parkourbeat.utils.TeleportUtils;

public class EditorMainMenu extends ParkourBeatInventory {
    @SuppressWarnings("deprecation")
    public EditorMainMenu(@NonNull ParkourBeat plugin, @NonNull EditActivity activity) {
        super(plugin, 5, "Параметры уровня");
        this.setItem(
                2,
                3,
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
                event -> {
                    Player player = event.getPlayer();

                    new SelectSongMenu(plugin, activity.getLevel()).open(player);
                });
        this.setItem(
                2,
                5,
                ItemUtils.create(Material.ENDER_PEARL, (meta) -> {
                    meta.setDisplayName(ChatColor.GOLD + "Точка спауна");
                    meta.setLore(Arrays.asList(
                            ChatColor.YELLOW + "Устанавливает точку спауна на уровне ваших ног.",
                            ChatColor.YELLOW + "Направление взгляда игроков будет точно таким же,",
                            ChatColor.YELLOW + "как при установке точки спауна"));
                }),
                event -> {
                    Player player = event.getPlayer();
                    player.closeInventory();

                    LevelSettings levelSettings = activity.getLevel().getLevelSettings();
                    Location playerLocation = player.getLocation();

                    if (!isValidSpawnPoint(playerLocation, levelSettings)) {
                        player.sendMessage("Точка спауна не может быть установлена здесь.");
                        return;
                    }

                    levelSettings.getWorldSettings().setSpawn(playerLocation);

                    player.sendMessage("Точка спауна установлена на уровне ваших ног. "
                            + "Убедитесь, что направление взгляда выбрано корректно! "
                            + "Именно в эту сторону будут повёрнуты игроки при телепортации");
                });
        this.setItem(
                2,
                7,
                ItemUtils.create(Material.FIREWORK_STAR, (meta) -> {
                    meta.setDisplayName(ChatColor.GOLD + "Цвет частиц");
                    meta.setLore(Arrays.asList(
                            ChatColor.YELLOW + "Изменяет цвет частиц после",
                            ChatColor.YELLOW + "достижения определённой позиции.",
                            ChatColor.YELLOW + "Вам потребуется указать в чате",
                            ChatColor.YELLOW + "HEX-цвет. Например: #FFCC66"));
                }),
                event -> {
                    Player player = event.getPlayer();
                    player.closeInventory();

                    PlayersInputManager manager = this.plugin.get(PlayersInputManager.class);
                    if (manager.isInputRequested(player)) {
                        player.sendMessage("Функция недоступна в данный момент");
                        return;
                    }

                    player.sendMessage("У вас есть 30 сек., чтобы указать в чате HEX-цвет. Например: #FFCC66");
                    manager.requestChatInput(player, 20 * 30).thenAccept(message -> {
                        if (message == null) {
                            player.sendMessage("Цвет не выбран");
                            return;
                        }

                        String hex = message.startsWith("#") ? message.substring(1) : message;
                        Color color;
                        try {
                            int r = Integer.valueOf(hex.substring(0, 2), 16);
                            int g = Integer.valueOf(hex.substring(2, 4), 16);
                            int b = Integer.valueOf(hex.substring(4, 6), 16);
                            color = Color.fromRGB(r, g, b);
                        } catch (Exception e) {
                            player.sendMessage("Ошибка. Пожалуйста, убедитесь, что вы ввели правильный HEX-код");
                            return;
                        }
                        activity.setCurrentColor(color);

                        player.sendMessage("Выбранный цвет:");

                        TextComponent textComponent = new TextComponent("#" + hex);
                        textComponent.setColor(ChatColor.of("#" + hex));
                        player.sendMessage(textComponent);
                    });
                });
        this.setItem(
                4,
                3,
                ItemUtils.create(Material.NETHER_STAR, (meta) -> {
                    meta.setDisplayName(ChatColor.GOLD + "Сбросить все точки");
                    meta.setLore(Arrays.asList(
                            ChatColor.RED + "" + ChatColor.BOLD + "Все установленные точки трека",
                            ChatColor.RED + "" + ChatColor.BOLD + "будут БЕЗВОЗВРАТНО удалены"));
                }),
                event -> {
                    Player player = event.getPlayer();
                    player.closeInventory();

                    EditTrackPointsItem.clearAllPoints(activity.getLevel());
                    player.sendMessage("Все точки сброшены");
                });
        this.setItem(
                4,
                5,
                ItemUtils.create(Material.REDSTONE_TORCH, (meta) -> {
                    meta.setDisplayName(ChatColor.GOLD + "Покинуть редактор");
                    meta.setLore(List.of(ChatColor.YELLOW + "Блоки и настройки будут сохранены"));
                }),
                event -> {
                    Player player = event.getPlayer();
                    TeleportUtils.teleportAsync(this.plugin, player, Settings.getLobbySpawn())
                            .thenAccept(success -> {
                                if (!success) return;
                                this.plugin.get(ActivityManager.class).setActivity(player, null);
                            });
                });
        this.setItem(
                4,
                7,
                ItemUtils.create(Material.BARRIER, (meta) -> {
                    meta.setDisplayName(ChatColor.GOLD + "Удалить уровень");
                    meta.setLore(Arrays.asList(
                            ChatColor.RED + "" + ChatColor.BOLD + "Уровень будет удалён",
                            ChatColor.RED + "" + ChatColor.BOLD + "БЕЗ возможности восстановления"));
                }),
                event -> {
                    Player player = event.getPlayer();
                    player.closeInventory();

                    CommandDelete.deleteLevel(
                            plugin,
                            player,
                            activity.getLevel().getLevelSettings().getGameSettings());
                });
    }
}
