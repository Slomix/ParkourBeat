package ru.sortix.parkourbeat.levels.settings;

import lombok.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class GameSettings {
    private final @NonNull UUID uniqueId;
    private final @Nullable String uniqueName;
    private final int uniqueNumber;

    private final @NonNull UUID ownerId;
    private final @NonNull String ownerName;
    @Setter
    private @NonNull Component displayName;

    private final long createdAtMills;
    private @Nullable Song song;
    private @Setter boolean customPhysicsEnabled;

    @NonNull
    public Component getDisplayName() {
        return this.displayName.colorIfAbsent(NamedTextColor.GOLD);
    }

    @NonNull
    public String getDisplayNameLegacy() {
        return LegacyComponentSerializer.legacySection().serialize(
            this.displayName.colorIfAbsent(NamedTextColor.GOLD));
    }

    @NonNull
    public String getDisplayNameLegacy(boolean useDefaultColor) {
        if (useDefaultColor) return this.getDisplayNameLegacy();
        return LegacyComponentSerializer.legacySection().serialize(
            this.displayName);
    }

    public void setSong(@NonNull Song song) {
        this.song = song;
    }

    public boolean isOwner(@NonNull UUID playerId) {
        return this.ownerId.equals(playerId);
    }

    public boolean isOwner(@NonNull CommandSender sender, boolean bypassForAdmins, boolean bypassMsg) {
        if (sender instanceof Player) {
            if (this.ownerId.equals(((Player) sender).getUniqueId())) {
                return true;
            }
            if (bypassForAdmins && sender.hasPermission("parkourbeat.restrictions.bypass")) {
                if (bypassMsg) sender.sendMessage("Использован обход прав, поскольку вы являетесь оператором сервера");
                return true;
            }
            return false;
        }
        return sender instanceof ConsoleCommandSender;
    }
}
