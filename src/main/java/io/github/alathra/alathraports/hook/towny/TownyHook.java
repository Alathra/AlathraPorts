package io.github.alathra.alathraports.hook.towny;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import io.github.alathra.alathraports.AlathraPorts;
import io.github.alathra.alathraports.core.TravelNode;
import io.github.alathra.alathraports.hook.AbstractHook;
import io.github.alathra.alathraports.hook.Hook;

public class TownyHook extends AbstractHook {
    private TownyAPI townyAPI;

    public TownyHook(AlathraPorts plugin) {
        super(plugin);
    }

    @Override
    public void onEnable(AlathraPorts plugin) {
        if (!isHookLoaded()) {
            return;
        }

        townyAPI = TownyAPI.getInstance();
    }

    @Override
    public void onDisable(AlathraPorts plugin) {
        if (!isHookLoaded())
            return;
    }

    @Override
    public boolean isHookLoaded() {
        return isPluginPresent(Hook.Towny.getPluginName()) && isPluginEnabled(Hook.Towny.getPluginName());
    }

    public TownyAPI getTownyAPI() {
        return townyAPI;
    }

    public static void addToTownBank(TravelNode node, double amount) {
        if (node.getTown() == null) {
            return;
        }
        Town town = node.getTown();
        // Town banks only accept integers. Round (ceiling) to the highest whole number and add to town bank
        int bankAmount = (int) Math.ceil(amount);
        switch (node.getType()) {
            case PORT -> town.getAccount().deposit(bankAmount, "Port tax");
            case CARRIAGE_STATION -> town.getAccount().deposit(bankAmount, "Carriage station tax");
        }
    }
}