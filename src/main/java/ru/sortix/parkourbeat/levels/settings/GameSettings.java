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
    private @Nullable String songPlayListName;
    private @Nullable String songName;

    public void setSong(String playlist, String name) {
        this.songPlayListName = playlist;
        this.songName = name;
    }

    public boolean isOwner(@NonNull CommandSender sender) {
        if (sender instanceof Player) {
            if (this.ownerId.equals(((Player) sender).getUniqueId())) {
                return true;
            }
            if (sender.isOp()) {
                sender.sendMessage("Использован обход прав, поскольку вы являетесь оператором сервера");
                return true;
            }
            return false;
        }
        return sender instanceof ConsoleCommandSender;
    }
}
