package io.github.alathra.alathraports.config;

import com.github.milkdrinkers.crate.Config;
import com.github.milkdrinkers.crate.internal.settings.ReloadSetting;
import io.github.alathra.alathraports.AlathraPorts;
import io.github.alathra.alathraports.Reloadable;

import javax.inject.Singleton;

/**
 * A class that generates/loads {@literal &} provides access to a configuration file.
 */
@Singleton
public class ConfigHandler implements Reloadable {
    private final AlathraPorts plugin;
    private Config cfg;
    private Config databaseCfg;

    /**
     * Instantiates a new Config handler.
     *
     * @param plugin the plugin instance
     */
    public ConfigHandler(AlathraPorts plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onLoad() {
        cfg = new Config("config", plugin.getDataFolder().getPath(), plugin.getResource("config.yml")); // Create a config file from the template in our resources folder
        Settings.update();
        cfg.setReloadSetting(ReloadSetting.MANUALLY);
        databaseCfg = new Config("database", plugin.getDataFolder().getPath(), plugin.getResource("database.yml"));
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    public void reloadConfig() {
        cfg.forceReload();
        Settings.update();
    }

    public void reloadDBConfig() {
        databaseCfg.forceReload(); // TODO Unnecessary, changes are loaded from disk the next time this config object is accessed anyways
    }

    /**
     * Gets main config object.
     *
     * @return the config object
     */
    public Config getConfig() {
        return cfg;
    }

    /**
     * Gets database config object.
     *
     * @return the config object
     */
    public Config getDatabaseConfig() {
        return databaseCfg;
    }
}
