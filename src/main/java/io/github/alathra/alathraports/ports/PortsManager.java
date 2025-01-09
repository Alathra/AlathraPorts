package io.github.alathra.alathraports.ports;

import com.github.milkdrinkers.colorparser.ColorParser;
import io.github.alathra.alathraports.config.Settings;
import io.github.alathra.alathraports.ports.exceptions.PortRegisterException;
import io.github.alathra.alathraports.utility.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.*;

public class PortsManager {

    private static final Set<Port> ports = new HashSet<>();

    public static Component getTagline() {
        return ColorParser.of("<blue>⚓⚓⚓").build();
    }

    public static void createPortFromSign(Player creator, Port port, BlockFace blockFace) {
        try {
            registerPort(port);
            placePortSign(creator, port, blockFace);
            creator.sendMessage(ColorParser.of("<green>Port " + port.getName() + " has been created").build());
        } catch (PortRegisterException e) {
            Logger.get().warn(e.getMessage());
            creator.sendMessage(ColorParser.of("<red>Port failed to register. Check console for more details").build());
        }
    }

    public static void placePortSign(Player creator, Port port, BlockFace blockFace) {
        Block block = port.getSignLocation().getBlock();
        if (blockFace == BlockFace.UP) {
            block.setType(Material.OAK_SIGN);
            float yaw = creator.getYaw();
            BlockFace direction;
            if (yaw < 0) {
                yaw += 360;
            }
            yaw %= 360;
            if (yaw >= 315 || yaw < 45) {
                direction = BlockFace.SOUTH;
            } else if (yaw < 135) {
                direction = BlockFace.WEST;
            } else if (yaw < 225) {
                direction = BlockFace.NORTH;
            } else {
                direction = BlockFace.EAST;
            }
            org.bukkit.block.data.type.Sign signData = (org.bukkit.block.data.type.Sign) block.getBlockData();
            signData.setRotation(direction);
            block.setBlockData(signData);
            Sign sign = (Sign) block.getState();
            sign = port.generatePortSign(sign);
            sign.update();
        } else {
            block.setType(Material.OAK_WALL_SIGN);
            Directional directional = (Directional) block.getBlockData();
            directional.setFacing(blockFace);
            block.setBlockData(directional);
            Sign sign = (Sign) block.getState();
            sign = port.generatePortSign(sign);
            sign.update();
        }
    }

    public static void deletePortWithSign(@Nullable Player deleter, Port port) {
        if(deregisterPort(port)) {
            Block signBlock = port.getSignLocation().getBlock();
            signBlock.setType(Material.AIR);
            if (deleter != null) {
                deleter.sendMessage(ColorParser.of("<yellow>Port " + port.getName() + " has been deleted").build());
            }
        } else {
            if (deleter != null) {
                deleter.sendMessage(ColorParser.of("<red>Port deletion failed because port could not be de-registered. Does it exist?").build());
            }
        }
    }

    public static void registerPort(Port newPort) throws PortRegisterException {
        for (Port port : ports) {

            // check for similar or matching port
            if (newPort.isSimilar(port)) {
                throw new PortRegisterException("Port Failed to Register: New port with name \"" + newPort.getName() + "\" is too similar to or matches a registered port");
            }
            // if the port's name matches one of a registered port
            if (newPort.getName().equalsIgnoreCase(port.getName())) {
                throw new PortRegisterException("Port Failed to Register: New port with name \"" + newPort.getName() + "\" matches the name of a registered port");
            }
            // if port has locations with different worlds... somehow
            if (!newPort.getSignLocation().getWorld().equals(newPort.getTeleportLocation().getWorld())) {
                throw new PortRegisterException("Port Failed to Register: New port with name \"" + newPort.getName() + "\" has sign and teleport locations in different worlds");
            }
            // if port is too close to a registered port
            if (newPort.getSignLocation().getWorld().equals(port.getSignLocation().getWorld())) {
                if (newPort.getSignLocation().distance(port.getSignLocation()) <= Settings.MINIMUM_PORT_DISTANCE) {
                    throw new PortRegisterException("Port Failed to Register: New port with name \"" + newPort.getName() + "\" has a sign location that is too close to a registered port");
                }
                if (newPort.getTeleportLocation().distance(port.getTeleportLocation()) <= Settings.MINIMUM_PORT_DISTANCE) {
                    throw new PortRegisterException("Port Failed to Register: New port with name \"" + newPort.getName() + "\" has a teleport location that is too close to a registered port");
                }
            }
            // if the port sign location block is not a sign
            if (Tag.ALL_SIGNS.isTagged(newPort.getSignLocation().getBlock().getType())) {
                throw new PortRegisterException("Port Failed to Register: New port with name \"" + newPort.getName() + "\" has a block that is not a sign at its sign location");
            }
            // if the port's tp location is not air blocks and is not over water, so a bad location
            if ( !(newPort.getTeleportLocation().getBlock().getType().isAir() && newPort.getTeleportLocation().getBlock().getRelative(BlockFace.UP).getType().isAir())
                || !(newPort.getTeleportLocation().getBlock().getRelative(BlockFace.DOWN).getType().isSolid()) ) {
                throw new PortRegisterException("Port Failed to Register: New port with name \"" + newPort.getName() + "\" has an unsafe teleport location");
            }
        }
        // register port
        ports.add(newPort);
    }

