package io.github.alathra.alathraports.gui;

import com.github.milkdrinkers.colorparser.ColorParser;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import io.github.alathra.alathraports.AlathraPorts;
import io.github.alathra.alathraports.config.Settings;
import io.github.alathra.alathraports.travelnodes.TravelNode;
import io.github.alathra.alathraports.travelnodes.carriagestations.CarriageStation;
import io.github.alathra.alathraports.travelnodes.ports.Port;
import io.github.alathra.alathraports.travelnodes.TravelNodesManager;
import io.github.alathra.alathraports.travelnodes.journey.Journey;
import io.github.alathra.alathraports.travelnodes.journey.JourneyManager;
import net.kyori.adventure.text.format.TextDecoration;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class TravelGui {

    public static PaginatedGui generatePaginatedBase(TravelNode travelNode) {
        PaginatedGui base;
        // Set build settings
        base = Gui.paginated()
            .title(ColorParser.of("<blue>" + travelNode.getName() + " - Travel Menu").build())
            .rows(6)
            .disableItemPlace()
            .disableItemSwap()
            .disableItemDrop()
            .disableItemTake()
            .create();

        // Apply gray glass pane border
        ItemStack grayBorder = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta grayBorderItemMeta = grayBorder.getItemMeta();
        grayBorderItemMeta.displayName(ColorParser.of("").build());
        grayBorder.setItemMeta(grayBorderItemMeta);
        base.getFiller().fillBorder(ItemBuilder.from(grayBorder).asGuiItem());

        // Create page nav buttons
        ItemStack nextPage = new ItemStack(Material.ARROW);
        ItemMeta nextPageMeta = nextPage.getItemMeta();
        nextPageMeta.displayName(ColorParser.of("<yellow>Next Page").build().decoration(TextDecoration.ITALIC, false));
        nextPage.setItemMeta(nextPageMeta);
        base.setItem(6, 6, ItemBuilder.from(nextPage).asGuiItem(event -> {
            base.next();
        }));

        ItemStack prevPage = new ItemStack(Material.ARROW);
        ItemMeta prevPageMeta = prevPage.getItemMeta();
        prevPageMeta.displayName(ColorParser.of("<yellow>Previous Page").build().decoration(TextDecoration.ITALIC, false));
        prevPage.setItemMeta(prevPageMeta);
        base.setItem(6, 4, ItemBuilder.from(prevPage).asGuiItem(event -> {
            base.previous();
        }));

        return base;
    }

    public static void generateNodeButtons(PaginatedGui gui, Player player, TravelNode travelNode) {

        final Economy economy = AlathraPorts.getVaultHook().getEconomy();
        // code to prevent animal calculations being run more than once
        int numAnimals = -1;
        for (TravelNode reachableNode : travelNode.getPossibleConnections() ) {
            Journey journey = new Journey(travelNode, reachableNode, player);
            if (numAnimals == -1) {
                journey.updateNumAnimals();
                numAnimals = journey.getNumAnimals();
            }
            journey.setNumAnimals(numAnimals);
            ItemStack nodeItem = new ItemStack(reachableNode.getSize().getIcon());
            ItemMeta nodeItemMeta = nodeItem.getItemMeta();
            nodeItemMeta.displayName(ColorParser.of("<blue><bold>" + reachableNode.getName()).build().decoration(TextDecoration.ITALIC, false));
            if (JourneyManager.isPlayerInOngoingJourney(player)) {
                nodeItemMeta.lore(List.of(
                    ColorParser.of("<gold>Size: <red>" + reachableNode.getSize().getName()).build().decoration(TextDecoration.ITALIC, false)
                ));
                Journey ongoing = JourneyManager.getJourneyFromPlayer(player);
                if (ongoing != null) {
                    if (ongoing.getNodes().get(journey.getCurrentIndex()).equals(reachableNode)) {
                        nodeItemMeta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, false);
                        nodeItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        nodeItemMeta.lore(List.of(
                            ColorParser.of("<gold>Size: <red>" + reachableNode.getSize().getName()).build().decoration(TextDecoration.ITALIC, false),
                            ColorParser.of("").build(),
                            ColorParser.of("<green>YOU ARE HERE").build().decoration(TextDecoration.ITALIC, false)
                        ));
                    }
                }
            } else {
                nodeItemMeta.lore(List.of(
                    ColorParser.of("<gold>Size: <red>" + reachableNode.getSize().getName()).build().decoration(TextDecoration.ITALIC, false),
                    ColorParser.of("<gold>Cost: " + economy.format(journey.getTotalCost())).build().decoration(TextDecoration.ITALIC, false),
                    ColorParser.of("<gold>Travel Time: " + journey.getTotalTime() + " seconds").build().decoration(TextDecoration.ITALIC, false),
                    ColorParser.of("").build(),
                    ColorParser.of("<green>Click to Travel").build().decoration(TextDecoration.ITALIC, false)
                ));
            }
            nodeItem.setItemMeta(nodeItemMeta);
            gui.addItem(ItemBuilder.from(nodeItem).asGuiItem(event -> {
                if (!JourneyManager.isPlayerInOngoingJourney(player)) {
                    journey.start();
                    gui.close(player);
                }
            }));
        }
    }

    public static void showBlockadedNodes(PaginatedGui gui, TravelNode travelNode) {
        // Display blockaded ports
        if (Settings.SHOW_BLOCKADED) {
            switch(travelNode.getType()) {
                case PORT:
                    for (Port potentiallyBlockadedPort : TravelNodesManager.getPorts()) {
                        if (potentiallyBlockadedPort.isBlockaded()) {
                            if (potentiallyBlockadedPort.equals(travelNode)) {
                                continue;
                            }
                            ItemStack blockadedPortItem = new ItemStack(Settings.BLOCKADE_ICON);
                            ItemMeta blockadedPortMeta = blockadedPortItem.getItemMeta();
                            blockadedPortMeta.displayName(ColorParser.of("<dark_red><bold>" + potentiallyBlockadedPort.getName()).build().decoration(TextDecoration.ITALIC, false));
                            blockadedPortMeta.lore(List.of(
                                ColorParser.of("<gold>Size: <red>" + potentiallyBlockadedPort.getSize().getName()).build().decoration(TextDecoration.ITALIC, false),
                                ColorParser.of("").build(),
                                ColorParser.of("<dark_red>This port is being blockaded and is unreachable everywhere").build()
                            ));
                            blockadedPortMeta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, false);
                            blockadedPortMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                            blockadedPortItem.setItemMeta(blockadedPortMeta);
                            gui.addItem(ItemBuilder.from(blockadedPortItem).asGuiItem());
                        }
                    }
                    break;
                case CARRIAGE_STATION:
                    for (CarriageStation potentiallyBlockadedCarriageStation : TravelNodesManager.getCarriageStations()) {
                        if (potentiallyBlockadedCarriageStation.isBlockaded()) {
                            if (potentiallyBlockadedCarriageStation.equals(travelNode)) {
                                continue;
                            }
                            ItemStack blockadedCarriageStationItem = new ItemStack(Settings.BLOCKADE_ICON);
                            ItemMeta blockadedCarriageStationMeta = blockadedCarriageStationItem.getItemMeta();
                            blockadedCarriageStationMeta.displayName(ColorParser.of("<dark_red><bold>" + potentiallyBlockadedCarriageStation.getName()).build().decoration(TextDecoration.ITALIC, false));
                            blockadedCarriageStationMeta.lore(List.of(
                                ColorParser.of("<gold>Size: <red>" + potentiallyBlockadedCarriageStation.getSize().getName()).build().decoration(TextDecoration.ITALIC, false),
                                ColorParser.of("").build(),
                                ColorParser.of("<dark_red>This carriage station is being blockaded and is unreachable everywhere").build()
                            ));
                            blockadedCarriageStationMeta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, false);
                            blockadedCarriageStationMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                            blockadedCarriageStationItem.setItemMeta(blockadedCarriageStationMeta);
                            gui.addItem(ItemBuilder.from(blockadedCarriageStationItem).asGuiItem());
                        }
                    }
                    break;
            }
        }
    }

    public static void generateOwnNodeIcon(PaginatedGui gui, TravelNode travelNode) {

        // If travel node is blockaded
        if (travelNode.isBlockaded()) {
            switch (travelNode.getType()) {
                case PORT:
                    ItemStack portItem = new ItemStack(Settings.BLOCKADE_ICON);
                    ItemMeta portItemMeta = portItem.getItemMeta();
                    portItemMeta.displayName(ColorParser.of("<dark_red><bold>" + travelNode.getName()).build().decoration(TextDecoration.ITALIC, false));
                    portItemMeta.lore(List.of(
                        ColorParser.of("<gold>Size: <red>" + travelNode.getSize().getName()).build().decoration(TextDecoration.ITALIC, false),
                        ColorParser.of("").build(),
                        ColorParser.of("<dark_red>This port is being blockaded. All other ports are unreachable").build()
                    ));
                    portItemMeta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, false);
                    portItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    portItem.setItemMeta(portItemMeta);
                    gui.setItem(1, 5, ItemBuilder.from(portItem).asGuiItem());
                    return;
                case CARRIAGE_STATION:
                    ItemStack carriageStationItem = new ItemStack(Settings.BLOCKADE_ICON);
                    ItemMeta carriageStationItemMeta = carriageStationItem.getItemMeta();
                    carriageStationItemMeta.displayName(ColorParser.of("<dark_red><bold>" + travelNode.getName()).build().decoration(TextDecoration.ITALIC, false));
                    carriageStationItemMeta.lore(List.of(
                        ColorParser.of("<gold>Size: <red>" + travelNode.getSize().getName()).build().decoration(TextDecoration.ITALIC, false),
                        ColorParser.of("").build(),
                        ColorParser.of("<dark_red>This carriage station is being blockaded. All other carriage stations are unreachable").build()
                    ));
                    carriageStationItemMeta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, false);
                    carriageStationItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    carriageStationItem.setItemMeta(carriageStationItemMeta);
                    gui.setItem(1, 5, ItemBuilder.from(carriageStationItem).asGuiItem());
                    return;
            }
        }

        // Places an icon at the top showing information about the port you are starting from in the travel menu
        ItemStack nodeItem = new ItemStack(travelNode.getSize().getIcon());
        ItemMeta nodeItemMeta = nodeItem.getItemMeta();
        nodeItemMeta.displayName(ColorParser.of("<green><bold>" + travelNode.getName()).build().decoration(TextDecoration.ITALIC, false));
        nodeItemMeta.lore(List.of(
            ColorParser.of("<gold>Size: <red>" + travelNode.getSize().getName()).build().decoration(TextDecoration.ITALIC, false)
        ));
        nodeItemMeta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, false);
        nodeItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        nodeItem.setItemMeta(nodeItemMeta);
        gui.setItem(1, 5, ItemBuilder.from(nodeItem).asGuiItem());
    }

    public static void generateStopJourneyButton(PaginatedGui gui, Player player) {
        if (JourneyManager.isPlayerInOngoingJourney(player)) {
            Journey ongoing = JourneyManager.getJourneyFromPlayer(player);
            if (ongoing == null) {
                return;
            }
            ItemStack stopButton = new ItemStack(Material.BARRIER);
            ItemMeta stopButtonMeta = stopButton.getItemMeta();
            stopButtonMeta.displayName(ColorParser.of("<red>Stop journey").build().decoration(TextDecoration.ITALIC, false));
            stopButton.setItemMeta(stopButtonMeta);
            gui.setItem(6, 5, ItemBuilder.from(stopButton).asGuiItem(event -> {
                if (JourneyManager.isPlayerInOngoingJourney(player)) {
                    ongoing.halt();
                    ongoing.stop();
                    gui.close(player);
                }
            }));
        }
    }

}
