package io.github.alathra.alathraports.listener.portlisteners;

import io.github.alathra.alathraports.ports.PortSign;
import org.bukkit.Tag;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Objects;

public class SignListeners implements Listener {

    @EventHandler
    public void onSignRightClick(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (Tag.ALL_SIGNS.isTagged(Objects.requireNonNull(event.getClickedBlock()).getType())) {
                Sign sign = (Sign) event.getClickedBlock();
                // Check if sign is a port sign
                if (sign.getSide(Side.FRONT).line(0).equals(PortSign.getTagline()) &&
                    sign.getSide(Side.FRONT).line(3).equals(PortSign.getTagline()) &&
                    sign.getSide(Side.BACK).line(0).equals(PortSign.getTagline()) &&
                    sign.getSide(Side.BACK).line(3).equals(PortSign.getTagline())
                ) {
                    // TODO: open port menu
                    // Prevent player from editing the sign
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Sign sign = (Sign) event.getBlock();
        // Check if sign matches a port sign
        if (sign.getSide(Side.FRONT).line(0).equals(PortSign.getTagline()) &&
            sign.getSide(Side.FRONT).line(3).equals(PortSign.getTagline()) &&
            sign.getSide(Side.BACK).line(0).equals(PortSign.getTagline()) &&
            sign.getSide(Side.BACK).line(3).equals(PortSign.getTagline()))
        {
            // Prevent a port sign from being created by a player writing on a sign, must be done with admin command
            event.setCancelled(true);
        }
    }
}
