package io.github.alathra.alathraports.gui;

import com.github.milkdrinkers.colorparser.ColorParser;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import io.github.alathra.alathraports.AlathraPorts;
import io.github.alathra.alathraports.config.Settings;
import io.github.alathra.alathraports.ports.Port;
import io.github.alathra.alathraports.ports.PortsManager;
import io.github.alathra.alathraports.ports.travel.Journey;
import io.github.alathra.alathraports.ports.travel.TravelManager;
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

    public static PaginatedGui generatePaginatedBase(Port port) {
        PaginatedGui base;
        // Set build settings
        base = Gui.paginated()
            .title(ColorParser.of("<blue>Port of " + port.getName()).build())
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

    public static void generatePortButtons(PaginatedGui gui, Player player, Port port) {

        final Economy economy = AlathraPorts.getVaultHook().getEconomy();
        // code to prevent animal calculations being run more than once
        int numAnimals = -1;
        for (Port reachablePort : port.getReachablePorts() ) {
            Journey journey = new Journey(port, reachablePort, player);
            if (numAnimals == -1) {
                journey.updateNumAnimals();
                numAnimals = journey.getNumAnimals();
            }
            journey.setNumAnimals(numAnimals);
            ItemStack portItem = new ItemStack(reachablePort.getSize().getIcon());
            ItemMeta portItemMeta = portItem.getItemMeta();
            portItemMeta.displayName(ColorParser.of("<blue><bold>" + reachablePort.getName()).build().decoration(TextDecoration.ITALIC, false));
            if (TravelManager.isPlayerInOngoingJourney(player)) {
                portItemMeta.lore(List.of(
                    ColorParser.of("<gold>Size: <red>" + reachablePort.getSize().getName()).build().decoration(TextDecoration.ITALIC, false)
                ));
                Journey ongoing = TravelManager.getJourneyFromPlayer(player);
                if (ongoing != null) {
                    if (ongoing.getNodes().get(journey.getCurrentIndex()).equals(reachablePort)) {
                        portItemMeta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, false);
                        portItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        portItemMeta.lore(List.of(
                            ColorParser.of("<gold>Size: <red>" + reachablePort.getSize().getName()).build().decoration(TextDecoration.ITALIC, false),
                            ColorParser.of("").build(),
                            ColorParser.of("<green>YOU ARE HERE").build().decoration(TextDecoration.ITALIC, false)
                        ));
                    }
                }
            } else {
                portItemMeta.lore(List.of(
                    ColorParser.of("<gold>Size: <red>" + reachablePort.getSize().getName()).build().decoration(TextDecoration.ITALIC, false),
                    ColorParser.of("<gold>Cost: " + economy.format(journey.getTotalCost())).build().decoration(TextDecoration.ITALIC, false),
                    ColorParser.of("<gold>Travel Time: " + journey.getTotalTime() + " seconds").build().decoration(TextDecoration.ITALIC, false),
                    ColorParser.of("").build(),
                    ColorParser.of("<green>Click to Travel").build().decoration(TextDecoration.ITALIC, false)
                ));
            }
            portItem.setItemMeta(portItemMeta);
            gui.addItem(ItemBuilder.from(portItem).asGuiItem(event -> {
                if (!TravelManager.isPlayerInOngoingJourney(player)) {
                    journey.start();
                    gui.close(player);
                }
            }));
        }
    }

    public static void showBlockadedPorts(PaginatedGui gui, Port port) {
        // Display blockaded ports
        if (Settings.SHOW_BLOCKADED) {
            for (Port potentiallyBlockadedPort : PortsManager.getPorts()) {
                if (potentiallyBlockadedPort.isBlockaded()) {
                    if (potentiallyBlockadedPort.equals(port)) {
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
        }
    }

    public static void generateOwnPortIcon(PaginatedGui gui, Port port) {

        // If port is blockaded
        if (port.isBlockaded()) {
            ItemStack portItem = new ItemStack(Settings.BLOCKADE_ICON);
            ItemMeta portItemMeta = portItem.getItemMeta();
            portItemMeta.displayName(ColorParser.of("<dark_red><bold>" + port.getName()).build().decoration(TextDecoration.ITALIC, false));
            portItemMeta.lore(List.of(
                ColorParser.of("<gold>Size: <red>" + port.getSize().getName()).build().decoration(TextDecoration.ITALIC, false),
                ColorParser.of("").build(),
                ColorParser.of("<dark_red>This port is being blockaded. All other ports are unreachable").build()
            ));
            portItemMeta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, false);
            portItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            portItem.setItemMeta(portItemMeta);
            gui.setItem(1, 5, ItemBuilder.from(portItem).asGuiItem());
            return;
        }

        // Places an icon at the top showing information about the port you are starting from in the travel menu
        ItemStack portItem = new ItemStack(port.getSize().getIcon());
        ItemMeta portItemMeta = portItem.getItemMeta();
        portItemMeta.displayName(ColorParser.of("<green><bold>" + port.getName()).build().decoration(TextDecoration.ITALIC, false));
        portItemMeta.lore(List.of(
            ColorParser.of("<gold>Size: <red>" + port.getSize().getName()).build().decoration(TextDecoration.ITALIC, false)
        ));
        portItemMeta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, false);
        portItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        portItem.setItemMeta(portItemMeta);
        gui.setItem(1, 5, ItemBuilder.from(portItem).asGuiItem());
    }

    public static void generateStopJourneyButton(PaginatedGui gui, Player player) {
        if (TravelManager.isPlayerInOngoingJourney(player)) {
            Journey ongoing = TravelManager.getJourneyFromPlayer(player);
            if (ongoing == null) {
                return;
            }
            ItemStack stopButton = new ItemStack(Material.BARRIER);
            ItemMeta stopButtonMeta = stopButton.getItemMeta();
            stopButtonMeta.displayName(ColorParser.of("<red>Stop journey").build().decoration(TextDecoration.ITALIC, false));
            stopButton.setItemMeta(stopButtonMeta);
            gui.setItem(6, 5, ItemBuilder.from(stopButton).asGuiItem(event -> {
                if (TravelManager.isPlayerInOngoingJourney(player)) {
                    ongoing.halt();
                    ongoing.stop();
                    gui.close(player);
                }
            }));
        }
    }

}
