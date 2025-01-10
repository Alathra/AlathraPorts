package io.github.alathra.alathraports.travelnodes.carriagestations;

import io.github.alathra.alathraports.travelnodes.exceptions.TravelNodeSizeSerialException;
import org.bukkit.Material;

public class CarriageStationSizeBuilder {
    private int tier;
    private String name;
    private double cost;
    private double speed;
    private double journeyHaltRadius;
    private Material icon;

    public CarriageStationSizeBuilder setTier(int tier) {
        this.tier = tier;
        return this;
    }

    public CarriageStationSizeBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public CarriageStationSizeBuilder setCost(double cost) {
        this.cost = cost;
        return this;
    }

    public CarriageStationSizeBuilder setSpeed(double speed) {
        this.speed = speed;
        return this;
    }

    public CarriageStationSizeBuilder setWalkRadius(double journeyHaltRadius) {
        this.journeyHaltRadius = journeyHaltRadius;
        return this;
    }

    public CarriageStationSizeBuilder setIcon(Material icon) {
        this.icon = icon;
        return this;
    }

    public CarriageStationSize createPortSize() throws TravelNodeSizeSerialException {
        if (tier == 0 || name.isEmpty() || cost == 0.0 || speed == 0.0 || journeyHaltRadius == 0.0 || icon == null) {
            throw new TravelNodeSizeSerialException("Carriage Station Size Failed to Serialize: Config contains error in carriage station size section");
        }
        return new CarriageStationSize(tier, name, cost, speed, journeyHaltRadius, icon);
    }
}
