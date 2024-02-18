package ru.sortix.parkourbeat.editor.menu;

import org.bukkit.inventory.ItemStack;

public abstract class SongMenuItem {

	private final int slot;

	public SongMenuItem(int slot) {
		this.slot = slot;
	}

	public int getSlot() {
		return slot;
	}

	public abstract ItemStack getItemStack();

	public abstract void onClick();
}