    public static boolean deregisterPort(Port targetPort) {
        if (ports.contains(targetPort)) {
            ports.remove(targetPort);
            return true;
        } else {
            for (Port port : ports) {
                if (targetPort.isSimilar(port)) {
                    ports.remove(port);
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean reregisterPort(Port modifiedPort) throws PortRegisterException {
        Port originalPort = getPortByID(modifiedPort.getUuid());
        // Port could not be found in registry, failed to de-register
        if (originalPort == null) {
            return false;
        }
        // De-register
        ports.remove(originalPort);
        // Try to register modified port
        try {
            registerPort(modifiedPort);
        } catch (PortRegisterException e) {
            // Registration failed, revert registry to original port
            ports.add(originalPort);
            throw new PortRegisterException(e.getMessage());
        }
        return true;
    }

    public static boolean isPortSign(Block block) {
        if (!(block.getState() instanceof Sign sign)) {
            return false;
        }
        if (!(Tag.STANDING_SIGNS.isTagged(sign.getType()) || Tag.WALL_SIGNS.isTagged(sign.getType()))) {
            return false;
        }
        if (sign.getSide(Side.FRONT).line(0).equals(PortsManager.getTagline()) &&
            sign.getSide(Side.FRONT).line(3).equals(PortsManager.getTagline()) &&
            sign.getSide(Side.BACK).line(0).equals(PortsManager.getTagline()) &&
            sign.getSide(Side.BACK).line(3).equals(PortsManager.getTagline())) {
            for (Port port : PortsManager.getPorts()) {
                Component frontComponent = sign.getSide(Side.FRONT).line(1);
                Component backComponent = sign.getSide(Side.FRONT).line(1);
                if ((frontComponent instanceof TextComponent frontTextComponent) && (backComponent instanceof TextComponent backTextComponent)) {
                    if (port.getName().contentEquals(frontTextComponent.content()) && port.getName().contentEquals(backTextComponent.content())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isAttachedToPortSign(Block block) {

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

    public static List<Port> orderPorts(Port currentPort, List<Port> toSort) {
        TreeMap<Double, Port> sortedMap = new TreeMap<Double, Port>();

        // Removes self
        toSort.remove(currentPort);

        // Adds all megaports to sortedMap & removes from toSort
        for (Port port : toSort) {
            if (port.getSize().getTier() == 5) {
                Double distance = currentPort.distanceTo(port);
                while (sortedMap.containsKey(distance)) distance += 0.01;
                sortedMap.put(distance, port);
            }
        }

        // Adds all megaports to final list & clears sortedMap
        List<Port> returnList = new ArrayList<Port>(sortedMap.values());
        sortedMap.clear();

        // Re-uses sortedMap for rest of ports
        for (Port port : toSort) {
            if (port.getSize().getTier() != 5) {
                Double distance = currentPort.distanceTo(port);
                while (sortedMap.containsKey(distance)) distance += 0.01;
                sortedMap.put(distance, port);
            }
        }
        // Adds rest and returns.
        returnList.addAll(sortedMap.values());

        return returnList;
    }

    public static Port getPortByName(String name) {
        for (Port port : ports) {
            if (port.getName().equalsIgnoreCase(name)) {
                return port;
            }
        }
        return null;
    }

    public static Port getPortByID(UUID uuid) {
        for (Port port : ports) {
            if (port.getUuid().equals(uuid)) {
                return port;
            }
        }
        return null;
    }

    public static Port getPortFromSign(Block block) {
        if (!(block.getState() instanceof Sign sign)) {
            return null;
        }
        if (!(Tag.STANDING_SIGNS.isTagged(sign.getType()) || Tag.WALL_SIGNS.isTagged(sign.getType()))) {
            return null;
        }
        if (sign.getSide(Side.FRONT).line(0).equals(PortsManager.getTagline()) &&
            sign.getSide(Side.FRONT).line(3).equals(PortsManager.getTagline()) &&
            sign.getSide(Side.BACK).line(0).equals(PortsManager.getTagline()) &&
            sign.getSide(Side.BACK).line(3).equals(PortsManager.getTagline())) {
            for (Port port : PortsManager.getPorts()) {
                Component frontComponent = sign.getSide(Side.FRONT).line(1);
                Component backComponent = sign.getSide(Side.FRONT).line(1);
                if ((frontComponent instanceof TextComponent frontTextComponent) && (backComponent instanceof TextComponent backTextComponent)) {
                    if (port.getName().contentEquals(frontTextComponent.content()) && port.getName().contentEquals(backTextComponent.content())) {
                        return port;
                    }
                }
            }
        }
        return null;
    }

    public static PortSize getPortSizeByName(String name) {
        for (Map.Entry<String, PortSize> entry : Settings.sizes.entrySet()) {
            if (name.equalsIgnoreCase(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static PortSize getPortSizeByTier(int tier) {
        for (PortSize size : Settings.sizes.values()) {
            if (tier == size.getTier()) {
                return size;
            }
        }
        return null;
    }

    public static Set<Port> getPorts() {
        return ports;
    }

}
