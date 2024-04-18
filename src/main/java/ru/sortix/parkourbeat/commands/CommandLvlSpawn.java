package ru.sortix.parkourbeat.commands;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.data.Settings;
import ru.sortix.parkourbeat.game.Game;
import ru.sortix.parkourbeat.world.TeleportUtils;

import static ru.sortix.parkourbeat.constant.PermissionConstants.COMMAND_PERMISSION;

@Command(
    name = "lvlspawn",
    aliases = {"levelspawn"})
@RequiredArgsConstructor
public class CommandLevelSpawn {

    private final ParkourBeat plugin;

    @Execute
    @Permission(COMMAND_PERMISSION + ".lvlspawn")
    public void onCommand(@Context Player player) {
        Game game = plugin.getGame(player);
        if (game == null || game.getCurrentState() != Game.State.RUNNING) {
            player.sendMessage("Вы не в редакторе!");
            return;
        }
        TeleportUtils.teleportAsync(this.plugin, player, game.getLevel().getSpawn());
    }
}
