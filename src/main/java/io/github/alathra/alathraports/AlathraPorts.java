package io.github.alathra.alathraports;

import com.github.milkdrinkers.colorparser.ColorParser;
import io.github.alathra.alathraports.command.CommandHandler;
import io.github.alathra.alathraports.config.ConfigHandler;
import io.github.alathra.alathraports.database.handler.DatabaseHandler;
import io.github.alathra.alathraports.hook.BStatsHook;
import io.github.alathra.alathraports.hook.PAPIHook;
import io.github.alathra.alathraports.hook.ProtocolLibHook;
import io.github.alathra.alathraports.hook.VaultHook;
import io.github.alathra.alathraports.listener.ListenerHandler;
import io.github.alathra.alathraports.translation.TranslationManager;
import io.github.alathra.alathraports.updatechecker.UpdateChecker;
import io.github.alathra.alathraports.utility.Logger;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Main class.
 */
public class AlathraPorts extends JavaPlugin {
    private static AlathraPorts instance;
    private ConfigHandler configHandler;
    private TranslationManager translationManager;
    //private DatabaseHandler databaseHandler;
    private CommandHandler commandHandler;
    private ListenerHandler listenerHandler;
    private UpdateChecker updateChecker;

    // Hooks
    private static BStatsHook bStatsHook;
    private static VaultHook vaultHook;
    private static ProtocolLibHook protocolLibHook;
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
        translationManager = new TranslationManager(instance);
        //databaseHandler = new DatabaseHandler(configHandler, getComponentLogger());
        commandHandler = new CommandHandler(instance);
        listenerHandler = new ListenerHandler(instance);
        updateChecker = new UpdateChecker();
        bStatsHook = new BStatsHook(instance);
        vaultHook = new VaultHook(instance);
        protocolLibHook = new ProtocolLibHook(instance);
        papiHook = new PAPIHook(instance);

        configHandler.onLoad();
        translationManager.onLoad();
        //databaseHandler.onLoad();
        commandHandler.onLoad();
        listenerHandler.onLoad();
        updateChecker.onLoad();
        bStatsHook.onLoad();
        vaultHook.onLoad();
        protocolLibHook.onLoad();
        papiHook.onLoad();
    }

    @Override
    public void onEnable() {
        configHandler.onEnable();
        translationManager.onEnable();
        //databaseHandler.onEnable();
        commandHandler.onEnable();
        listenerHandler.onEnable();
        updateChecker.onEnable();
        bStatsHook.onEnable();
        vaultHook.onEnable();
        protocolLibHook.onEnable();
        papiHook.onEnable();

        //if (!databaseHandler.isRunning()) {
            //Logger.get().warn(ColorParser.of("<yellow>Database handler failed to start. Database support has been disabled.").build());
        //}

        if (vaultHook.isVaultLoaded()) {
            Logger.get().info(ColorParser.of("<green>Vault has been found on this server. Vault support enabled.").build());
        } else {
            Logger.get().warn(ColorParser.of("<yellow>Vault is not installed on this server. Vault support has been disabled.").build());
        }

        if (protocolLibHook.isHookLoaded()) {
            Logger.get().info(ColorParser.of("<green>ProtocolLib has been found on this server. ProtocolLib support enabled.").build());
        } else {
            Logger.get().warn(ColorParser.of("<yellow>ProtocolLib is not installed on this server. ProtocolLib support has been disabled.").build());
        }
    }

    @Override
    public void onDisable() {
        configHandler.onDisable();
        translationManager.onDisable();
        //databaseHandler.onDisable();
        commandHandler.onDisable();
        listenerHandler.onDisable();
        updateChecker.onDisable();
        bStatsHook.onDisable();
        vaultHook.onDisable();
        protocolLibHook.onDisable();
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
     * Gets config handler.
     *
     * @return the translation handler
     */
    @NotNull
    public TranslationManager getTranslationManager() {
        return translationManager;
    }

    /**
     * Gets update checker.
     *
     * @return the update checker
     */
    @NotNull
    public UpdateChecker getUpdateChecker() {
        return updateChecker;
    }

    /**
     * Gets bStats hook.
     *
     * @return the bStats hook
     */
    @NotNull
    public static BStatsHook getBStatsHook() {
        return bStatsHook;
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

    /**
     * Gets ProtocolLib hook.
     *
     * @return the ProtocolLib hook
     */
    @NotNull
    public static ProtocolLibHook getProtocolLibHook() {
        return protocolLibHook;
    }
}
