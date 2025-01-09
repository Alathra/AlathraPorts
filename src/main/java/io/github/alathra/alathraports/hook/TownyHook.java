package io.github.alathra.alathraports.hook;

import com.palmergames.bukkit.towny.TownyAPI;
import io.github.alathra.alathraports.AlathraPorts;
import io.github.alathra.alathraports.Reloadable;

public class TownyHook implements Reloadable {

    private final AlathraPorts plugin;
    private TownyAPI townyAPI;

    public TownyHook(AlathraPorts plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {
        if (!isTownyLoaded()) {
            return;
        }

        townyAPI = TownyAPI.getInstance();
    }

    @Override
    public void onDisable() {

    }

    public boolean isTownyLoaded() {
        return plugin.getServer().getPluginManager().isPluginEnabled("Towny");
    }

    public TownyAPI getTownyAPI() {
        return townyAPI;
    }
}
