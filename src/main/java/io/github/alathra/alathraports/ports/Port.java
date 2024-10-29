package io.github.alathra.alathraports.ports;

import io.github.alathra.alathraports.ports.enums.PortSize;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

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
        if (object instanceof Port) {
            Port port = (Port) object;
            if (port.getName().equalsIgnoreCase( this.getName()) &&
                port.signLocation.equals(this.getSignLocation()) &&
                port.getTeleportLocation().equals(this.getTeleportLocation()) &&
                port.getPortSize().equals(this.getPortSize()) &&
                port.getUuid().equals(this.uuid)
            ) {
                return true;
            }
        }
        return false;
    }

    public boolean isSimilar(Object object) {
        if (object instanceof Port) {
            Port port = (Port) object;
            if (world == null) {
                return false;
            }
            if (
                port.getName().equalsIgnoreCase( this.getName()) &&
                port.signLocation.distance(this.signLocation) > Ports.MINIMUM_PORT_DISTANCE &&
                port.teleportLocation.distance(this.signLocation) > Ports.MINIMUM_PORT_DISTANCE &&
                port.getPortSize().equals(this.getPortSize())
            ) {
                return true;
            }
        }
        return false;
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

    public PortSize getPortSize() {
        return portSize;
    }

    public void setPortSize(PortSize portSize) {
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
