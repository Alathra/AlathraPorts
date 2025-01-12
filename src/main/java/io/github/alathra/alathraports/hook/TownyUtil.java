package io.github.alathra.alathraports.hook;

import com.palmergames.bukkit.towny.object.Town;
import io.github.alathra.alathraports.core.TravelNode;

public class TownyUtil {
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
