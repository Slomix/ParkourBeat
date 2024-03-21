package ru.sortix.parkourbeat.commands;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import ru.sortix.parkourbeat.constant.Messages;

import static ru.sortix.parkourbeat.constant.PermissionConstants.COMMAND_PERMISSION;

@Command(name = "color")
public class CommandColor {

    @Execute
    @Permission(COMMAND_PERMISSION + ".color")
    public String onExecute() {
        return Messages.USE_LEVEL_PARAMETERS_ITEM;
    }
}
