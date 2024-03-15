package ru.sortix.parkourbeat.commands;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;

@RequiredArgsConstructor
public abstract class PluginCommand<P extends JavaPlugin> implements CommandExecutor {
    protected final @NonNull P plugin;
}
