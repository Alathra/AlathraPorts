package io.github.alathra.alathraports.core;

import com.palmergames.bukkit.towny.object.Town;
import io.github.alathra.alathraports.AlathraPorts;
import io.github.alathra.alathraports.config.Settings;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class TravelNode implements Cloneable {
    // Unique id of the travel node
    protected final UUID uuid;
    // The raw name of the travel node (no colors, underscore for space)
    protected String name;
    // The size (tier) of the travel node. Effects things like cost, travel speed, etc.
    protected TravelNodeSize size;
    // The location of the sign block associated with the travel node
    protected Location signLocation;
    // The location where the player is teleported to (1 block above the ground)
    protected Location teleportLocation;
    // If the travel node is set as blockaded (disables the travel node)
    protected boolean isBlockaded;
    // The town associated with the travel node (optional)
    protected Town town;
    // The fee currently being set by the town mayor
    protected double townFee;
    // The world where this node is located
    protected World world;
    // The type of travel node this is (Port or Carriage Station)
    protected TravelNodeType type;

    public enum TravelNodeType {
        PORT,
        CARRIAGE_STATION
    }

    public TravelNode(UUID uuid, String name, TravelNodeSize size, Location signLocation, Location teleportLocation) {
        this.uuid = uuid;
        this.name = name;
        this.size = size;
        this.signLocation = signLocation;
        this.teleportLocation = teleportLocation;
        this.townFee = 0.0;

        if (signLocation.getWorld().equals(teleportLocation.getWorld())) {
            this.world = signLocation.getWorld();
        }
    }

    public TravelNode(String name, TravelNodeSize size, Location signLocation, Location teleportLocation) {
        this.uuid = UUID.randomUUID();
        this.name = name;
        this.size = size;
        this.signLocation = signLocation;
        this.teleportLocation = teleportLocation;
        this.townFee = 0.0;

        if (signLocation.getWorld().equals(teleportLocation.getWorld())) {
            this.world = signLocation.getWorld();
        }
    }

    // Abstract methods
    public abstract Sign generateNodeSign(Sign sign);
    public abstract List<TravelNode> getDirectConnections();

    @Override
    public boolean equals(Object object) {
        if (object instanceof TravelNode travelNode) {
            return travelNode.getName().equalsIgnoreCase(this.getName()) &&
                travelNode.signLocation.equals(this.getSignLocation()) &&
                travelNode.getTeleportLocation().equals(this.getTeleportLocation()) &&
                travelNode.getSize().equals(this.getSize()) &&
                travelNode.getUuid().equals(this.uuid);
        }
        return false;
    }

    public boolean isSimilar(Object object) {
        if (object instanceof TravelNode travelNode) {
            if (world == null) {
                return false;
            }
            return travelNode.getName().equalsIgnoreCase(this.getName()) &&
                travelNode.signLocation.distance(this.signLocation) < Settings.MINIMUM_PORT_DISTANCE &&
                travelNode.teleportLocation.distance(this.signLocation) < Settings.MINIMUM_PORT_DISTANCE &&
                travelNode.getType().equals(this.type) &&
                travelNode.getSize().equals(this.size);
        }
        return false;
    }

    public List<TravelNode> getPossibleConnections() {
        // Implements Breadth-First Traversal to generate the "graph" of reachable travel nodes
        if (this.isBlockaded) {
            return Collections.emptyList();
        }
        HashSet<TravelNode> visited = new HashSet<>();
        visited.add(this);
        LinkedList<TravelNode> queue = new LinkedList<>();
        queue.add(this);
        TravelNode node;
        while (!queue.isEmpty()) {
            node = queue.poll();
            visited.add(node);
            for (TravelNode travelNode : node.getDirectConnections()) {
                if (!visited.contains(travelNode)) {
                    queue.add(travelNode);
                }
            }
        }
        // Don't include own node
        visited.remove(this);
        return visited.stream().toList();
    }

    public void refreshNodeSign() {
        BlockState blockState = signLocation.getBlock().getState(false);
        blockState.update(true);
        Sign sign = generateNodeSign((Sign) blockState);
        sign.update(true);
    }

    public double distanceTo(TravelNode travelNode) {
        if (!this.world.equals(travelNode.world)) {
            return 0.0;
        }
        return (this.getSignLocation().distance(travelNode.getSignLocation()));
    }

    // Find the town in which the port exists. If no town found then it will be set to null
    public void findTown() {
        town = AlathraPorts.getTownyHook().getTownyAPI().getTown(signLocation);
    }

    public void setDefaultTax() {
        if (town != null) {
            townFee = size.maxTownFee;
        }
    }

    public void setTown(Town town) {
        this.town = town;
    }

    public @Nullable Town getTown() {
        return town;
    }

    public double getTownFee() {
        return townFee;
    }

    public boolean setTownFee(double fee) {
        if (size.maxTownFee < fee) {
            return false;
        }
        if (fee < 0) {
            townFee = 0.0;
            return false;
        }
        this.townFee = fee;
        return true;
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

    public TravelNodeSize getSize() {
        return size;
    }

    public void setSize(TravelNodeSize size) {
        this.size = size;
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

    public World getWorld() {
        return world;
    }

    public boolean isBlockaded() {
        return isBlockaded;
    }

    public void setBlockaded(boolean isBlockaded) {
        this.isBlockaded = isBlockaded;
    }

    public TravelNodeType getType() {
        return type;
    }

    @Override
    public TravelNode clone() {
        try {
            return (TravelNode) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
