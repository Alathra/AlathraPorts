package io.github.alathra.alathraports.ports;

import com.github.milkdrinkers.colorparser.ColorParser;
import io.github.alathra.alathraports.AlathraPorts;
import io.github.alathra.alathraports.ports.exceptions.PortRegisterException;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class Ports {
    public static final double MINIMUM_PORT_DISTANCE = 1.0;

    private static final Set<Port> ports = new HashSet<>();

    public static void createPort(Player creator, Port port) {
        try {
            registerPort(port);
            // TODO: place port sign
        } catch (PortRegisterException e) {
            AlathraPorts.getInstance().getLogger().warning(e.getMessage());
            creator.sendMessage(ColorParser.of("<red>Port creation failed. Check console for more details").build());
        }
    }

    public static void deletePort(Player deleter, Port port) {
        if(deregisterPort(port)) {
            // TODO: clear port sign
        } else {
            deleter.sendMessage(ColorParser.of("<red>Port deletion failed because port could not be deregistered. Does it exist?").build());
        }
    }

    public static void registerPort(Port newPort) throws PortRegisterException {
        for (Port port : ports) {
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
                if (newPort.getSignLocation().distance(port.getSignLocation()) < MINIMUM_PORT_DISTANCE) {
                    throw new PortRegisterException("Port Failed to Register: New port with name \"" + newPort.getName() + "\" has a sign location that is too close to a registered port");
                }
                if (newPort.getTeleportLocation().distance(port.getTeleportLocation()) < MINIMUM_PORT_DISTANCE) {
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

            // safety check
            if (newPort.isSimilar(port)) {
                throw new PortRegisterException("Port Failed to Register: New port with name \"" + newPort.getName() + "\" is too similar to a registered port");
            }

            // register port
            ports.add(port);
        }
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

    public Set<Port> getPorts() {
        return ports;
    }

}
