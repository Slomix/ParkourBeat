package ru.sortix.parkourbeat.editor.items;

import java.util.ArrayList;
import java.util.Arrays;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import ru.sortix.parkourbeat.levels.Level;
import ru.sortix.parkourbeat.levels.settings.LevelSettings;
import ru.sortix.parkourbeat.levels.settings.WorldSettings;

public class StartFinishItem extends EditorItem {

	private static final ItemStack startFinishItem;
	private static final int slot = 5;

	static {
		startFinishItem = new ItemStack(Material.STICK);
		ItemMeta meta = startFinishItem.getItemMeta();
		meta.setDisplayName("Зона старта/финиша");
		meta.setLore(
				new ArrayList<>(
						Arrays.asList("ПКМ - установить зону старта", "ЛКМ - установить зону финиша")));
		startFinishItem.setItemMeta(meta);
	}

	private Vector startPoint;
	private Vector finishPoint;

	public StartFinishItem(Player player, Level level) {
		super(startFinishItem.clone(), slot, player, level);
		WorldSettings worldSettings = level.getLevelSettings().getWorldSettings();
		startPoint = worldSettings.getStartBorder();
		finishPoint = worldSettings.getFinishBorder();
	}

	@Override
	public void onClick(Action action, Block block) {
		if (action == Action.RIGHT_CLICK_BLOCK) {
			finishPoint = block.getLocation().toVector();
			player.sendMessage("Finish border at: " + finishPoint);
			updateRegions();
		} else if (action == Action.LEFT_CLICK_BLOCK) {
			startPoint = block.getLocation().toVector();
			player.sendMessage("Start border at: " + startPoint);
			updateRegions();
		}
	}

	private void updateRegions() {
		LevelSettings levelSettings = level.getLevelSettings();
		WorldSettings worldSettings = levelSettings.getWorldSettings();
		worldSettings.setStartBorder(startPoint);
		worldSettings.setFinishBorder(finishPoint);
		levelSettings.updateDirectionChecker();
	}
}
