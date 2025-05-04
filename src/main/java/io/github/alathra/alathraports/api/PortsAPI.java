package io.github.alathra.alathraports.api;

import com.github.milkdrinkers.colorparser.ColorParser;
import com.palmergames.bukkit.towny.object.Town;
import io.github.alathra.alathraports.AlathraPorts;
import io.github.alathra.alathraports.config.Settings;
import io.github.alathra.alathraports.core.TravelNode;
import io.github.alathra.alathraports.core.carriagestations.CarriageStation;
import io.github.alathra.alathraports.core.carriagestations.CarriageStationSize;
import io.github.alathra.alathraports.core.exceptions.TravelNodeRegisterException;
import io.github.alathra.alathraports.core.ports.Port;
import io.github.alathra.alathraports.core.TravelNodesManager;
import io.github.alathra.alathraports.core.ports.PortSize;
import io.github.alathra.alathraports.database.DBAction;
import io.github.alathra.alathraports.gui.GuiHandler;
import io.github.alathra.alathraports.utility.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PortsAPI {

    // All active (registered) ports
    public static Set<Port> getAllPorts() {
        return TravelNodesManager.getPorts();
    }

    // All active (registered) carriage station
    public static Set<CarriageStation> getAllCarriageStations() {
        return TravelNodesManager.getCarriageStations();
    }

    public static void setBlockaded(TravelNode node, boolean isBlockaded) {
        node.setBlockaded(isBlockaded);
    }

    public static boolean isBlockaded(TravelNode node) {
        return node.isBlockaded();
    }

    public static void openTravelMenu(Player player, TravelNode travelNode) {
        GuiHandler.generateTravelGui(player, travelNode);
    }

    public static @Nullable Port createAbstractPort(String name, PortSize portSize, Location baseLocation, Location teleportLocation) {
        Port port = new Port(name, portSize, baseLocation, teleportLocation);
        port.setAbstract(true);
        try {
            TravelNodesManager.registerPort(port);
        } catch (TravelNodeRegisterException e) {
            Logger.get().warn(e.getMessage());
            return null;
        }
        if (AlathraPorts.getDynmapHook().isDynmapLoaded()) {
            AlathraPorts.getDynmapHook().placePortMarker(port);
            AlathraPorts.getDynmapHook().placePortRangeMarker(port);
        }
        DBAction.saveAllPortsToDB();
        return port;
    }

    public static @Nullable Port createAbstractPort(String name, Location location) {
        Port port = new Port(name, Settings.findPortSize(1), location, location);
        port.setAbstract(true);
        try {
            TravelNodesManager.registerPort(port);
        } catch (TravelNodeRegisterException e) {
            Logger.get().warn(e.getMessage());
            return null;
        }
        if (AlathraPorts.getDynmapHook().isDynmapLoaded()) {
            AlathraPorts.getDynmapHook().placePortMarker(port);
            AlathraPorts.getDynmapHook().placePortRangeMarker(port);
        }
        DBAction.saveAllPortsToDB();
        return port;
    }

    public static boolean deleteAbstractPort(Port port) {
        if (TravelNodesManager.deregisterPort(port)) {
            if (AlathraPorts.getDynmapHook().isDynmapLoaded()) {
                AlathraPorts.getDynmapHook().removePortMarker(port);
                AlathraPorts.getDynmapHook().removePortRangeMarker(port);
            }
            DBAction.deletePortFromDB(port);
            return true;
        } else {
            Logger.get().warn(ColorParser.of("<red>Port deletion failed because port could not be de-registered. Does it exist?").build());
            return false;
        }
    }

    public static boolean isPortUpgradable(Port port) {
        int highestTier = 1;
        for (PortSize portSize : Settings.portSizes.values()) {
            if (portSize.getTier() > highestTier)
                highestTier = portSize.getTier();
        }
        return port.getSize().getTier() < highestTier;
    }

    public static boolean upgradePort(Port port) {
        if (!isPortUpgradable(port))
            return false;

        for (PortSize portSize : Settings.portSizes.values()) {
            if (portSize.getTier() == port.getSize().getTier()+1) {
                port.setSize(portSize);
                return true;
            }
        }
        return false;
    }

    public static @Nullable Port getPortByName(String name) {
        for (Port port : TravelNodesManager.getPorts()) {
            if (port.getName().equalsIgnoreCase(name)) {
                return port;
            }
        }
        return null;
    }

    public static @Nullable Port getPortByUUID(UUID uuid) {
        for (Port port : TravelNodesManager.getPorts()) {
            if (port.getUuid().equals(uuid)) {
                return port;
            }
        }
        return null;
    }

    public static @Nullable Port getPortFromTown(Town town) {
        if (!AlathraPorts.getTownyHook().isTownyLoaded()) {
            return null;
        }
        for (Port port : TravelNodesManager.getPorts()) {
            if (port.getTown() == null) {
                continue;
            }
            if (port.getTown().equals(town)) {
                return port;
            }
        }
        return null;
    }

    public static @Nullable Port getPortFromSign(Block block) {
        if (!(block.getState() instanceof Sign sign)) {
            return null;
        }
        if (!(Tag.STANDING_SIGNS.isTagged(sign.getType()) || Tag.WALL_SIGNS.isTagged(sign.getType()))) {
            return null;
        }
        if (sign.getSide(Side.FRONT).line(0).equals(Port.getTagline()) &&
            sign.getSide(Side.FRONT).line(3).equals(Port.getTagline()) &&
            sign.getSide(Side.BACK).line(0).equals(Port.getTagline()) &&
            sign.getSide(Side.BACK).line(3).equals(Port.getTagline())) {
            for (Port port : TravelNodesManager.getPorts()) {
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

    public static boolean isPortSign(Block block) {
        if (!(block.getState() instanceof Sign sign)) {
            return false;
        }
        if (!(Tag.STANDING_SIGNS.isTagged(sign.getType()) || Tag.WALL_SIGNS.isTagged(sign.getType()))) {
            return false;
        }
        if (sign.getSide(Side.FRONT).line(0).equals(Port.getTagline()) &&
            sign.getSide(Side.FRONT).line(3).equals(Port.getTagline()) &&
            sign.getSide(Side.BACK).line(0).equals(Port.getTagline()) &&
            sign.getSide(Side.BACK).line(3).equals(Port.getTagline())) {
            for (Port port : TravelNodesManager.getPorts()) {
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

    public static @Nullable PortSize getPortSizeByName(String name) {
        for (Map.Entry<String, PortSize> entry : Settings.portSizes.entrySet()) {
            if (name.equalsIgnoreCase(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static @Nullable PortSize getPortSizeByTier(int tier) {
        for (PortSize size : Settings.portSizes.values()) {
            if (tier == size.getTier()) {
                return size;
            }
        }
        return null;
    }

    public static @Nullable CarriageStation createAbstractCarriageStation(String name, CarriageStationSize carriageStationSize, Location baseLocation, Location teleportLocation) {
        CarriageStation carriageStation = new CarriageStation(name, carriageStationSize, baseLocation, teleportLocation);
        carriageStation.setAbstract(true);
        try {
            TravelNodesManager.registerCarriageStation(carriageStation);
        } catch (TravelNodeRegisterException e) {
            Logger.get().warn(e.getMessage());
            return null;
        }
        if (AlathraPorts.getDynmapHook().isDynmapLoaded()) {
            AlathraPorts.getDynmapHook().placeCarriageStationMarker(carriageStation);
        }
        DBAction.saveAllCarriageStationsToDB();
        return carriageStation;
    }

    public static @Nullable CarriageStation createAbstractCarriageStation(String name, Location location) {
        CarriageStation carriageStation = new CarriageStation(name, Settings.findCarriageStationSize(1), location, location);
        carriageStation.setAbstract(true);
        try {
            TravelNodesManager.reRegisterCarriageStation(carriageStation);
        } catch (TravelNodeRegisterException e) {
            Logger.get().warn(e.getMessage());
            return null;
        }
        if (AlathraPorts.getDynmapHook().isDynmapLoaded()) {
            AlathraPorts.getDynmapHook().placeCarriageStationMarker(carriageStation);
        }
        DBAction.saveAllCarriageStationsToDB();
        return carriageStation;
    }

    public static boolean deleteAbstractCarriageStation(CarriageStation carriageStation) {
        if (TravelNodesManager.deregisterCarriageStation(carriageStation)) {
            if (AlathraPorts.getDynmapHook().isDynmapLoaded()) {
                AlathraPorts.getDynmapHook().removeCarriageStationMarker(carriageStation);
                AlathraPorts.getDynmapHook().removeCarriageStationConnectionMarkers(carriageStation);
            }
            DBAction.deleteCarriageStationFromDB(carriageStation);
            return true;
        } else {
            Logger.get().warn(ColorParser.of("<red>Carriage Station deletion failed because port could not be de-registered. Does it exist?").build());
            return false;
        }
    }

    public static boolean isCarriageStationUpgradable(CarriageStation carriageStation) {
        int highestTier = 1;
        for (CarriageStationSize carriageStationSize : Settings.carriageStationSizes.values()) {
            if (carriageStationSize.getTier() > highestTier)
                highestTier = carriageStationSize.getTier();
        }
        return carriageStation.getSize().getTier() < highestTier;
    }

    public static boolean upgradePort(CarriageStation carriageStation) {
        if (!isCarriageStationUpgradable(carriageStation))
            return false;

        for (CarriageStationSize carriageStationSize : Settings.carriageStationSizes.values()) {
            if (carriageStationSize.getTier() == carriageStation.getSize().getTier()+1) {
                carriageStation.setSize(carriageStationSize);
                return true;
            }
        }
        return false;
    }

    public static @Nullable CarriageStation getCarriageStationByName(String name) {
        for (CarriageStation carriageStation : TravelNodesManager.getCarriageStations()) {
            if (carriageStation.getName().equalsIgnoreCase(name)) {
                return carriageStation;
            }
        }
        return null;
    }

    public static @Nullable CarriageStation getCarriageStationByUUID(UUID uuid) {
        for (CarriageStation carriageStation : TravelNodesManager.getCarriageStations()) {
            if (carriageStation.getUuid().equals(uuid)) {
                return carriageStation;
            }
        }
        return null;
    }

    public static @Nullable CarriageStation getCarriageStationFromTown(Town town) {
        if (!AlathraPorts.getTownyHook().isTownyLoaded()) {
            return null;
        }
        for (CarriageStation carriageStation : TravelNodesManager.getCarriageStations()) {
            if (carriageStation.getTown() == null) {
                continue;
            }
            if (carriageStation.getTown().equals(town)) {
                return carriageStation;
            }
        }
        return null;
    }

    public static @Nullable CarriageStation getCarriageStationFromSign(Block block) {
        if (!(block.getState() instanceof Sign sign)) {
            return null;
        }
        if (!(Tag.STANDING_SIGNS.isTagged(sign.getType()) || Tag.WALL_SIGNS.isTagged(sign.getType()))) {
            return null;
        }
        if (sign.getSide(Side.FRONT).line(0).equals(CarriageStation.getTagline()) &&
            sign.getSide(Side.FRONT).line(3).equals(CarriageStation.getTagline()) &&
            sign.getSide(Side.BACK).line(0).equals(CarriageStation.getTagline()) &&
            sign.getSide(Side.BACK).line(3).equals(CarriageStation.getTagline())) {
            for (CarriageStation carriageStation : TravelNodesManager.getCarriageStations()) {
                Component frontComponent = sign.getSide(Side.FRONT).line(1);
                Component backComponent = sign.getSide(Side.FRONT).line(1);
                if ((frontComponent instanceof TextComponent frontTextComponent) && (backComponent instanceof TextComponent backTextComponent)) {
                    if (carriageStation.getName().contentEquals(frontTextComponent.content()) && carriageStation.getName().contentEquals(backTextComponent.content())) {
                        return carriageStation;
                    }
                }
            }
        }
        return null;
    }

    public static boolean isCarriageStationSign(Block block) {
        if (!(block.getState() instanceof Sign sign)) {
            return false;
        }
        if (!(Tag.STANDING_SIGNS.isTagged(sign.getType()) || Tag.WALL_SIGNS.isTagged(sign.getType()))) {
            return false;
        }
        if (sign.getSide(Side.FRONT).line(0).equals(CarriageStation.getTagline()) &&
            sign.getSide(Side.FRONT).line(3).equals(CarriageStation.getTagline()) &&
            sign.getSide(Side.BACK).line(0).equals(CarriageStation.getTagline()) &&
            sign.getSide(Side.BACK).line(3).equals(CarriageStation.getTagline())) {
            for (CarriageStation carriageStation : TravelNodesManager.getCarriageStations()) {
                Component frontComponent = sign.getSide(Side.FRONT).line(1);
                Component backComponent = sign.getSide(Side.FRONT).line(1);
                if ((frontComponent instanceof TextComponent frontTextComponent) && (backComponent instanceof TextComponent backTextComponent)) {
                    if (carriageStation.getName().contentEquals(frontTextComponent.content()) && carriageStation.getName().contentEquals(backTextComponent.content())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static @Nullable CarriageStationSize getCarriageStationSizeByName(String name) {
        for (Map.Entry<String, CarriageStationSize> entry : Settings.carriageStationSizes.entrySet()) {
            if (name.equalsIgnoreCase(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static @Nullable CarriageStationSize getCarriageStationSizeByTier(int tier) {
        for (CarriageStationSize size : Settings.carriageStationSizes.values()) {
            if (tier == size.getTier()) {
                return size;
            }
        }
        return null;
    }

}
