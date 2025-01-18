package io.github.alathra.alathraports.hook;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.manager.ICombatManager;
import io.github.alathra.alathraports.AlathraPorts;
import io.github.alathra.alathraports.Reloadable;
import org.bukkit.entity.Player;

public class CombatLogXHook implements Reloadable {

    private final AlathraPorts plugin;
    private static ICombatLogX combatLogXAPI;
    private static ICombatManager combatManager;

    public CombatLogXHook(AlathraPorts plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {
        if (!isCombatLogXLoaded()) {
            return;
        }

        combatLogXAPI = (ICombatLogX) plugin.getServer().getPluginManager().getPlugin("CombatLogX");
        if (combatLogXAPI != null) {
            combatManager = combatLogXAPI.getCombatManager();
        }
    }

    @Override
    public void onDisable() {

    }

    public boolean isCombatLogXLoaded() {
        return plugin.getServer().getPluginManager().isPluginEnabled("CombatLogX");
    }

    public ICombatLogX getCombatLogXAPI() {
        return combatLogXAPI;
    }

    public boolean isInCombat(Player player) {
        return combatManager.isInCombat(player);
    }

}
