package io.github.alathra.alathraports.core.ports;

import com.github.milkdrinkers.colorparser.ColorParser;
import io.github.alathra.alathraports.core.TravelNode;
import io.github.alathra.alathraports.core.TravelNodesManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;

import java.util.*;

public class Port extends TravelNode implements Cloneable {

    public Port(UUID uuid, String name, PortSize size, Location signLocation, Location teleportLocation) {
        super(uuid, name, size, signLocation, teleportLocation);
        super.type = TravelNodeType.PORT;
    }

    public Port(String name, PortSize size, Location signLocation, Location teleportLocation) {
        super(name, size, signLocation, teleportLocation);
        super.type = TravelNodeType.PORT;
    }

    @Override
    public Sign generateNodeSign(Sign sign) {
        sign.getSide(Side.FRONT).line(0, Port.getTagline());
        sign.getSide(Side.FRONT).line(1, ColorParser.of("<dark_green><bold>" + this.name).build());
        sign.getSide(Side.FRONT).line(2, ColorParser.of("<red>" + this.size.getName()).build());
        sign.getSide(Side.FRONT).line(3, Port.getTagline());
        sign.getSide(Side.BACK).line(0, Port.getTagline());
        sign.getSide(Side.BACK).line(1, ColorParser.of("<dark_green><bold>" + this.name).build());
        sign.getSide(Side.BACK).line(2, ColorParser.of("<red>" + this.size.getName()).build());
        sign.getSide(Side.BACK).line(3, Port.getTagline());
        return sign;
    }

    @Override
    public List<TravelNode> getDirectConnections() {
        ArrayList<TravelNode> ports = new ArrayList<>();
        if (this.isBlockaded) {
            return ports;
        }
        for (Port port : TravelNodesManager.getPorts()) {
            if (port.equals(this) || port.isBlockaded) {
                continue;
            }
            double distance = this.distanceTo(port);
            if (distance > (double) ((PortSize) this.size).range && distance > (double) ((PortSize) port.size).getRange()) {
                continue;
            }
            ports.add(port);
        }

        return ports;
    }

    public static Component getTagline() {
        return ColorParser.of("<blue>⚓⚓⚓").build();
    }

    @Override
    public Port clone() {
        return (Port) super.clone();
    }
}
