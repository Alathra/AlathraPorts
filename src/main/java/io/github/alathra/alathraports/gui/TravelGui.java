package io.github.alathra.alathraports.gui;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.github.milkdrinkers.colorparser.ColorParser;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import io.github.alathra.alathraports.AlathraPorts;
import io.github.alathra.alathraports.command.PortsCommand;
import io.github.alathra.alathraports.config.Settings;
import io.github.alathra.alathraports.core.TravelNode;
import io.github.alathra.alathraports.core.carriagestations.CarriageStation;
import io.github.alathra.alathraports.core.ports.Port;
import io.github.alathra.alathraports.core.TravelNodesManager;
import io.github.alathra.alathraports.core.journey.Journey;
import io.github.alathra.alathraports.core.journey.JourneyManager;
import io.github.alathra.alathraports.utility.StringUtil;
import net.kyori.adventure.text.format.TextDecoration;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

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
        ItemStack nextPage = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta nextPageMeta = (SkullMeta) nextPage.getItemMeta();
        final UUID uuid1 = UUID.randomUUID();
        final PlayerProfile playerProfile1 = Bukkit.createProfile(uuid1, uuid1.toString().substring(0, 16));
        final String rightArrowTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19";
        playerProfile1.setProperty(new ProfileProperty("textures", rightArrowTexture));
        nextPageMeta.setPlayerProfile(playerProfile1);
        nextPageMeta.displayName(ColorParser.of("<yellow>Next Page").build().decoration(TextDecoration.ITALIC, false));
        nextPage.setItemMeta(nextPageMeta);
        base.setItem(6, 6, ItemBuilder.from(nextPage).asGuiItem(event -> {
            base.next();
        }));

        ItemStack prevPage = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta prevPageMeta = (SkullMeta) prevPage.getItemMeta();
        final UUID uuid2 = UUID.randomUUID();
        final PlayerProfile playerProfile2 = Bukkit.createProfile(uuid2, uuid2.toString().substring(0, 16));
        final String leftArrowTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==";
        playerProfile2.setProperty(new ProfileProperty("textures", leftArrowTexture));
        prevPageMeta.setPlayerProfile(playerProfile2);
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
        final List<Journey> journeys = new ArrayList<>();
        // Find possible journeys
        for (TravelNode reachableNode : travelNode.getPossibleConnections()) {
            Journey journey = new Journey(travelNode, reachableNode, player);
            if (numAnimals == -1) {
                journey.updateNumAnimals();
                numAnimals = journey.getNumAnimals();
            }
            journey.setNumAnimals(numAnimals);
            journeys.add(new Journey(travelNode, reachableNode, player));
        }
        // Re-order journeys from low to high based on cost
        journeys.sort(new Comparator<Journey>() {
            @Override
            public int compare(Journey j1, Journey j2) {
                return j1.getDestination().getName().compareTo(j2.getDestination().getName());
            }
        });

        // Place node buttons for each journey
        for (Journey journey : journeys) {
            ItemStack nodeItem = new ItemStack(journey.getDestination().getSize().getIcon());
            ItemMeta nodeItemMeta = nodeItem.getItemMeta();
            nodeItemMeta.displayName(ColorParser.of("<blue><bold>" + journey.getDestination().getName()).build().decoration(TextDecoration.ITALIC, false));
            if (JourneyManager.isPlayerInOngoingJourney(player)) {
                nodeItemMeta.lore(List.of(
                    ColorParser.of("<gold>Size: <red>" + journey.getDestination().getSize().getName()).build().decoration(TextDecoration.ITALIC, false)
                ));
                Journey ongoing = JourneyManager.getJourneyFromPlayer(player);
                if (ongoing != null) {
                    if (ongoing.getNodes().get(journey.getCurrentIndex()).equals(journey.getDestination())) {
                        nodeItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        nodeItemMeta.lore(List.of(
                            ColorParser.of("<gold>Size: <red>" + journey.getDestination().getSize().getName()).build().decoration(TextDecoration.ITALIC, false),
                            ColorParser.of("").build(),
                            ColorParser.of("<green>YOU ARE HERE").build().decoration(TextDecoration.ITALIC, false)
                        ));
                    }
                }
            } else {
                nodeItemMeta.lore(List.of(
                    ColorParser.of("<gold>Size: <red>" + journey.getDestination().getSize().getName()).build().decoration(TextDecoration.ITALIC, false),
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

    public static void generateTaxRateIcon(PaginatedGui gui, TravelNode travelNode) {
        if (!AlathraPorts.getTownyHook().isTownyLoaded()) {
            return;
        }
        if (travelNode.getTown() == null) {
            return;
        }

        ItemStack currentTaxItem = new ItemStack(Material.EMERALD);
        ItemMeta currentTaxMeta = currentTaxItem.getItemMeta();
        String townFeePercent = StringUtil.doubleToPercent(travelNode.getTownFee());;
        currentTaxMeta.displayName(ColorParser.of("<green>Current Tax Rate: " + townFeePercent).build().decoration(TextDecoration.ITALIC, false));
        currentTaxItem.setItemMeta(currentTaxMeta);
        gui.setItem(1, 4, ItemBuilder.from(currentTaxItem).asGuiItem());
    }

    public static void generateSetTaxButton(PaginatedGui gui, Player player, TravelNode travelNode) {
        if (!AlathraPorts.getTownyHook().isTownyLoaded()) {
            return;
        }
        TownyAPI townyAPI = AlathraPorts.getTownyHook().getTownyAPI();
        if (!player.hasPermission(PortsCommand.ADMIN_PERM)) {
            Resident resident = townyAPI.getResident(player);
            if (resident == null) {
                return;
            }
            if (travelNode.getTown() == null) {
                return;
            }
            Town town = resident.getTownOrNull();
            if (town == null) {
                return;
            }
            if (travelNode.getTown() != town) {
                return;
            }
            if (!town.getMayor().equals(resident)) {
                return;
            }
        }
        if (travelNode.getTown() == null) {
            return;
        }

        // Player is mayor of the town associated with the port OR a ports admin
        ItemStack taxButton = new ItemStack(Material.PAPER);
        ItemMeta taxButtonMeta = taxButton.getItemMeta();
        taxButtonMeta.displayName(ColorParser.of("<green>Set Tax Rate").build().decoration(TextDecoration.ITALIC, false));
        taxButton.setItemMeta(taxButtonMeta);
        gui.setItem(1, 6, ItemBuilder.from(taxButton).asGuiItem(event -> {
            GuiHandler.generateTaxGUI(player, travelNode);
        }));

    }


}
