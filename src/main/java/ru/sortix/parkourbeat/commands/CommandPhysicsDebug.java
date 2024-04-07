package ru.sortix.parkourbeat.commands;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.flag.Flag;
import dev.rollczi.litecommands.annotations.permission.Permission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.constant.Messages;
import ru.sortix.parkourbeat.physics.CustomPhysicsManager;

import static ru.sortix.parkourbeat.constant.PermissionConstants.COMMAND_PERMISSION;

@Command(
    name = "toggle-physics-debug",
    aliases = {"debugp", "debug-physics", "debugphysics"}
)
public class CommandPhysicsDebug {

    private final CustomPhysicsManager customPhysicsManager;

    public CommandPhysicsDebug(ParkourBeat plugin) {
        this.customPhysicsManager = plugin.get(CustomPhysicsManager.class);
    }

    @Execute
    @Permission(COMMAND_PERMISSION + ".toggle-physics-debug")
    public void onCommand(@Context Player player, @Flag("-f") boolean force) {
        boolean enabled = customPhysicsManager.getDebugViewerRegistry().toggleDebug(player);
        player.sendMessage(Component.text(
            String.format(Messages.PHYSICS_DEBUG_SWITCHED, enabled ? "включена" : "выключена"),
            enabled ? NamedTextColor.GREEN : NamedTextColor.RED
        ));

        if (force) {
            if (enabled) {
                customPhysicsManager.addPlayer(player, null);
            } else {
                customPhysicsManager.purgePlayer(player);
            }
        }
    }

}
