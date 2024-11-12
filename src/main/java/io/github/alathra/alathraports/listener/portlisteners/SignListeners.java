package io.github.alathra.alathraports.listener.portlisteners;

import com.github.milkdrinkers.colorparser.ColorParser;
import io.github.alathra.alathraports.ports.Port;
import io.github.alathra.alathraports.ports.Ports;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.sign.Side;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Objects;

public class SignListeners implements Listener {

    @EventHandler
    public void onSignRightClick(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (isPortSign(Objects.requireNonNull(event.getClickedBlock()))) {
                // TODO: open port menu
                event.getPlayer().sendMessage(ColorParser.of("<green> Open port menu").build());
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (isPortSign(event.getBlock())) {
            // Prevent a port sign from being created by a player writing on a sign, must be done with admin command
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSignBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (isPortSign(block) || isAttachedToPortSign(block)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSignExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> isPortSign(block) || isAttachedToPortSign(block));
    }

    @EventHandler
    public void onSignExplodeByEntity(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> isPortSign(block) || isAttachedToPortSign(block));
    }

    @EventHandler
    public void onSignMovedByPistonExtension(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (isPortSign(block) || isAttachedToPortSign(block)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onSignMovedByPistonRetraction(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (isPortSign(block) || isAttachedToPortSign(block)) {
                event.setCancelled(true);
            }
        }
    }

    // Example: Enderman picks up block. I know this is stupid but why not
    @EventHandler
    public void onSignMovedByEntity(EntityChangeBlockEvent event) {
        if (isPortSign(event.getBlock()) || isAttachedToPortSign(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    private boolean isPortSign(Block block) {
        if (!(block.getState() instanceof Sign sign)) {
            return false;
        }
        if (!(Tag.STANDING_SIGNS.isTagged(sign.getType()) || Tag.WALL_SIGNS.isTagged(sign.getType()))) {
            return false;
        }
        if (sign.getSide(Side.FRONT).line(0).equals(Ports.getTagline()) &&
            sign.getSide(Side.FRONT).line(3).equals(Ports.getTagline()) &&
            sign.getSide(Side.BACK).line(0).equals(Ports.getTagline()) &&
            sign.getSide(Side.BACK).line(3).equals(Ports.getTagline())) {
            for (Port port : Ports.getPorts()) {
                Component frontComponent = sign.getSide(Side.FRONT).line(1);
                Component backComponent = sign.getSide(Side.FRONT).line(1);
                if ((frontComponent instanceof TextComponent frontTextComponent) && (backComponent instanceof TextComponent backTextComponent)) {
                    if (frontTextComponent.content().contains(port.getName()) && backTextComponent.content().contains(port.getName())) {
                        return true;
                    }
                }
            }
            return true;
        }
        return false;
    }

    private boolean isAttachedToPortSign(Block block) {

        if (!block.isSolid() || !block.isCollidable()) {
            return false;
        }

        Block[] adjacentBlocks = {
            block.getRelative(BlockFace.UP),
            block.getRelative(BlockFace.NORTH),
            block.getRelative(BlockFace.SOUTH),
            block.getRelative(BlockFace.EAST),
            block.getRelative(BlockFace.WEST)
        };

        for (int i = 0; i < 5; i++) {
            if (adjacentBlocks[i].getState() instanceof Sign) {
                boolean isStandingSign = Tag.STANDING_SIGNS.isTagged(adjacentBlocks[i].getType());
                boolean isWallSign = Tag.WALL_SIGNS.isTagged(adjacentBlocks[i].getType());

                // If above block is a standing sign port sign
                if (i == 0) {
                    if (isStandingSign) {
                        if (isPortSign(adjacentBlocks[i])) {
                            return true;
                        }
                    }
                }

                // If sides of block contain a wall sign port sign
                if (isWallSign && i != 0 && isPortSign(adjacentBlocks[i])) {
                    BlockFace facing = ((Directional) adjacentBlocks[i].getBlockData()).getFacing();
                    return adjacentBlocks[i].getRelative(facing.getOppositeFace()).getLocation().equals(block.getLocation());
                }
            }
        }
        return false;
    }
}
