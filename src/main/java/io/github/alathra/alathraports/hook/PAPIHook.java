package io.github.alathra.alathraports.hook;

import io.github.alathra.alathraports.AlathraPorts;
import io.github.alathra.alathraports.Reloadable;
import org.bukkit.Bukkit;

/**
 * A hook to interface with <a href="https://wiki.placeholderapi.com/">PlaceholderAPI</a>.
 */
public class PAPIHook implements Reloadable {
    private final AlathraPorts plugin;
    private final static String pluginName = "PlaceholderAPI";
    private PAPIExpansion PAPIExpansion;

    /**
     * Instantiates a new PlaceholderAPI hook.
     *
     * @param plugin the plugin instance
     */
    public PAPIHook(AlathraPorts plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onLoad() {
    }

    @Override
    public void onEnable() {
        if (!Bukkit.getPluginManager().isPluginEnabled(pluginName))
            return;

        PAPIExpansion = new PAPIExpansion(plugin);
    }

    @Override
    public void onDisable() {
        if (!Bukkit.getPluginManager().isPluginEnabled(pluginName))
            return;

        PAPIExpansion.unregister();
        PAPIExpansion = null;
    }
}
