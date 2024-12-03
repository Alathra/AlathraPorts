package io.github.alathra.alathraports.gui.tasks;

import io.github.alathra.alathraports.gui.BaseMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class PageTask implements BaseTask {

	private int direction;

	public PageTask(int dir) {
		this.direction = dir;
	}

	@Override
	public void run(InventoryClickEvent event) {
		BaseMenu menu = (BaseMenu) event.getInventory().getHolder();
		menu.changePage((Player) event.getWhoClicked(), direction);
	}
}
