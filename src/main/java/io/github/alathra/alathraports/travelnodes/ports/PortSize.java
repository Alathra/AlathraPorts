package io.github.alathra.alathraports.travelnodes.ports;


import io.github.alathra.alathraports.travelnodes.TravelNodeSize;
import org.bukkit.Material;

public class PortSize extends TravelNodeSize {

    int range;

    protected PortSize(int tier, String name, double cost, double speed, double journeyHaltRadius, Material icon, int range) {
        super(tier, name, cost, speed, journeyHaltRadius, icon);
        this.range = range;
    }

    public int getRange() {
        return range;
    }

}
