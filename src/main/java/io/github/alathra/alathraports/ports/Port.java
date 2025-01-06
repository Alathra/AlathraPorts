package io.github.alathra.alathraports.ports;

import com.github.milkdrinkers.colorparser.ColorParser;
import io.github.alathra.alathraports.config.Settings;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;

import java.util.*;

public class Port {
    private final UUID uuid;
    private String name;
    private PortSize portSize;
    private Location signLocation; // The location of the sign block associated with the port
    private Location teleportLocation; // The location where the player is teleported to (1 block above the ground)

    private World world;

    public Port(UUID uuid, String name, PortSize portSize, Location signLocation, Location teleportLocation) {
        this.uuid = uuid;
        this.signLocation = signLocation;
        this.teleportLocation = teleportLocation;
        this.name = name;
        this.portSize = portSize;

        if (signLocation.getWorld().equals(teleportLocation.getWorld())) {
            this.world = signLocation.getWorld();
        }
    }

    public Port(String name, PortSize portSize, Location signLocation, Location teleportLocation) {
        this.uuid = UUID.randomUUID();
        this.signLocation = signLocation;
        this.teleportLocation = teleportLocation;
        this.name = name;
        this.portSize = portSize;

        if (signLocation.getWorld().equals(teleportLocation.getWorld())) {
            this.world = signLocation.getWorld();
        }
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Port port) {
            return port.getName().equalsIgnoreCase(this.getName()) &&
                port.signLocation.equals(this.getSignLocation()) &&
                port.getTeleportLocation().equals(this.getTeleportLocation()) &&
                port.getSize().equals(this.getSize()) &&
                port.getUuid().equals(this.uuid);
        }
        return false;
    }

    public boolean isSimilar(Object object) {
        if (object instanceof Port port) {
            if (world == null) {
                return false;
            }
            return port.getName().equalsIgnoreCase(this.getName()) &&
                port.signLocation.distance(this.signLocation) <= Settings.MINIMUM_PORT_DISTANCE &&
                port.teleportLocation.distance(this.signLocation) <= Settings.MINIMUM_PORT_DISTANCE &&
                port.getSize().equals(this.getSize());
        }
        return false;
    }

    public Sign generatePortSign(Sign sign) {
        sign.getSide(Side.FRONT).line(0, PortsManager.getTagline());
        sign.getSide(Side.FRONT).line(1, ColorParser.of("<dark_green><bold>" + this.name).build());
        sign.getSide(Side.FRONT).line(2, ColorParser.of("<red>" + this.portSize.getName()).build());
        sign.getSide(Side.FRONT).line(3, PortsManager.getTagline());
        sign.getSide(Side.BACK).line(0, PortsManager.getTagline());
        sign.getSide(Side.BACK).line(1, ColorParser.of("<dark_green><bold>" + this.name).build());
        sign.getSide(Side.BACK).line(2, ColorParser.of("<red>" + this.portSize.getName()).build());
        sign.getSide(Side.BACK).line(3, PortsManager.getTagline());
        return sign;
    }

    public boolean refreshSign() {
        if (this.getSignLocation().getBlock().getState() instanceof Sign sign) {
            if (PortsManager.isPortSign(this.getSignLocation().getBlock())) {
                sign = generatePortSign(sign);
                sign.update();
                return true;
            }
        }
        return false;
    }

    public Double distanceTo(Port port) {
        if (!this.signLocation.getWorld().equals(port.getSignLocation().getWorld())) {
            return 0.0;
        }
        return (this.getSignLocation().distance(port.getSignLocation()));
    }

    public List<Port> getPortsInRange() {
        ArrayList<Port> ports = new ArrayList<>();
        for (Port port : PortsManager.getPorts()) {
            if (port.equals(this)) {
                continue;
            }
            double distance = this.distanceTo(port);
            if (distance > this.portSize.getRange() && distance > port.portSize.getRange()) {
                continue;
            }
            ports.add(port);
        }

        return ports;
    }

    public List<Port> getReachablePorts() {
        // Implements Breadth-First Traversal to generate the "graph" of reachable ports
        HashSet<Port> visited = new HashSet<>();
        visited.add(this);
        LinkedList<Port> queue = new LinkedList<>();
        queue.add(this);
        Port node;
        while (!queue.isEmpty()) {
            node = queue.poll();
            visited.add(node);
            for (Port port : node.getPortsInRange()) {
                if (!visited.contains(port)) {
                    queue.add(port);
                }
            }
        }
        // Don't include own port
        visited.remove(this);
        return visited.stream().toList();
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PortSize getSize() {
        return portSize;
    }

    public void setSize(PortSize portSize) {
        this.portSize = portSize;
    }

    public Location getSignLocation() {
        return signLocation;
    }

    public void setSignLocation(Location signLocation) {
        this.signLocation = signLocation;
    }

    public Location getTeleportLocation() {
        return teleportLocation;
    }

    public void setTeleportLocation(Location teleportLocation) {
        this.teleportLocation = teleportLocation;
    }
}
