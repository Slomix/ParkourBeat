package ru.sortix.parkourbeat.commands;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.ParkourBeat;
import ru.sortix.parkourbeat.inventory.type.CreateLevelMenu;

import static ru.sortix.parkourbeat.constant.PermissionConstants.COMMAND_PERMISSION;

@Command(name = "create")
@RequiredArgsConstructor
public class CommandCreate {

    private final ParkourBeat plugin;

    @Execute
    @Permission(COMMAND_PERMISSION + ".create")
    public void onCommand(@Context Player sender) {
        new CreateLevelMenu(this.plugin).open((Player) sender);
    }
}
