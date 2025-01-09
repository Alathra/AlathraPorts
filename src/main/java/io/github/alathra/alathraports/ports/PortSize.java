package io.github.alathra.alathraports.ports;


import org.bukkit.Material;

public class PortSize {

    private final int tier;
    private final String name;
    private final int range;
    private final double cost;
    private final double speed;
    private final double journeyHaltRadius;
    private final Material icon;

    protected PortSize(int tier, String name, int range, double cost, double speed, double journeyHaltRadius, Material icon) {
        this.tier = tier;
        this.name = name;
        this.cost = cost;
        this.range = range;
        this.speed = speed;
        this.journeyHaltRadius = journeyHaltRadius;
        this.icon = icon;
    }

    public int getTier() {
        return tier;
    }

    public String getName() {
        return name;
    }

    public int getRange() {
        return range;
    }

    public double getCost() {
        return cost;
    }

    public double getSpeed() {
        return speed;
    }

    public double getJourneyHaltRadius() {
        return journeyHaltRadius;
    }

    public Material getIcon() {
        return  icon;
    }
}
