package io.github.alathra.alathraports.gui.tasks;

import io.github.alathra.alathraports.ports.Port;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;
import java.util.List;

public class JourneyTask implements BaseTask {

	//private List<Port> ports;

	public JourneyTask(Port origin, Port destination) {
		//ports = Arrays.asList(origin, destination);
	}

	@Override
	public void run(InventoryClickEvent event) {
		//TravelHandler.startJourney((Player) event.getWhoClicked(), ports.get(0), ports.get(1));
		event.getWhoClicked().closeInventory();
	}

}
