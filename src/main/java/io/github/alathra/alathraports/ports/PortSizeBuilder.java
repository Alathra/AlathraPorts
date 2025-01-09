package io.github.alathra.alathraports.ports;

import io.github.alathra.alathraports.ports.exceptions.PortSizeSerialException;
import org.bukkit.Material;

public class PortSizeBuilder {
    private int tier;
    private String name;
    private int range;
    private double cost;
    private double speed;
    private double journeyHaltRadius;
    private Material icon;

    public PortSizeBuilder setTier(int tier) {
        this.tier = tier;
        return this;
    }

    public PortSizeBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public PortSizeBuilder setRange(int range) {
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

    public PortSizeBuilder setWalkRadius(double journeyHaltRadius) {
        this.journeyHaltRadius = journeyHaltRadius;
        return this;
    }

    public PortSizeBuilder setIcon(Material icon) {
        this.icon = icon;
        return this;
    }

    public PortSize createPortSize() throws PortSizeSerialException {
        if (tier == 0 || name.isEmpty() || range == 0 || cost == 0.0 || speed == 0.0 || journeyHaltRadius == 0.0 || icon == null) {
            throw new PortSizeSerialException("Port Size Failed to Serialize: Config contains error in port size section");
        }
        return new PortSize(tier, name, range, cost, speed, journeyHaltRadius, icon);
    }
}