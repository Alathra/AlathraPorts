package io.github.alathra.alathraports.listener.portlisteners;

import io.github.alathra.alathraports.gui.GuiHandler;
import io.github.alathra.alathraports.core.carriagestations.CarriageStation;
import io.github.alathra.alathraports.core.ports.Port;
import io.github.alathra.alathraports.core.TravelNodesManager;
import io.github.alathra.alathraports.utility.Logger;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignListeners implements Listener {

    @EventHandler
    public void onSignRightClick(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Block block = event.getClickedBlock();
            if (block == null) {
                return;
            }
            if (TravelNodesManager.isPortSign(block)) {
                Player player = event.getPlayer();
                Port port = TravelNodesManager.getPortFromSign(block);
                if (port == null) {
                    Logger.get().warn("Port Not Found: Port sign detected but could not associate with port");
                    return;
                }
                GuiHandler.generateTravelGui(player, port);
                event.setCancelled(true);
            } else if (TravelNodesManager.isCarriageStationSign(block)) {
                Player player = event.getPlayer();
                CarriageStation carriageStation = TravelNodesManager.getCarriageStationFromSign(block);
                if (carriageStation == null) {
                    Logger.get().warn("Carriage Station Not Found: Carriage station sign detected but could not associate with port");
                    return;
                }
                GuiHandler.generateTravelGui(player, carriageStation);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (TravelNodesManager.isPortSign(event.getBlock()) || TravelNodesManager.isCarriageStationSign(event.getBlock())) {
            // Prevent a port sign from being created by a player writing on a sign, must be done with admin command
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSignBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (TravelNodesManager.isPortSign(block) || TravelNodesManager.isCarriageStationSign(block) || TravelNodesManager.isAttachedToTravelNodeSign(block)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSignExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> TravelNodesManager.isPortSign(block) || TravelNodesManager.isCarriageStationSign(block) || TravelNodesManager.isAttachedToTravelNodeSign(block));
    }

    @EventHandler
    public void onSignExplodeByEntity(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> TravelNodesManager.isPortSign(block) || TravelNodesManager.isCarriageStationSign(block) || TravelNodesManager.isAttachedToTravelNodeSign(block));
    }

    @EventHandler
    public void onSignMovedByPistonExtension(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (TravelNodesManager.isPortSign(block) || TravelNodesManager.isCarriageStationSign(block) || TravelNodesManager.isAttachedToTravelNodeSign(block)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onSignMovedByPistonRetraction(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (TravelNodesManager.isPortSign(block) || TravelNodesManager.isCarriageStationSign(block) || TravelNodesManager.isAttachedToTravelNodeSign(block)) {
                event.setCancelled(true);
            }
        }
    }

    // Example: Enderman picks up block. I know this is stupid but why not
    @EventHandler
    public void onSignMovedByEntity(EntityChangeBlockEvent event) {
        if (TravelNodesManager.isPortSign(event.getBlock()) || TravelNodesManager.isCarriageStationSign(event.getBlock()) || TravelNodesManager.isAttachedToTravelNodeSign(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSignBurned(BlockBurnEvent event) {
        if (TravelNodesManager.isPortSign(event.getBlock()) || TravelNodesManager.isCarriageStationSign(event.getBlock()) || TravelNodesManager.isAttachedToTravelNodeSign(event.getBlock())) {
            event.setCancelled(true);
        }
    }
}
