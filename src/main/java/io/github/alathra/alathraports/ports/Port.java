package io.github.alathra.alathraports.ports;

import com.github.milkdrinkers.colorparser.ColorParser;
import io.github.alathra.alathraports.ports.enums.PortSize;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;

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
        if (object instanceof Port port) {
            return port.getName().equalsIgnoreCase(this.getName()) &&
                port.signLocation.equals(this.getSignLocation()) &&
                port.getTeleportLocation().equals(this.getTeleportLocation()) &&
                port.getPortSize().equals(this.getPortSize()) &&
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
                port.signLocation.distance(this.signLocation) > Ports.MINIMUM_PORT_DISTANCE &&
                port.teleportLocation.distance(this.signLocation) > Ports.MINIMUM_PORT_DISTANCE &&
                port.getPortSize().equals(this.getPortSize());
        }
        return false;
    }

    public Sign generatePortSign(Sign sign) {
        sign.getSide(Side.FRONT).line(0, Ports.getTagline());
        sign.getSide(Side.FRONT).line(1, ColorParser.of("<gold><bold>" + this.name).build());
        sign.getSide(Side.FRONT).line(2, ColorParser.of("<light_red>" + this.getPortSizeName()).build());
        sign.getSide(Side.FRONT).line(3, Ports.getTagline());
        sign.getSide(Side.BACK).line(0, Ports.getTagline());
        sign.getSide(Side.BACK).line(1, ColorParser.of("<gold><bold>" + this.name).build());
        sign.getSide(Side.BACK).line(2, ColorParser.of("<light_red>" + this.getPortSizeName()).build());
        sign.getSide(Side.BACK).line(3, Ports.getTagline());
        return sign;
    }

    // Convert port size enum to formatted string
    public String getPortSizeName() {
        String[] words = portSize.name().toLowerCase().split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
        }
        return result.toString().trim();
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
