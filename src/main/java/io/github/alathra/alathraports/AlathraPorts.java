package io.github.alathra.alathraports;

import io.github.alathra.alathraports.command.CommandHandler;
import io.github.alathra.alathraports.config.ConfigHandler;
import io.github.alathra.alathraports.database.DBAction;
import io.github.alathra.alathraports.database.handler.DatabaseHandler;
import io.github.alathra.alathraports.database.handler.DatabaseHandlerBuilder;
import io.github.alathra.alathraports.hook.HookManager;
import io.github.alathra.alathraports.listener.ListenerHandler;
import io.github.alathra.alathraports.utility.DB;
import io.github.alathra.alathraports.utility.Logger;
import io.github.milkdrinkers.colorparser.ColorParser;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Main class.
 */
public class AlathraPorts extends JavaPlugin {

    private static AlathraPorts instance;

    // Handlers/Managers
    private ConfigHandler configHandler;
    private DatabaseHandler databaseHandler;
    private HookManager hookManager;
    private CommandHandler commandHandler;
    private ListenerHandler listenerHandler;

    private List<? extends Reloadable> handlers;

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

        configHandler = new ConfigHandler(this);
        hookManager = new HookManager(this);
        databaseHandler = new DatabaseHandlerBuilder()
            .withConfigHandler(configHandler)
            .withLogger(getComponentLogger())
            .build();
        commandHandler = new CommandHandler(this);
        listenerHandler = new ListenerHandler(this);

        handlers = List.of(
            configHandler,
            hookManager,
            databaseHandler,
            commandHandler,
            listenerHandler
        );

        DB.init(databaseHandler);
        for (Reloadable handler : handlers)
            handler.onLoad(instance);
    }

    @Override
    public void onEnable() {

        for (Reloadable handler : handlers)
            handler.onEnable(instance);

        if (!DB.isReady()) {
            Logger.get().warn(ColorParser.of("<yellow>DatabaseHolder handler failed to start. Database support has been disabled.").build());
            Bukkit.getPluginManager().disablePlugin(this);
        }

        DBAction.registerPortsFromDB();
        DBAction.registerCarriageStationsFromDB();
        DBAction.initPeriodicDBSaving();
    }

    @Override
    public void onDisable() {
        DBAction.stopPeriodicDBSaving();

        for (Reloadable handler : handlers.reversed()) // If reverse doesn't work implement a new List with your desired disable order
            handler.onDisable(instance);
    }


    @NotNull
    public ConfigHandler getConfigHandler() {
        return configHandler;
    }

    @NotNull
    public DatabaseHandler getDatabaseHandler() {
        return databaseHandler;
    }

    @NotNull
    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    @NotNull
    public ListenerHandler getListenerHandler() {
        return listenerHandler;
    }
}
