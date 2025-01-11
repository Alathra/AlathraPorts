package io.github.alathra.alathraports.api;

import com.palmergames.bukkit.towny.object.Town;
import io.github.alathra.alathraports.AlathraPorts;
import io.github.alathra.alathraports.travelnodes.TravelNode;
import io.github.alathra.alathraports.travelnodes.carriagestations.CarriageStation;
import io.github.alathra.alathraports.travelnodes.ports.Port;
import io.github.alathra.alathraports.travelnodes.TravelNodesManager;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public class PortsAPI {

    public static @Nullable Port getPortByName(String name) {
        return TravelNodesManager.getPortByName(name);
    }

    public static @Nullable Port getPortByUUID(UUID uuid) {
        return TravelNodesManager.getPortByID(uuid);
    }

    public static @Nullable Port getPortFromSign(Block block) {
        return TravelNodesManager.getPortFromSign(block);
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

    public static @Nullable CarriageStation getCarriageStationByName(String name) {
        return TravelNodesManager.getCarriageStationByName(name);
    }

    public static @Nullable CarriageStation getCarriageStationByUUID(String name) {
        return TravelNodesManager.getCarriageStationByName(name);
    }

    public static @Nullable CarriageStation getCarriageStationFromSign(Block block) {
        return TravelNodesManager.getCarriageStationFromSign(block);
    }

    // All active (registered) ports
    public static Set<Port> getAllPorts() {
        return TravelNodesManager.getPorts();
    }

    // All active (registered) carriage station
    public static Set<CarriageStation> getAllCarriageStations() {
        return TravelNodesManager.getCarriageStations();
    }

    public static void setBlockade(TravelNode node, boolean blockade) {
        node.setBlockaded(blockade);
    }

    public static boolean isBlockaded(TravelNode node) {
        return node.isBlockaded();
    }

}
