package ru.sortix.parkourbeat.commands;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import ru.sortix.parkourbeat.constant.Messages;

import static ru.sortix.parkourbeat.constant.PermissionConstants.COMMAND_PERMISSION;

@Command(name = "song")
public class CommandSong {

    @Execute
    @Permission(COMMAND_PERMISSION + ".song")
    public String onCommand() {
        return Messages.USE_LEVEL_PARAMETERS_ITEM;
    }
}
