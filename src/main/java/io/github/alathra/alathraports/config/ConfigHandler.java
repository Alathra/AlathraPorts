package io.github.alathra.alathraports.config;

import io.github.alathra.alathraports.AlathraPorts;
import io.github.alathra.alathraports.Reloadable;
import io.github.alathra.alathraports.hook.Hook;
import io.github.milkdrinkers.crate.Config;
import io.github.milkdrinkers.crate.ConfigBuilder;
import io.github.milkdrinkers.crate.internal.settings.ReloadSetting;

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
    public void onLoad(AlathraPorts plugin) {
        cfg = ConfigBuilder
            .fromPath("config", plugin.getDataFolder().getPath())
            .addInputStream(plugin.getResource("config.yml"))
            .setReloadSetting(ReloadSetting.MANUALLY)
            .create();
        Settings.update();
        databaseCfg = new Config("database", plugin.getDataFolder().getPath(), plugin.getResource("database.yml"));
    }

    @Override
    public void onEnable(AlathraPorts plugin) {
    }

    @Override
    public void onDisable(AlathraPorts plugin) {
    }

    public void reloadConfig() {
        cfg.forceReload();
        Settings.update();
        Hook.getDynmapHook().refreshAllMarkers();
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
