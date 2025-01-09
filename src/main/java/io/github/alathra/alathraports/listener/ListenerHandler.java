package io.github.alathra.alathraports.listener;

import io.github.alathra.alathraports.AlathraPorts;
import io.github.alathra.alathraports.Reloadable;
import io.github.alathra.alathraports.listener.portlisteners.SignListeners;
import io.github.alathra.alathraports.listener.portlisteners.optional.TownyListeners;

/**
 * A class to handle registration of event listeners.
 */
public class ListenerHandler implements Reloadable {
    private final AlathraPorts plugin;

    /**
     * Instantiates the Listener handler.
     *
     * @param plugin the plugin instance
     */
    public ListenerHandler(AlathraPorts plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onLoad() {
    }

    @Override
    public void onEnable() {
        // Register listeners here
        if (AlathraPorts.getVaultHook().isVaultLoaded()) {
            plugin.getServer().getPluginManager().registerEvents(new VaultListener(), plugin);
        }
        if (AlathraPorts.getTownyHook().isTownyLoaded()) {
            plugin.getServer().getPluginManager().registerEvents(new TownyListeners(), plugin);
        }

        plugin.getServer().getPluginManager().registerEvents(new SignListeners(), plugin);
    }

    @Override
    public void onDisable() {
    }
}
