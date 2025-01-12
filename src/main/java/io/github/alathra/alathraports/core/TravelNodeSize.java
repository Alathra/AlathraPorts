package io.github.alathra.alathraports.core;

import org.bukkit.Material;

public abstract class TravelNodeSize {

    protected final int tier;
    protected final String name;
    protected final double cost;
    protected final double speed;
    protected final double maxTownFee;
    protected final double journeyHaltRadius;
    protected final Material icon;

    protected TravelNodeSize(int tier, String name, double cost, double speed, double maxTownFee, double journeyHaltRadius, Material icon) {
        this.tier = tier;
        this.name = name;
        this.cost = cost;
        this.speed = speed;
        this.maxTownFee = maxTownFee;
        this.journeyHaltRadius = journeyHaltRadius;
        this.icon = icon;
    }

    public int getTier() {
        return tier;
    }

    public String getName() {
        return name;
    }

    public double getCost() {
        return cost;
    }

    public double getSpeed() {
        return speed;
    }

    public double getMaxTownFee() {
        return maxTownFee;
    }

    public double getJourneyHaltRadius() {
        return journeyHaltRadius;
    }

    public Material getIcon() {
        return  icon;
    }

}
