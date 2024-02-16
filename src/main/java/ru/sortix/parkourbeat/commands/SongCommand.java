package ru.sortix.parkourbeat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.sortix.parkourbeat.editor.EditorSession;
import ru.sortix.parkourbeat.editor.LevelEditorsManager;

public class SongCommand implements CommandExecutor {

  private final LevelEditorsManager levelEditorsManager;

  public SongCommand(LevelEditorsManager levelEditorsManager) {
    this.levelEditorsManager = levelEditorsManager;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage("Команда только для игроков!");
      return true;
    }
    Player player = (Player) sender;
    EditorSession editorSession = levelEditorsManager.getEditorSession(player);
    if (editorSession == null) {
      player.sendMessage("Вы не в режиме редактирования!");
      return true;
    }
    editorSession.openSongMenu();
    return true;
  }
}
