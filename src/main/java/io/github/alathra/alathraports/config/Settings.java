package io.github.alathra.alathraports.config;

import com.github.milkdrinkers.crate.Config;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.github.alathra.alathraports.AlathraPorts;
import io.github.alathra.alathraports.core.TravelNodesManager;
import io.github.alathra.alathraports.core.carriagestations.CarriageStation;
import io.github.alathra.alathraports.core.carriagestations.CarriageStationSize;
import io.github.alathra.alathraports.core.carriagestations.CarriageStationSizeBuilder;
import io.github.alathra.alathraports.core.ports.Port;
import io.github.alathra.alathraports.core.ports.PortSize;
import io.github.alathra.alathraports.core.ports.PortSizeBuilder;
import io.github.alathra.alathraports.core.exceptions.TravelNodeSizeSerialException;
import io.github.alathra.alathraports.utility.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.*;

public class Settings {

    /**
     * A convenience class for interfacing ConfigHandler that stores settings in memory
     */

    public static int MINIMUM_PORT_DISTANCE;
    public static double BASE_COST;
    public static double BASE_ANIMAL_COST;
    public static Material BLOCKADE_ICON;
    public static boolean SHOW_BLOCKADED;
    public static boolean REMOVE_PORT_ON_TOWN_DELETE;
    public static int DATA_SAVE_INTERVAL;
    public static Map<String, PortSize> portSizes = new HashMap<>();
    public static Map<String, CarriageStationSize> carriageStationSizes = new HashMap<>();
    public static int TRAVEL_NODES_DYNMAP_LAYER;
    public static int PORT_RANGES_DYNMAP_LAYER;
    public static int CARRIAGE_CONNECTIONS_DYNMAP_LAYER;

    public static Config getConfig() {
        return AlathraPorts.getInstance().getConfigHandler().getConfig();
    }

    public static void update() {
        MINIMUM_PORT_DISTANCE = getConfig().getOrDefault("GlobalSettings.minimumDistance", 10);
        BASE_COST = getConfig().getOrDefault("GlobalSettings.baseCost", 5.00);
        BASE_ANIMAL_COST = getConfig().getOrDefault("GlobalSettings.baseAnimalCost", 5.00);
        try {
            BLOCKADE_ICON = Material.valueOf(getConfig().getOrDefault("GlobalSettings.blockadeIcon", "SKELETON_SKULL"));
        } catch (IllegalArgumentException e) {
            BLOCKADE_ICON = Material.SKELETON_SKULL;
            Logger.get().warn("Config Error: blockadeIcon is not set to a valid material");
        }
        SHOW_BLOCKADED = getConfig().getOrDefault("GlobalSettings.showBlockaded", true);
        REMOVE_PORT_ON_TOWN_DELETE = getConfig().getOrDefault("GlobalSettings.removeOnTownDelete", true);
        DATA_SAVE_INTERVAL = getConfig().getOrDefault("GlobalSettings.dataSaveInterval", 1800);
        updateSizes();
        TRAVEL_NODES_DYNMAP_LAYER = getConfig().getOrDefault("DynmapSettings.TravelNodeLayerPriority",3);
        PORT_RANGES_DYNMAP_LAYER = getConfig().getOrDefault("DynmapSettings.PortRangeLayerPriority",4);
        CARRIAGE_CONNECTIONS_DYNMAP_LAYER = getConfig().getOrDefault("CarriageConnectionsLayerPriority", 5);
    }

