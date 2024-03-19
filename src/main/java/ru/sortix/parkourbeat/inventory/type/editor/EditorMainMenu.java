package ru.sortix.parkourbeat.inventory.type.editor;

import static ru.sortix.parkourbeat.utils.LocationUtils.isValidSpawnPoint;

import java.util.Arrays;
import java.util.List;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
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
import ru.sortix.parkourbeat.utils.TeleportUtils;

public class EditorMainMenu extends ParkourBeatInventory {
    @SuppressWarnings("deprecation")
    public EditorMainMenu(@NonNull ParkourBeat plugin, @NonNull EditActivity activity) {
        super(plugin, 3, "Параметры уровня");
        this.setItem(
                2,
                1,
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
                player -> {
                    new SelectSongMenu(plugin, activity.getLevel()).open(player);
                });
        this.setItem(
                2,
                3,
                ItemUtils.create(Material.ENDER_PEARL, (meta) -> {
                    meta.setDisplayName(ChatColor.GOLD + "Точка спауна");
                    meta.setLore(Arrays.asList(
                            ChatColor.YELLOW + "Устанавливает точку спауна на уровне ваших ног.",
                            ChatColor.YELLOW + "Направление взгляда игроков будет точно таким же,",
                            ChatColor.YELLOW + "как при установке точки спауна"));
                }),
                player -> {
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
                5,
                ItemUtils.create(Material.REDSTONE_TORCH, (meta) -> {
                    meta.setDisplayName(ChatColor.GOLD + "Покинуть редактор");
                    meta.setLore(List.of(ChatColor.YELLOW + "Блоки и настройки будут сохранены"));
                }),
                player -> {
                    TeleportUtils.teleportAsync(this.plugin, player, Settings.getLobbySpawn())
                            .thenAccept(success -> {
                                if (!success) return;
                                this.plugin.get(ActivityManager.class).setActivity(player, null);
                            });
                });
        this.setItem(
                2,
                7,
                ItemUtils.create(Material.NETHER_STAR, (meta) -> {
                    meta.setDisplayName(ChatColor.GOLD + "Сбросить все точки");
                    meta.setLore(Arrays.asList(
                            ChatColor.RED + "" + ChatColor.BOLD + "Все установленные точки трека",
                            ChatColor.RED + "" + ChatColor.BOLD + "будут БЕЗВОЗВРАТНО удалены"));
                }),
                player -> {
                    player.closeInventory();
                    EditTrackPointsItem.clearAllPoints(activity.getLevel());
                    player.sendMessage("Все точки сброшены");
                });
        this.setItem(
                2,
                9,
                ItemUtils.create(Material.BARRIER, (meta) -> {
                    meta.setDisplayName(ChatColor.GOLD + "Удалить уровень");
                    meta.setLore(Arrays.asList(
                            ChatColor.RED + "" + ChatColor.BOLD + "Уровень будет удалён",
                            ChatColor.RED + "" + ChatColor.BOLD + "БЕЗ возможности восстановления"));
                }),
                player -> {
                    player.closeInventory();
                    CommandDelete.deleteLevel(
                            plugin,
                            player,
                            activity.getLevel().getLevelSettings().getGameSettings());
                });
    }
}
