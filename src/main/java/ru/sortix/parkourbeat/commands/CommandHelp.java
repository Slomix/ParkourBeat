package ru.sortix.parkourbeat.commands;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import ru.sortix.parkourbeat.ParkourBeat;

@Command(
    name = "pbhelp",
    aliases = {"parkourbeat", "pb?", "parkourbeat?", "parkourbeathelp"})
@RequiredArgsConstructor
public class CommandHelp {

    private final ParkourBeat plugin;

    @Execute
    public void onCommand(@Context CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage("§d§lParkourBeat");
        sender.sendMessage("§e/create §f- §e открывает меню для создания своего уровня");
        sender.sendMessage("§e/edit §f- §eоткрывает меню для редактирования ваших уровней");
        sender.sendMessage("§e/delete §f- §eудаляет ваш уровень");
        sender.sendMessage("§e/play §f- §eоткрывает меню с списком игровых уровней");
        sender.sendMessage("§e/tptoworld §f- §eнаблюдать за определенным уровнем");
        sender.sendMessage("");
        sender.sendMessage("§c§lАдминистративное");
        sender.sendMessage("§c/physicsdebug §f- §cвключает отладку пользовательской физики");
        sender.sendMessage("§c/convertdata §f- §cконвертирует дату старого уровня в новый формат");
        sender.sendMessage("§c/test §f- §cвключает тестовые функции");
        sender.sendMessage("§c/updatetrack §f- §cобновляет проблемный трек");
        sender.sendMessage("");
    }
}
