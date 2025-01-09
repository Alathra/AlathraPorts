package io.github.alathra.alathraports.gui;

import dev.triumphteam.gui.guis.PaginatedGui;
import io.github.alathra.alathraports.ports.Port;
import org.bukkit.entity.Player;

public class GuiHandler {

    public static void generateTravelGui(Player player, Port port) {
        PaginatedGui travelGui = TravelGui.generatePaginatedBase(port);
        TravelGui.generatePortButtons(travelGui, player, port);
        TravelGui.generateOwnPortIcon(travelGui, port);
        TravelGui.showBlockadedPorts(travelGui, port);
        TravelGui.generateStopJourneyButton(travelGui, player);
        travelGui.open(player);
    }
}
