package io.github.alathra.alathraports.hook.worldguard;

import io.github.alathra.alathraports.AlathraPorts;
import io.github.alathra.alathraports.hook.AbstractHook;
import io.github.alathra.alathraports.hook.Hook;

public class WorldguardHook extends AbstractHook {


    public WorldguardHook(AlathraPorts plugin) {
        super(plugin);
    }

    @Override
    public void onEnable(AlathraPorts plugin) {
        if (!isHookLoaded())
            return;
    }

    @Override
    public void onDisable(AlathraPorts plugin) {
        if (!isHookLoaded())
            return;
    }

    @Override
    public boolean isHookLoaded() {
        return isPluginPresent(Hook.Worldguard.getPluginName()) && isPluginEnabled(Hook.Worldguard.getPluginName());
    }
}
