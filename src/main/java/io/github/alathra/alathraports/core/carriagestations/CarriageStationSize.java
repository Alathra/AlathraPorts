package io.github.alathra.alathraports.core.carriagestations;


import io.github.alathra.alathraports.core.TravelNodeSize;
import org.bukkit.Material;

public class CarriageStationSize extends TravelNodeSize {

    protected CarriageStationSize(int tier, String name, double cost, double speed, double maxTownFee, double journeyHaltRadius, Material icon) {
        super(tier, name, cost, speed, maxTownFee, journeyHaltRadius, icon);
    }

}
