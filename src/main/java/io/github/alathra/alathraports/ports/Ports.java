package io.github.alathra.alathraports.ports;

import io.github.alathra.alathraports.ports.exceptions.PortRegisterException;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;

import java.util.HashSet;
import java.util.Set;

public class Ports {
    public static final double MINIMUM_PORT_DISTANCE = 1.0;

    private Set<Port> ports = new HashSet<>();

    public void registerPort(Port newPort) throws PortRegisterException {
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

    public void deregisterPort(Port targetPort) {
        if (!ports.contains(targetPort)) {
            for (Port port : ports) {
                if (targetPort.isSimilar(port)) {
                    ports.remove(port);
                    return;
                }
            }
        }
    }

    public Set<Port> getPorts() {
        return ports;
    }

}
