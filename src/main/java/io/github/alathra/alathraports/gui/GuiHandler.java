package io.github.alathra.alathraports.gui;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import io.github.alathra.alathraports.core.TravelNode;
import org.bukkit.entity.Player;

public class GuiHandler {

    public static void generateTravelGui(Player player, TravelNode node) {
        PaginatedGui travelGui = TravelGui.generatePaginatedBase(node);
        TravelGui.generateNodeButtons(travelGui, player, node);
        TravelGui.generateOwnNodeIcon(travelGui, node);
        TravelGui.showBlockadedNodes(travelGui, node);
        TravelGui.generateStopJourneyButton(travelGui, player);
        TravelGui.generateTaxButton(travelGui, player, node);
        travelGui.open(player);
    }

    public static void generateTaxGUI(Player player, TravelNode node) {
        Gui taxGui = TaxGui.generateBase(node);
        TaxGui.generateCurrentTaxButton(taxGui, node);
        TaxGui.generateBackButton(taxGui, player, node);
        TaxGui.generateRaiseButton(taxGui, player, node);
        TaxGui.generateLowerButton(taxGui, player, node);
        taxGui.open(player);
    }
}
