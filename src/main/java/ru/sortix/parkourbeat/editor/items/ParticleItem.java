package ru.sortix.parkourbeat.editor.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import ru.sortix.parkourbeat.levels.Level;
import ru.sortix.parkourbeat.levels.ParticleController;
import ru.sortix.parkourbeat.location.Waypoint;

public class ParticleItem extends EditorItem {

	private static final ItemStack particleItem;
	private static final int slot = 0;

	static {
		particleItem = new ItemStack(Material.BLAZE_ROD);
		ItemMeta meta = particleItem.getItemMeta();
		meta.setDisplayName("Путь");
		meta.setLore(
				new ArrayList<>(
						Arrays.asList(
								"ПКМ - первая точка",
								"ЛКМ - вторая точка",
								"SHIFT + ПКМ/ЛКМ - управление высотой")));
		particleItem.setItemMeta(meta);
	}

	private Color currentColor;
	private double currentHeight;

	public ParticleItem(Player player, Level level) {
		super(particleItem.clone(), slot, player, level);
		currentColor = Color.BLACK;
		currentHeight = 0;
	}

	public void setCurrentColor(Color currentColor) {
		this.currentColor = currentColor;
	}

	@Override
	public void onClick(Action action, Block block) {
		AtomicBoolean change = new AtomicBoolean(false);

		ArrayList<Waypoint> waypoints = level.getLevelSettings().getWorldSettings().getWaypoints();
		if (player.isSneaking()
				&& (action == Action.RIGHT_CLICK_AIR || action == Action.LEFT_CLICK_AIR)) {
			Waypoint startSegment = getLookingSegment(player, waypoints);
			if (startSegment != null) {
				if (action == Action.RIGHT_CLICK_AIR) {
					currentHeight =
							Math.min(255 - startSegment.getLocation().getY(), startSegment.getHeight() + 0.5);
				} else {
					currentHeight = Math.max(0, startSegment.getHeight() - 0.5);
				}
				startSegment.setHeight(currentHeight);
				change.set(true);
			}
		}
		if (!player.isSneaking()) {
			Location particleLoc = block.getLocation().add(0.5, 1, 0.5);
			if (action == Action.RIGHT_CLICK_BLOCK) {
				waypoints.stream()
						.filter(waypoint -> waypoint.getLocation().distance(particleLoc) < 2)
						.findFirst()
						.ifPresent(
								waypoint -> {
									waypoints.remove(waypoint);
									player.sendMessage("Вы успешно удалили точку.");
									change.set(true);
								});
			}
			if (action == Action.LEFT_CLICK_BLOCK) {
				waypoints.add(new Waypoint(particleLoc, currentColor, currentHeight));
				player.sendMessage("Вы успешно добавили точку.");
				change.set(true);
			}
		}

		if (change.get()) {
			ParticleController particleController = level.getLevelSettings().getParticleController();
			particleController.stopSpawnParticles(player);
			particleController.loadParticleLocations(waypoints);
			particleController.startSpawnParticles(player);
		}
	}

	private Waypoint getLookingSegment(Player player, List<Waypoint> waypoints) {

		for (int i = 0; i < waypoints.size() - 1; i++) {
			Waypoint startSegment = waypoints.get(i);
			Waypoint endSegment = waypoints.get(i + 1);

			if (isLookingAt(
					player, startSegment.getLocation().toVector(), endSegment.getLocation().toVector())) {
				return startSegment;
			}
		}
		return null;
	}

	public boolean isLookingAt(Player player, Vector block1, Vector block2) {
		Vector toBlock1 = block1.subtract(player.getEyeLocation().toVector()).setY(0);
		Vector toBlock2 = block2.subtract(player.getEyeLocation().toVector()).setY(0);

		Vector playerDirection = player.getEyeLocation().getDirection().setY(0);

		Vector cross1 = playerDirection.getCrossProduct(toBlock1);
		Vector cross2 = playerDirection.getCrossProduct(toBlock2);

		double dot = cross1.dot(cross2);

		boolean sameHalfPlane = playerDirection.dot(toBlock1) > 0 && playerDirection.dot(toBlock2) > 0;

		return dot < 0 && sameHalfPlane;
	}
}
