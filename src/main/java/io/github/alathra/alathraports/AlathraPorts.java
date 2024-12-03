package io.github.alathra.alathraports;

import com.github.milkdrinkers.colorparser.ColorParser;
import io.github.alathra.alathraports.command.CommandHandler;
import io.github.alathra.alathraports.config.ConfigHandler;
import io.github.alathra.alathraports.hook.PAPIHook;
import io.github.alathra.alathraports.hook.VaultHook;
import io.github.alathra.alathraports.listener.ListenerHandler;
import io.github.alathra.alathraports.utility.Logger;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Main class.
 */
public class AlathraPorts extends JavaPlugin {
    private static AlathraPorts instance;
    private ConfigHandler configHandler;
    //private DatabaseHandler databaseHandler;
    private CommandHandler commandHandler;
    private ListenerHandler listenerHandler;

    // Hooks
    private static VaultHook vaultHook;
    private static PAPIHook papiHook;

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
        //databaseHandler = new DatabaseHandler(configHandler, getComponentLogger());
        commandHandler = new CommandHandler(instance);
        listenerHandler = new ListenerHandler(instance);
        vaultHook = new VaultHook(instance);
        papiHook = new PAPIHook(instance);

        configHandler.onLoad();
        //databaseHandler.onLoad();
        commandHandler.onLoad();
        listenerHandler.onLoad();
        vaultHook.onLoad();
        papiHook.onLoad();
    }

    @Override
    public void onEnable() {
        configHandler.onEnable();
        //databaseHandler.onEnable();
        commandHandler.onEnable();
        listenerHandler.onEnable();
        vaultHook.onEnable();
        papiHook.onEnable();

        //if (!databaseHandler.isRunning()) {
            //Logger.get().warn(ColorParser.of("<yellow>Database handler failed to start. Database support has been disabled.").build());
        //}

        if (vaultHook.isVaultLoaded()) {
            Logger.get().info(ColorParser.of("<green>Vault has been found on this server. Vault support enabled.").build());
        } else {
            Logger.get().warn(ColorParser.of("<yellow>Vault is not installed on this server. Vault support has been disabled.").build());
        }

    }

    @Override
    public void onDisable() {
        configHandler.onDisable();
        //databaseHandler.onDisable();
        commandHandler.onDisable();
        listenerHandler.onDisable();
        vaultHook.onDisable();
        papiHook.onDisable();
    }

    /**
     * Gets data handler.
     *
     * @return the data handler
     */
    //@NotNull
    //public DatabaseHandler getDataHandler() {
        //return databaseHandler;
    //}

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

}
