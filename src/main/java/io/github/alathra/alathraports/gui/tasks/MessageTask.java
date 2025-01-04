package io.github.alathra.alathraports.gui.tasks;

import io.github.alathra.alathraports.gui.tasks.base.BaseTask;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MessageTask implements BaseTask {

	private final String message;

	public MessageTask(String msg) {
		this.message = msg;
	}

	@Override
	public void run(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		player.sendMessage(message);
	}

}
