package io.github.alathra.alathraports.gui.tasks;

import io.github.alathra.alathraports.gui.menu.base.BaseMenu;
import io.github.alathra.alathraports.gui.tasks.base.BaseTask;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class PageTask implements BaseTask {

	private final int direction;

	public PageTask(int dir) {
		this.direction = dir;
	}

	@Override
	public void run(InventoryClickEvent event) {
		BaseMenu menu = (BaseMenu) event.getInventory().getHolder();
        if (menu != null) {
            menu.changePage((Player) event.getWhoClicked(), direction);
        }
	}
}
