package io.github.alathra.alathraports;

import com.github.milkdrinkers.colorparser.ColorParser;
import io.github.alathra.alathraports.command.CommandHandler;
import io.github.alathra.alathraports.config.ConfigHandler;
import io.github.alathra.alathraports.core.TravelNodesManager;
import io.github.alathra.alathraports.core.carriagestations.CarriageStation;
import io.github.alathra.alathraports.core.exceptions.TravelNodeRegisterException;
import io.github.alathra.alathraports.core.ports.Port;
import io.github.alathra.alathraports.database.Queries;
import io.github.alathra.alathraports.database.handler.DatabaseHandlerBuilder;
import io.github.alathra.alathraports.hook.*;
import io.github.alathra.alathraports.listener.ListenerHandler;
import io.github.alathra.alathraports.utility.DB;
import io.github.alathra.alathraports.utility.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Main class.
 */
public class AlathraPorts extends JavaPlugin {
    private static AlathraPorts instance;
    private ConfigHandler configHandler;
    private CommandHandler commandHandler;
    private ListenerHandler listenerHandler;

    // Hooks
    private static VaultHook vaultHook;
    private static PAPIHook papiHook;
    private static TownyHook townyHook;
    private static CombatLogXHook combatLogXHook;
    private static DynmapHook dynmapHook;

    /**
     * Gets plugin instance.
     *
     * @return the plugin instance
     */
    public static AlathraPorts getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
        configHandler = new ConfigHandler(instance);
        DB.init(
            new DatabaseHandlerBuilder()
                .withConfigHandler(configHandler)
                .withLogger(getComponentLogger())
                .build()
        );
        commandHandler = new CommandHandler(instance);
        listenerHandler = new ListenerHandler(instance);
        vaultHook = new VaultHook(instance);
        papiHook = new PAPIHook(instance);
        townyHook = new TownyHook(instance);
        combatLogXHook = new CombatLogXHook(instance);
        dynmapHook = new DynmapHook(instance);

        configHandler.onLoad();
        DB.getHandler().onLoad();
        commandHandler.onLoad();
        listenerHandler.onLoad();
        vaultHook.onLoad();
        papiHook.onLoad();
        townyHook.onLoad();
        combatLogXHook.onLoad();
        dynmapHook.onLoad();
    }

    @Override
    public void onEnable() {
        configHandler.onEnable();
        DB.getHandler().onEnable();
        commandHandler.onEnable();
        listenerHandler.onEnable();
        vaultHook.onEnable();
        papiHook.onEnable();
        townyHook.onEnable();
        combatLogXHook.onEnable();
        dynmapHook.onEnable();

        if (!DB.isReady()) {
            Logger.get().warn(ColorParser.of("<yellow>DatabaseHolder handler failed to start. Database support has been disabled.").build());
            Bukkit.getPluginManager().disablePlugin(this);
        }

        if (vaultHook.isVaultLoaded()) {
            Logger.get().info(ColorParser.of("<green>Vault has been found on this server. Vault support enabled.").build());
        } else {
            Logger.get().warn(ColorParser.of("<yellow>Vault is not installed on this server. Vault support has been disabled.").build());
            Logger.get().error(ColorParser.of("<red>Vault is a hard dependency. Plugin disabling...").build());
            this.getServer().getPluginManager().disablePlugin(this);
        }

        if (townyHook.isTownyLoaded()) {
            Logger.get().info(ColorParser.of("<green>Towny has been found on this server. Towny support enabled.").build());
        } else {
            Logger.get().warn(ColorParser.of("<yellow>Towny is not installed on this server. Towny support has been disabled.").build());
            Logger.get().error(ColorParser.of("<red>Vault is a hard dependency. Plugin disabling...").build());
            this.getServer().getPluginManager().disablePlugin(this);
        }

        if (combatLogXHook.isCombatLogXLoaded()) {
            Logger.get().info(ColorParser.of("<green>CombatLogX has been found on this server. CombatLogX support enabled.").build());
        } else {
            Logger.get().warn(ColorParser.of("<yellow>CombatLogX is not installed on this server. CombatLogX support has been disabled.").build());
        }

        if (dynmapHook.isDynmapLoaded()) {
            Logger.get().info(ColorParser.of("<green>Dynmap has been found on this server. Dynmap support enabled.").build());
        } else {
            Logger.get().warn(ColorParser.of("<yellow>Dynmap is not installed on this server. Dynmap support has been disabled.").build());
        }

        registerPortsFromDB();
        registerCarriageStationsFromDB();

    }

    @Override
    public void onDisable() {
        configHandler.onDisable();
        DB.getHandler().onDisable();
        commandHandler.onDisable();
        listenerHandler.onDisable();
        vaultHook.onDisable();
        papiHook.onDisable();
        townyHook.onDisable();
        combatLogXHook.onDisable();
        dynmapHook.onDisable();
    }

    /**
     * Gets config handler.
     *
     * @return the config handler
     */
    @NotNull
    public ConfigHandler getConfigHandler() {
        return configHandler;
    }

    /**
     * Gets vault hook.
     *
     * @return the vault hook
     */
    @NotNull
    public static VaultHook getVaultHook() {
        return vaultHook;
    }

    @NotNull
    public static TownyHook getTownyHook() {
        return townyHook;
    }

    @NotNull
    public static CombatLogXHook getCombatLogXHook() {
        return combatLogXHook;
    }

    @NotNull
    public static DynmapHook getDynmapHook() {
        return dynmapHook;
    }

    public static void registerPortsFromDB() {
        Queries.Ports.loadAllPorts().thenAccept(data -> {
            for (Port port : data) {
                try {
                    TravelNodesManager.registerPort(port);
                } catch (TravelNodeRegisterException e) {
                    io.github.alathra.alathraports.utility.Logger.get().warn(e.getMessage());
                }
            }
        });
    }

    public static void registerCarriageStationsFromDB() {
        Queries.Carriages.loadAllCarriages().thenAccept(data -> {
            for (CarriageStation carriageStation : data.keySet()) {
                // Register carriage station
                try {
                    TravelNodesManager.registerCarriageStation(carriageStation);
                } catch (TravelNodeRegisterException e) {
                    io.github.alathra.alathraports.utility.Logger.get().warn(e.getMessage());
                    continue;
                }
                // Match UUID of direct connections in registered carriage stations and add
                for (UUID uuid : data.get(carriageStation)) {
                    for (CarriageStation registeredCarriageStation : TravelNodesManager.getCarriageStations()) {
                        if (registeredCarriageStation.isSimilar(carriageStation)) {
                            continue;
                        }
                        if (uuid.equals(registeredCarriageStation.getUuid())) {
                            carriageStation.addDirectConnection(registeredCarriageStation);
                        }
                    }
                }
            }
        });
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
