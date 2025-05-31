package io.github.alathra.alathraports.listener;

import io.github.alathra.alathraports.AlathraPorts;
import io.github.alathra.alathraports.Reloadable;
import io.github.alathra.alathraports.hook.Hook;
import io.github.alathra.alathraports.listener.portlisteners.SignListeners;
import io.github.alathra.alathraports.listener.portlisteners.external.TownyListeners;

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
    public void onLoad(AlathraPorts plugin) {
    }

    @Override
    public void onEnable(AlathraPorts plugin) {
        // Register listeners here
        if (Hook.getTownyHook().isHookLoaded()) {
            plugin.getServer().getPluginManager().registerEvents(new TownyListeners(), plugin);
        }

        plugin.getServer().getPluginManager().registerEvents(new SignListeners(), plugin);
    }

    @Override
    public void onDisable(AlathraPorts plugin) {
    }
}
