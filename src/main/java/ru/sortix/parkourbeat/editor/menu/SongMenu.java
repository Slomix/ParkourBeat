package ru.sortix.parkourbeat.editor.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import ru.sortix.parkourbeat.data.Songs;
import ru.sortix.parkourbeat.levels.settings.GameSettings;

public class SongMenu implements InventoryHolder {

	private final int maxPage;
	private final Songs songs;
	private final Map<Integer, SongMenuItem> items;
	private final Player player;
	private final GameSettings gameSettings;
	private final Inventory inventory;
	private int page = 0;

	public SongMenu(Songs songs, Player player, GameSettings gameSettings) {
		this.inventory = Bukkit.createInventory(this, 54, "Список песен");
		this.songs = songs;
		this.maxPage = (songs.getAllSongs().size() + 1) / 45;
		this.items = new HashMap<>();
		this.player = player;
		this.gameSettings = gameSettings;
		initPage(0);
	}

	private void initPage(int page) {
		List<String> songs = new ArrayList<>(this.songs.getAllSongs());
		for (int slot = 0; slot < 45; slot++) {
			int songID = slot + 45 * page;
			if (songID < songs.size()) {
				String songName = songs.get(songID);
				SongMenuItem item =
						new SongItem(
								slot, this.songs.getSongPlaylist(songName), songName, player, gameSettings);
				inventory.setItem(slot, item.getItemStack());
				items.put(slot, item);
			}
		}

		if (page > 0) {
			SongMenuItem item = new ArrowItem(45, this, false);
			inventory.setItem(45, item.getItemStack());
			items.put(45, item);
		}
		if (page < maxPage) {
			SongMenuItem item = new ArrowItem(45, this, true);
			inventory.setItem(53, item.getItemStack());
			items.put(53, item);
		}
		SongMenuItem exitItem = new ExitItem(49, player);
		inventory.setItem(49, exitItem.getItemStack());
		items.put(49, exitItem);
	}

	public void onClick(int slot) {
		SongMenuItem item = items.get(slot);
		if (item != null) {
			item.onClick();
		}
	}

	public void next() {
		if (page < maxPage) {
			page++;
			initPage(page);
		}
	}

	public void previous() {
		if (page > 0) {
			page--;
			initPage(page);
		}
	}

	@Override
	public Inventory getInventory() {
		return inventory;
	}
}
