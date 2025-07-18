package io.github.alathra.alathraports.core;

import com.github.milkdrinkers.colorparser.ColorParser;
import io.github.alathra.alathraports.api.PortsAPI;
import io.github.alathra.alathraports.config.Settings;
import io.github.alathra.alathraports.core.carriagestations.CarriageStation;
import io.github.alathra.alathraports.core.exceptions.TravelNodeRegisterException;
import io.github.alathra.alathraports.core.ports.Port;
import io.github.alathra.alathraports.database.DBAction;
import io.github.alathra.alathraports.hook.Hook;
import io.github.alathra.alathraports.utility.Logger;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.*;

public class TravelNodesManager {

    private static final Set<Port> ports = new HashSet<>();
    private static final Set<CarriageStation> carriageStations = new HashSet<>();

    public static Set<Port> getPorts() {
        return ports;
    }

    public static Set<CarriageStation> getCarriageStations() {
        return carriageStations;
    }

    public static void createTravelNodeFromSign(Player creator, TravelNode travelNode, BlockFace blockFace) {
        try {
            switch(travelNode.type) {
                case PORT:
                    registerPort((Port) travelNode);
                    if (Hook.Dynmap.isLoaded()) {
                        Hook.getDynmapHook().placePortMarker((Port) travelNode);
                        Hook.getDynmapHook().placePortRangeMarker((Port) travelNode);
                    }
                    DBAction.saveAllPortsToDB();
                    creator.sendMessage(ColorParser.of("<green>Port " + travelNode.getName() + " has been created").build());
                    break;
                case CARRIAGE_STATION:
                    registerCarriageStation((CarriageStation) travelNode);
                    if (Hook.Dynmap.isLoaded()) {
                        Hook.getDynmapHook().placeCarriageStationMarker((CarriageStation) travelNode);
                    }
                    DBAction.saveAllCarriageStationsToDB();
                    creator.sendMessage(ColorParser.of("<green>Carriage Station " + travelNode.getName() + " has been created").build());
                    break;
            }
            placeTravelNodeSign(creator, travelNode, blockFace);
        } catch (TravelNodeRegisterException e) {
            Logger.get().warn(e.getMessage());
            switch(travelNode.type) {
                case PORT:
                    creator.sendMessage(ColorParser.of("<red>Port failed to register. Check console for more details").build());
                    break;
                case CARRIAGE_STATION:
                    creator.sendMessage(ColorParser.of("<red>Carriage Station failed to register. Check console for more details").build());
                    break;
            }
        }
    }

