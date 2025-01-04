package io.github.alathra.alathraports.gui.tasks;

import io.github.alathra.alathraports.gui.tasks.base.BaseTask;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class CommandTask implements BaseTask {

	private final String commandString;
	private Boolean closeMenu = false;

	public CommandTask(String command) {
		this.commandString = command;
	}

	public CommandTask autoClose() {
		closeMenu = true;
		return this;
	}

	@Override
	public void run(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		player.chat(commandString);
		if (closeMenu) player.closeInventory();
	}

}
