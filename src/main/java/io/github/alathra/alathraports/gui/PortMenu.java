package io.github.alathra.alathraports.gui;

import com.github.milkdrinkers.colorparser.ColorParser;
import io.github.alathra.alathraports.AlathraPorts;
import io.github.alathra.alathraports.gui.tasks.InventoryTask;
import io.github.alathra.alathraports.gui.tasks.JourneyTask;
import io.github.alathra.alathraports.ports.Port;
import io.github.alathra.alathraports.ports.PortsManager;
import io.github.alathra.alathraports.ports.algo.TravelHandler;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PortMenu {

	/**
	 * Creates the base menu for a port, showing all travel-able ports and a button to open global menu.
	 *
	 * @param player - Player opening menu
	 * @param port   - Port to generate menu from
	 * @return BaseMenu of port
	 */
	public static BaseMenu createPortMenu(Player player, Port port) {

		BaseMenu.PagedBuilder builder = BaseMenu.createPagedBuilder()
			.makePageButtons()
			.setButton(4, portToButton(player, port, port))
			.setButton(49, BaseButton.create(Material.OAK_CHEST_BOAT).glow()
                .name(ColorParser.of("<em><green>See all ports").build())
				.task(new InventoryTask(createPortGlobalMenu(player, port))))
			.title(ColorParser.of("<blue>Port - " + port.getName()).build())
            .openMsg(ColorParser.of("<blue>[<gold>AlathraPorts<blue>] <green>Opening port <blue>" + port.getName() + "<green>...").build());

		ArrayList<BaseButton> buttonList = new ArrayList<>();
		for (Port availablePort : PortsManager.orderPorts(port, port.getNearby())) {
			buttonList.add(portToButton(player, port, availablePort));
		}
		builder = builder.setContents(buttonList);

		return builder.build();

	}

	/**
	 * Creates the global menu for a port, where every public port is in view
	 *
	 * @param player - Player opening menu
	 * @param port   - Port to generate menu from
	 * @return BaseMenu of port
	 */
	public static BaseMenu createPortGlobalMenu(Player player, Port port) {

		BaseMenu.PagedBuilder builder = BaseMenu.createPagedBuilder()
			.makePageButtons()
			.setButton(4, portToButton(player, port, port))
            .title(ColorParser.of("<red>Global Port Menu").build())
            .openMsg(ColorParser.of("<blue>[<gold>AlathraPorts<blue>] <green>Opening <red>Global Port Menu <green>...").build());

		ArrayList<BaseButton> buttonList = new ArrayList<>();
		for (Port availablePort : PortsManager.orderPorts(port, new ArrayList<>(PortsManager.getPorts()))) {
			buttonList.add(portToButton(player, port, availablePort));
		}
		builder = builder.setContents(buttonList);

		return builder.build();
	}

	public static BaseButton portToButton(Player player, Port currentPort, Port port) {

		BaseButton portButton = BaseButton.create();
        final Economy economy = AlathraPorts.getVaultHook().getEconomy();
        List<Component> lore = new ArrayList<>();

		if (currentPort.equals(port)) {
			// Creating button for current port.
			portButton = portButton.itemStack(new ItemStack(port.getSize().getIcon()));
			portButton = portButton.glow().name(ColorParser.of(port.getName()).build());

            //lore.add(ChatColor.GOLD + "Size: " + port.getSize().getName());
            lore.add(ColorParser.of("<gold>Size: " + port.getSize().getName()).build());

			portButton = portButton.lore(lore).task(null);
		} else {
			// Creating button for travel-able port.
			portButton = portButton.itemStack(new ItemStack(port.getSize().getIcon()));
			portButton = portButton.name(ColorParser.of(port.getName()).build());

			if (currentPort.getNearby().contains(port)) {
				List<Port> path = TravelHandler.findPath(player, currentPort, port);
				lore = Arrays.asList(
                    ColorParser.of("<gold>Size: " + port.getSize().getName()).build(),
                    ColorParser.of("<gold>Travel Time: " + (TravelHandler.getJourneyWait(path) / 20L)).build(),
                    ColorParser.of("<gold>Cost: " + economy.format(TravelHandler.getJourneyCost(path))).build(),
                    ColorParser.of("<green>Click to Travel").build()
				);
			} else {
				lore = Arrays.asList(
                    ColorParser.of("<gold>Size: " + port.getSize().getName()).build(),
                    ColorParser.of("<green>Click to Begin Your Journey").build()
				);
			}


			portButton = portButton.lore(lore);
			portButton = portButton.task(new JourneyTask(currentPort, port));
		}

		return portButton;
	}

}