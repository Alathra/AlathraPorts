package io.github.alathra.alathraports.core.ports;


import io.github.alathra.alathraports.core.TravelNodeSize;
import org.bukkit.Material;

public class PortSize extends TravelNodeSize {

    int range;

    protected PortSize(int tier, String name, double cost, double speed, double maxTownFee, double journeyHaltRadius, Material icon, int range) {
        super(tier, name, cost, speed, maxTownFee, journeyHaltRadius, icon);
        this.range = range;
    }

    public int getRange() {
        return range;
    }

}
