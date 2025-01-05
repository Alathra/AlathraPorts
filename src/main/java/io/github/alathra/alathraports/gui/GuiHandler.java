package io.github.alathra.alathraports.gui;

import dev.triumphteam.gui.guis.PaginatedGui;
import io.github.alathra.alathraports.ports.Port;
import org.bukkit.entity.Player;

public class GuiHandler {

    public static void generateTravelGui(Player player, Port port) {
        PaginatedGui travelGui = GuiUtil.generatePaginatedBase();
        GuiUtil.generatePortButtons(travelGui, player, port);
        GuiUtil.generateOwnPortIcon(travelGui, port);
        travelGui.open(player);
    }
}
