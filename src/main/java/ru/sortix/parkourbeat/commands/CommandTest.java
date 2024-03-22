package ru.sortix.parkourbeat.commands;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;

import static ru.sortix.parkourbeat.constant.PermissionConstants.COMMAND_PERMISSION;

@Command(name = "test")
public class CommandTest {

    @Execute
    @Permission(COMMAND_PERMISSION + ".test")
    public String onCommand() {
        // TODO реализовать логику команды или удалить команду
        return "Nothing here";
    }
}
