package io.github.alathra.alathraports.database;

import io.github.alathra.alathraports.AlathraPorts;
import io.github.alathra.alathraports.config.Settings;
import io.github.alathra.alathraports.core.TravelNodesManager;
import io.github.alathra.alathraports.core.carriagestations.CarriageStation;
import io.github.alathra.alathraports.core.exceptions.TravelNodeRegisterException;
import io.github.alathra.alathraports.core.ports.Port;
import io.github.alathra.alathraports.utility.Logger;
import org.bukkit.Bukkit;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DBAction {

    private static int DBSaveTask;

    public static void initPeriodicDBSaving() {
        // Starts after 5 minutes, runs at interval defined by config
        DBSaveTask = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(AlathraPorts.getInstance(), () -> {
            saveAllPortsToDB();
            saveAllCarriageStationsToDB();
        }, 300 * 20L, Settings.DATA_SAVE_INTERVAL* 20L);
    }

    public static void stopPeriodicDBSaving() {
        Bukkit.getServer().getScheduler().cancelTask(DBSaveTask);
    }

    public static void registerPortsFromDB() {
        final Set<Port> ports = Queries.Ports.loadAllPorts();
        for (Port port : ports) {
            try {
                TravelNodesManager.registerPort(port);
            } catch (TravelNodeRegisterException e) {
                // Prune port since it couldn't be registered
                deletePortFromDB(port);
                Logger.get().warn(e.getMessage());
            }
        }
    }

    public static void registerCarriageStationsFromDB() {
        final Set<CarriageStation> carriageStations = Queries.Carriages.loadAllCarriageStations();
        for (CarriageStation carriageStation : carriageStations) {
            try {
                TravelNodesManager.registerCarriageStation(carriageStation);
            } catch (TravelNodeRegisterException e) {
                // Prune port since it couldn't be registered
                deleteCarriageStationFromDB(carriageStation);
                Logger.get().warn(e.getMessage());
            }
        }
    }

    public static void saveAllPortsToDB() {
        Queries.Ports.saveAllPorts(TravelNodesManager.getPorts());
    }

    public static void saveAllCarriageStationsToDB() {
        Queries.Carriages.saveAllCarriages(TravelNodesManager.getCarriageStations());
    }

    public static void deletePortFromDB(Port port) {
        Queries.Ports.deletePortQuery(port);
    }

    public static void deleteCarriageStationFromDB(CarriageStation carriageStation) {
        Queries.Carriages.deleteCarriageQuery(carriageStation);
    }
}
