package io.github.alathra.alathraports.listener.portlisteners.external;

import io.github.milkdrinkers.colorparser.ColorParser;
import com.palmergames.bukkit.towny.event.PreDeleteTownEvent;
import io.github.alathra.alathraports.config.Settings;
import io.github.alathra.alathraports.core.ports.Port;
import io.github.alathra.alathraports.core.TravelNodesManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TownyListeners implements Listener {
    @EventHandler
    public void onTownDelete(PreDeleteTownEvent event) {
        if (!Settings.REMOVE_PORT_ON_TOWN_DELETE) {
            return;
        }
        // Remove all ports associated with this town
        for (Port port : TravelNodesManager.getPorts()) {
            if (port.getTown() != null) {
                if (port.getTown().equals(event.getTown())) {
                    // Remove the port. Null is here since it is being deleted programmatically (no player)
                    for (Player player : port.getWorld().getPlayers()) {
                        player.sendMessage(ColorParser.of("<yellow>The port of <light_purple>" + port.getName() + " <yellow>has been abandoned").build());
                    }
                    TravelNodesManager.deleteTravelNodeWithSign(null, port);
                }
            }
        }
    }
}