    public static void placeTravelNodeSign(Player creator, TravelNode travelNode, BlockFace blockFace) {
        Block block = travelNode.getSignLocation().getBlock();
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
            sign = travelNode.generateNodeSign(sign);
            sign.update();
        } else {
            block.setType(Material.OAK_WALL_SIGN);
            Directional directional = (Directional) block.getBlockData();
            directional.setFacing(blockFace);
            block.setBlockData(directional);
            Sign sign = (Sign) block.getState();
            sign = travelNode.generateNodeSign(sign);
            sign.update();
        }
    }

    public static void deleteTravelNodeWithSign(@Nullable Player deleter, TravelNode travelNode) {
        switch (travelNode.type) {
            case PORT:
                if(deregisterPort((Port) travelNode)) {
                    Block signBlock = travelNode.getSignLocation().getBlock();
                    signBlock.setType(Material.AIR);
                    if (Hook.Dynmap.isLoaded()) {
                        Hook.getDynmapHook().removePortMarker((Port) travelNode);
                        Hook.getDynmapHook().removePortRangeMarker((Port) travelNode);
                    }
                    DBAction.deletePortFromDB((Port) travelNode);
                    if (deleter != null) {
                        deleter.sendMessage(ColorParser.of("<yellow>Port " + travelNode.getName() + " has been deleted").build());
                    }
                } else {
                    if (deleter != null) {
                        deleter.sendMessage(ColorParser.of("<red>Port deletion failed because port could not be de-registered. Does it exist?").build());
                    }
                }
                break;
            case CARRIAGE_STATION:
                if(deregisterCarriageStation((CarriageStation) travelNode)) {
                    Block signBlock = travelNode.getSignLocation().getBlock();
                    signBlock.setType(Material.AIR);
                    if (Hook.Dynmap.isLoaded()) {
                        Hook.getDynmapHook().removeCarriageStationMarker((CarriageStation) travelNode);
                        Hook.getDynmapHook().removeCarriageStationConnectionMarkers((CarriageStation) travelNode);
                    }
                    DBAction.deleteCarriageStationFromDB((CarriageStation) travelNode);
                    if (deleter != null) {
                        deleter.sendMessage(ColorParser.of("<yellow>Carriage Station " + travelNode.getName() + " has been deleted").build());
                    }
                } else {
                    if (deleter != null) {
                        deleter.sendMessage(ColorParser.of("<red>Carriage Station deletion failed because port could not be de-registered. Does it exist?").build());
                    }
                }
                break;
        }
    }

    public static void registerPort(Port newPort) throws TravelNodeRegisterException {
        for (Port port : ports) {

            // check for similar or matching port
            if (newPort.isSimilar(port)) {
                throw new TravelNodeRegisterException("Travel Node Failed to Register: New port with name \"" + newPort.getName() + "\" is too similar to or matches a registered port");
            }
            // if the port's name matches one of a registered port
            if (newPort.getName().equalsIgnoreCase(port.getName())) {
                throw new TravelNodeRegisterException("Travel Node Failed to Register: New port with name \"" + newPort.getName() + "\" matches the name of a registered port");
            }
            // if port has locations with different worlds... somehow
            if (!newPort.getSignLocation().getWorld().equals(newPort.getTeleportLocation().getWorld())) {
                throw new TravelNodeRegisterException("Travel Node Failed to Register: New port with name \"" + newPort.getName() + "\" has sign and teleport locations in different worlds");
            }
            // if port is too close to a registered port
            if (newPort.getSignLocation().getWorld().equals(port.getSignLocation().getWorld())) {
                if (newPort.getSignLocation().distance(port.getSignLocation()) <= Settings.MINIMUM_PORT_DISTANCE) {
                    throw new TravelNodeRegisterException("Travel Node Failed to Register: New port with name \"" + newPort.getName() + "\" has a sign location that is too close to a registered port");
                }
                if (newPort.getTeleportLocation().distance(port.getTeleportLocation()) <= Settings.MINIMUM_PORT_DISTANCE) {
                    throw new TravelNodeRegisterException("Travel Node Failed to Register: New port with name \"" + newPort.getName() + "\" has a teleport location that is too close to a registered port");
                }
            }
        }
        // register port
        ports.add(newPort);
    }

    public static void registerCarriageStation(CarriageStation newCarriageStation) throws TravelNodeRegisterException {
        for (CarriageStation carriageStation : carriageStations) {

            // check for similar or matching carriage station
            if (newCarriageStation.isSimilar(carriageStation)) {
                throw new TravelNodeRegisterException("Travel Node Failed to Register: New carriage station with name \"" + newCarriageStation.getName() + "\" is too similar to or matches a registered carriage station");
            }
            // if the carriage station's name matches one of a registered carriage station
            if (newCarriageStation.getName().equalsIgnoreCase(carriageStation.getName())) {
                throw new TravelNodeRegisterException("Travel Node Failed to Register: New carriage station with name \"" + newCarriageStation.getName() + "\" matches the name of a registered carriage station");
            }
            // if carriage station has locations with different worlds... somehow
            if (!newCarriageStation.getSignLocation().getWorld().equals(newCarriageStation.getTeleportLocation().getWorld())) {
                throw new TravelNodeRegisterException("Travel Node Failed to Register: New carriage station with name \"" + newCarriageStation.getName() + "\" has sign and teleport locations in different worlds");
            }
            // if carriage station is too close to a registered carriage station
            if (newCarriageStation.getSignLocation().getWorld().equals(carriageStation.getSignLocation().getWorld())) {
                if (newCarriageStation.getSignLocation().distance(carriageStation.getSignLocation()) <= Settings.MINIMUM_PORT_DISTANCE) {
                    throw new TravelNodeRegisterException("Travel Node Failed to Register: New carriage station with name \"" + newCarriageStation.getName() + "\" has a sign location that is too close to a registered carriage station");
                }
                if (newCarriageStation.getTeleportLocation().distance(carriageStation.getTeleportLocation()) <= Settings.MINIMUM_PORT_DISTANCE) {
                    throw new TravelNodeRegisterException("Travel Node Failed to Register: New carriage station with name \"" + newCarriageStation.getName() + "\" has a teleport location that is too close to a registered carriage station");
                }
            }
        }
        // register carriage station
        carriageStations.add(newCarriageStation);
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

    public static boolean deregisterCarriageStation(CarriageStation targetCarriageStation) {
        if (carriageStations.contains(targetCarriageStation)) {
            carriageStations.remove(targetCarriageStation);
            return true;
        } else {
            for (CarriageStation carriageStation : carriageStations) {
                if (targetCarriageStation.isSimilar(carriageStation)) {
                    carriageStations.remove(carriageStation);
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean reRegisterPort(Port modifiedPort) throws TravelNodeRegisterException {
        Port originalPort = PortsAPI.getPortByUUID(modifiedPort.getUuid());
        // Port could not be found in registry, failed to de-register
        if (originalPort == null) {
            return false;
        }
        // De-register
        ports.remove(originalPort);
        // Try to register modified port
        try {
            registerPort(modifiedPort);
        } catch (TravelNodeRegisterException e) {
            // Registration failed, revert registry to original port
            ports.add(originalPort);
            throw new TravelNodeRegisterException(e.getMessage());
        }
        return true;
    }

    public static boolean reRegisterCarriageStation(CarriageStation modifiedcarriageStation) throws TravelNodeRegisterException {
        CarriageStation originalCarriageStation =  PortsAPI.getCarriageStationByUUID(modifiedcarriageStation.getUuid());
        // Carriage station could not be found in registry, failed to de-register
        if (originalCarriageStation == null) {
            return false;
        }
        // De-register
        carriageStations.remove(originalCarriageStation);
        // Try to register modified carriage station
        try {
            registerCarriageStation(modifiedcarriageStation);
        } catch (TravelNodeRegisterException e) {
            // Registration failed, revert registry to original carriage station
            carriageStations.add(originalCarriageStation);
            throw new TravelNodeRegisterException(e.getMessage());
        }
        return true;
    }

    public static boolean isAttachedToTravelNodeSign(Block block) {

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
                        if (PortsAPI.isPortSign(adjacentBlocks[i]) || PortsAPI.isCarriageStationSign(adjacentBlocks[i])) {
                            return true;
                        }
                    }
                }

                // If sides of block contain a wall sign port sign
                if (isWallSign && i != 0 && PortsAPI.isPortSign(adjacentBlocks[i]) || PortsAPI.isCarriageStationSign(adjacentBlocks[i])) {
                    BlockFace facing = ((Directional) adjacentBlocks[i].getBlockData()).getFacing();
                    return adjacentBlocks[i].getRelative(facing.getOppositeFace()).getLocation().equals(block.getLocation());
                }
            }
        }
        return false;
    }

}
