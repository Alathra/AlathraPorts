package io.github.alathra.alathraports.hook;

import io.github.alathra.alathraports.AlathraPorts;
import io.github.alathra.alathraports.Reloadable;

public class WorldguardHook implements Reloadable {

    private final AlathraPorts plugin;

    public WorldguardHook(AlathraPorts plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {
        if (!isWorldGuardLoaded()) {
            return;
        }
    }

    @Override
    public void onDisable() {

    }

    public boolean isWorldGuardLoaded() {
        return plugin.getServer().getPluginManager().isPluginEnabled("WorldGuard");
    }
}
