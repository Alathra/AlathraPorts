package io.github.alathra.alathraports.listener.portlisteners;

import com.github.milkdrinkers.colorparser.ColorParser;
import io.github.alathra.alathraports.ports.Ports;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Objects;

public class SignListeners implements Listener {

    @EventHandler
    public void onSignRightClick(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (Tag.ALL_SIGNS.isTagged(Objects.requireNonNull(event.getClickedBlock()).getType())) {
                Sign sign = (Sign) event.getClickedBlock().getState();
                if (isPortSign(sign)) {
                    // TODO: open port menu
                    event.getPlayer().sendMessage(ColorParser.of("<green> Open port menu").build());
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Sign sign = (Sign) event.getBlock().getState();
        if (isPortSign(sign)) {
            // Prevent a port sign from being created by a player writing on a sign, must be done with admin command
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSignBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (event.getBlock().getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            if (isPortSign(sign)) {
                event.setCancelled(true);
            }
        }
    }

    private boolean isPortSign(Sign sign) {
        return sign.getSide(Side.FRONT).line(0).equals(Ports.getTagline()) &&
            sign.getSide(Side.FRONT).line(3).equals(Ports.getTagline()) &&
            sign.getSide(Side.BACK).line(0).equals(Ports.getTagline()) &&
            sign.getSide(Side.BACK).line(3).equals(Ports.getTagline());
    }
}
