package io.github.alathra.alathraports.gui.tasks;

import io.github.alathra.alathraports.gui.BaseMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryTask implements BaseTask {

	private BaseMenu menu;

	public InventoryTask(BaseMenu menu) {
		this.menu = menu;
	}

	@Override
	public void run(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		menu.open(player);
	}

}
