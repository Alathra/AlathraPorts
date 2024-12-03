package io.github.alathra.alathraports.ports;

import io.github.alathra.alathraports.ports.exceptions.PortSizeSerialException;

public class PortSizeBuilder {
    private int tier;
    private String name;
    private double range;
    private double cost;
    private double speed;
    private double walkRadius;

    public PortSizeBuilder setTier(int tier) {
        this.tier = tier;
        return this;
    }

    public PortSizeBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public PortSizeBuilder setRange(double range) {
        this.range = range;
        return this;
    }

    public PortSizeBuilder setCost(double cost) {
        this.cost = cost;
        return this;
    }

    public PortSizeBuilder setSpeed(double speed) {
        this.speed = speed;
        return this;
    }

    public PortSizeBuilder setWalkRadius(double walkRadius) {
        this.walkRadius = walkRadius;
        return this;
    }

    public PortSize createPortSize() throws PortSizeSerialException {
        if (tier == 0 || name.isEmpty() || range == 0.0 || cost == 0.0 || speed == 0.0 || walkRadius == 0.0) {
            throw new PortSizeSerialException("Port Size Failed to Serialize: Config contains error in port size section");
        }
        return new PortSize(tier, name, range, cost, speed, walkRadius);
    }
}