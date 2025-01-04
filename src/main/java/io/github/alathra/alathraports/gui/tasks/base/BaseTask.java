package io.github.alathra.alathraports.gui.tasks.base;

import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Tasks are assigned to slots in the BaseMenu.
 *
 * @author NinjaMandalorian
 */
public interface BaseTask {

	public void run(InventoryClickEvent event);

}