    private static void updateSizes() {
        portSizes.clear();
        Map<?, ?> portSizesMap = getConfig().getMap("PortSettings.sizes");
        for (Map.Entry<?, ?> entry : portSizesMap.entrySet()) {
            final String baseKey = "PortSettings.sizes." + entry.getKey().toString() + ".";
            if (entry.getValue() instanceof Map<?, ?>) {
                try {
                String formattedName = getConfig().getString(baseKey + "name").replace(' ', '_');
                if (formattedName.isEmpty()) {
                    throw new TravelNodeSizeSerialException("Port Size Failed to Serialize: Config contains error in port size section");
                }
                portSizes.put(formattedName, new PortSizeBuilder()
                    .setTier(getConfig().getInt(baseKey + "tier"))
                    .setName(getConfig().getString(baseKey + "name"))
                    .setRange(getConfig().getInt(baseKey + "range"))
                    .setCost(getConfig().getDouble(baseKey + "cost"))
                    .setSpeed(getConfig().getDouble(baseKey + "speed"))
                    .setMaxTownFee(getConfig().getDouble(baseKey + "maxTownFee"))
                    .setJourneyHaltRadius(getConfig().getDouble(baseKey + "journeyHaltRadius"))
                    .setIcon(Material.valueOf(getConfig().getString(baseKey + "icon")))
                    .createPortSize());
                } catch (TravelNodeSizeSerialException | IllegalArgumentException e) {
                    Logger.get().warn(e.getMessage());
                }
            }
        }

        carriageStationSizes.clear();
        Map<?, ?> carriageStationSizesMap = getConfig().getMap("CarriageStationSettings.sizes");
        for (Map.Entry<?, ?> entry : carriageStationSizesMap.entrySet()) {
            final String baseKey = "CarriageStationSettings.sizes." + entry.getKey().toString() + ".";
            if (entry.getValue() instanceof Map<?, ?>) {
                try {
                    String formattedName = getConfig().getString(baseKey + "name").replace(' ', '_');
                    if (formattedName.isEmpty()) {
                        throw new TravelNodeSizeSerialException("Carriage Station Size Failed to Serialize: Config contains error in carriage station size section");
                    }
                    carriageStationSizes.put(formattedName, new CarriageStationSizeBuilder()
                        .setTier(getConfig().getInt(baseKey + "tier"))
                        .setName(getConfig().getString(baseKey + "name"))
                        .setRange(getConfig().getInt(baseKey + "range"))
                        .setCost(getConfig().getDouble(baseKey + "cost"))
                        .setSpeed(getConfig().getDouble(baseKey + "speed"))
                        .setMaxTownFee(getConfig().getDouble(baseKey + "maxTownFee"))
                        .setJourneyHaltRadius(getConfig().getDouble(baseKey + "journeyHaltRadius"))
                        .setIcon(Material.valueOf(getConfig().getString(baseKey + "icon")))
                        .createPortSize());
                } catch (TravelNodeSizeSerialException | IllegalArgumentException e) {
                    Logger.get().warn(e.getMessage());
                }
            }
        }

        updateSizesInRegistries();
    }

    public static PortSize findPortSize(int tier) {
        for (PortSize portSize : portSizes.values()) {
            if (tier == portSize.getTier()) {
                return portSize;
            }
        }
        return ((ArrayList<PortSize>) portSizes.values()).get(0);
    }

    public static CarriageStationSize findCarriageStationSize(int tier) {
        for (CarriageStationSize carriageStationSize : carriageStationSizes.values()) {
            if (tier == carriageStationSize.getTier()) {
                return carriageStationSize;
            }
        }
        return ((ArrayList<CarriageStationSize>) carriageStationSizes.values()).get(0);
    }

    private static void updateSizesInRegistries() {
        for (Port port : TravelNodesManager.getPorts()) {
            boolean found = false;
            for (PortSize portSize : portSizes.values()) {
                if (port.getSize().getTier() == portSize.getTier()) {
                    port.setSize(portSize);
                    found = true;
                }
            }
            if (!found) {
                Iterator<PortSize> iterator = portSizes.values().iterator();
                if (iterator.hasNext()) {
                    port.setSize(iterator.next());
                }
            }
        }

        for (CarriageStation carriageStation : TravelNodesManager.getCarriageStations()) {
            boolean found = false;
            for (CarriageStationSize carriageStationSize : carriageStationSizes.values()) {
                if (carriageStation.getSize().getTier() == carriageStationSize.getTier()) {
                    carriageStation.setSize(carriageStationSize);
                    found = true;
                }
            }
            if (!found) {
                Iterator<CarriageStationSize> iterator = carriageStationSizes.values().iterator();
                if (iterator.hasNext()) {
                    carriageStation.setSize(iterator.next());
                }
            }
        }
    }

    public static World getWorld() {
        return Bukkit.getWorld(getConfig().getString("GlobalSettings.world"));
    }

    public static Set<ProtectedRegion> getCarriageStationRegions() {
        Set<ProtectedRegion> regions = new HashSet<>();
        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(new BukkitWorld(getWorld()));
        if (regionManager == null) {
            return regions;
        }
        for (String regionName : getConfig().getStringList("CarriageStationSettings.regions")) {
            ProtectedRegion region = regionManager.getRegion(regionName);
            if (region != null) {
                regions.add(region);
            }
        }
        return regions;
    }
}
