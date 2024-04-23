package ru.sortix.parkourbeat.commands;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("ParkourBeat", NamedTextColor.LIGHT_PURPLE));
        sender.sendMessage(Component.text("/create - открывает меню для создания своего уровня", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/edit - открывает меню для редактирования ваших уровней", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/delete - удаляет ваш уровень", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/play - открывает меню с списком игровых уровней", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/tptoworld - наблюдать за определенным уровнем", NamedTextColor.YELLOW));
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("Административное", NamedTextColor.RED));
        sender.sendMessage(Component.text("/physicsdebug - включает отладку пользовательской физики", NamedTextColor.RED));
        sender.sendMessage(Component.text("/convertdata - конвертирует дату старого уровня в новый формат", NamedTextColor.RED));
        sender.sendMessage(Component.text("/test - включает тестовые функции", NamedTextColor.RED));
        sender.sendMessage(Component.text("/updatetrack - обновляет проблемный трек", NamedTextColor.RED));
        sender.sendMessage(Component.empty());
    }
}
