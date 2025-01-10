package io.github.alathra.alathraports.travelnodes.carriagestations;


import io.github.alathra.alathraports.travelnodes.TravelNodeSize;
import org.bukkit.Material;

public class CarriageStationSize extends TravelNodeSize {

    protected CarriageStationSize(int tier, String name, double cost, double speed, double journeyHaltRadius, Material icon) {
        super(tier, name, cost, speed, journeyHaltRadius, icon);
    }

}
