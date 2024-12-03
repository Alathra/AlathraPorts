package io.github.alathra.alathraports.ports;


public class PortSize {

    private final int tier;
    private final String name;
    private final double range;
    private final double cost;
    private final double speed;
    private final double walkRadius;

    protected PortSize(int tier, String name, double range, double cost, double speed, double walkRadius) {
        this.tier = tier;
        this.name = name;
        this.cost = cost;
        this.range = range;
        this.speed = speed;
        this.walkRadius = walkRadius;
    }

    public int getTier() {
        return tier;
    }

    public String getName() {
        return name;
    }

    public double getRange() {
        return range;
    }

    public double getCost() {
        return cost;
    }

    public double getSpeed() {
        return speed;
    }

    public double getWalkRadius() {
        return walkRadius;
    }
}
