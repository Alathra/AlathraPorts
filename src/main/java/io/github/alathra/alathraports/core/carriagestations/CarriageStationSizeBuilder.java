package io.github.alathra.alathraports.core.carriagestations;

import io.github.alathra.alathraports.core.exceptions.TravelNodeSizeSerialException;
import org.bukkit.Material;

public class CarriageStationSizeBuilder {
    private int tier;
    private String name;
    private int range;
    private double cost;
    private double speed;
    private double maxTownFee;
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

    public CarriageStationSizeBuilder setRange(int range) {
        this.range = range;
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

    public CarriageStationSizeBuilder setMaxTownFee(double maxTownFee) {
        this.maxTownFee = maxTownFee;
        return this;
    }

    public CarriageStationSizeBuilder setJourneyHaltRadius(double journeyHaltRadius) {
        this.journeyHaltRadius = journeyHaltRadius;
        return this;
    }

    public CarriageStationSizeBuilder setIcon(Material icon) {
        this.icon = icon;
        return this;
    }

    public CarriageStationSize createPortSize() throws TravelNodeSizeSerialException {
        if (tier == 0 || name.isEmpty() || range == 0 || cost == 0.0 || speed == 0.0 || journeyHaltRadius == 0.0 || icon == null) {
            throw new TravelNodeSizeSerialException("Carriage Station Size Failed to Serialize: Config contains error in carriage station size section");
        }
        return new CarriageStationSize(tier, name, range, cost, speed, maxTownFee, journeyHaltRadius, icon);
    }
}
