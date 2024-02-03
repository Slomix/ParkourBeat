package parkourbeat.commands;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import parkourbeat.ParkourBeat;
import parkourbeat.data.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TpToWorldCommand implements CommandExecutor, TabCompleter {

    public TpToWorldCommand() {

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length > 0) {
                String worldId = args[0].toLowerCase();

                World world = Bukkit.getWorld(worldId);
                if (world == null) {
                    sender.sendMessage("World not loaded!");
                    return true;
                }
                player.teleport(world.getSpawnLocation());
                player.setGameMode(GameMode.SPECTATOR);
                player.sendMessage("Teleported to parkourbeat level " + world.getName());
            } else {
                player.teleport(Settings.getExitLocation());
                player.sendMessage("Teleported to lobby");
                player.setGameMode(GameMode.ADVENTURE);
            }
        } else {
            sender.sendMessage("Command only for players!");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        ArrayList<String> tabComplete = new ArrayList<>();
        if (sender instanceof Player && args.length == 1) {
            for (String inGameWorld : ParkourBeat.getLevelsManager().getLoadedLevels()) {
                if (inGameWorld.toLowerCase().startsWith(args[0].toLowerCase())) {
                    tabComplete.add(inGameWorld);
                }
            }
        }
        return tabComplete;
    }
}
