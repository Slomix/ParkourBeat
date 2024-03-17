package ru.sortix.parkourbeat.item.editor.type;

import static ru.sortix.parkourbeat.utils.LocationUtils.isValidSpawnPoint;

import java.util.Arrays;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.activity.type.EditActivity;
import ru.sortix.parkourbeat.item.editor.EditorItem;
import ru.sortix.parkourbeat.levels.settings.LevelSettings;

public class SetSpawnPointItem extends EditorItem {
    @SuppressWarnings("deprecation")
    public SetSpawnPointItem(@NonNull ParkourBeat plugin, int slot) {
        super(plugin, slot, newStack(Material.ENDER_PEARL, (meta) -> {
            meta.setDisplayName(ChatColor.DARK_AQUA + "Точка спауна (см. описание)");
            meta.setLore(Arrays.asList(
                    ChatColor.YELLOW + "Устанавливает точку спауна на уровне ваших ног.",
                    ChatColor.YELLOW + "Направление взгляда игроков будет точно таким же,",
                    ChatColor.YELLOW + "как при установке точки спауна"));
        }));
    }

    @Override
    public void onUse(@NonNull PlayerInteractEvent event, @NonNull EditActivity activity) {
        Player player = event.getPlayer();
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
    }
}
