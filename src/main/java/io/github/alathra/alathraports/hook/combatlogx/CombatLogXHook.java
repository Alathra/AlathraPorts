package io.github.alathra.alathraports.hook.combatlogx;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.manager.ICombatManager;
import io.github.alathra.alathraports.AlathraPorts;
import io.github.alathra.alathraports.hook.AbstractHook;
import io.github.alathra.alathraports.hook.Hook;
import org.bukkit.entity.Player;

public class CombatLogXHook extends AbstractHook {

    private static ICombatLogX combatLogXAPI;
    private static ICombatManager combatManager;

    public CombatLogXHook(AlathraPorts plugin) {
        super(plugin);
    }

    @Override
    public void onEnable(AlathraPorts plugin) {
        if (!isHookLoaded())
            return;

        combatLogXAPI = (ICombatLogX) plugin.getServer().getPluginManager().getPlugin("CombatLogX");
        if (combatLogXAPI != null) {
            combatManager = combatLogXAPI.getCombatManager();
        }
    }

    @Override
    public void onDisable(AlathraPorts plugin) {
        if (!isHookLoaded())
            return;
    }

    @Override
    public boolean isHookLoaded() {
        return isPluginPresent(Hook.CombatLogX.getPluginName()) && isPluginEnabled(Hook.CombatLogX.getPluginName());
    }

    public ICombatLogX getCombatLogXAPI() {
        return combatLogXAPI;
    }

    public boolean isInCombat(Player player) {
        return combatManager.isInCombat(player);
    }
}
