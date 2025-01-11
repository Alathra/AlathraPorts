package io.github.alathra.alathraports.gui;

import dev.triumphteam.gui.guis.PaginatedGui;
import io.github.alathra.alathraports.travelnodes.TravelNode;
import io.github.alathra.alathraports.travelnodes.ports.Port;
import org.bukkit.entity.Player;

public class GuiHandler {

    public static void generateTravelGui(Player player, TravelNode node) {
        PaginatedGui travelGui = TravelGui.generatePaginatedBase(node);
        TravelGui.generateNodeButtons(travelGui, player, node);
        TravelGui.generateOwnNodeIcon(travelGui, node);
        TravelGui.showBlockadedNodes(travelGui, node);
        TravelGui.generateStopJourneyButton(travelGui, player);
        travelGui.open(player);
    }
}
