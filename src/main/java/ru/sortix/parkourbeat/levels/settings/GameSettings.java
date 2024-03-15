package ru.sortix.parkourbeat.levels.settings;

import java.util.UUID;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class GameSettings {
    private final @NonNull UUID levelId;
    private final @NonNull String levelName;
    private final @NonNull UUID ownerId;
    private final @NonNull String ownerName;
    private @Nullable Song song;

    public void setSong(@NonNull Song song) {
        this.song = song;
    }

    public boolean isOwner(@NonNull CommandSender sender, boolean bypassAdmins, boolean bypassMsg) {
        if (sender instanceof Player) {
            if (this.ownerId.equals(((Player) sender).getUniqueId())) {
                return true;
            }
            if (bypassAdmins && sender.hasPermission("parkourbeat.restrictions.bypass")) {
                if (bypassMsg) sender.sendMessage("Использован обход прав, поскольку вы являетесь оператором сервера");
                return true;
            }
            return false;
        }
        return sender instanceof ConsoleCommandSender;
    }
}
