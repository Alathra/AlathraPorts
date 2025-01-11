package io.github.alathra.alathraports.travelnodes.ports;

import com.github.milkdrinkers.colorparser.ColorParser;
import io.github.alathra.alathraports.travelnodes.TravelNode;
import io.github.alathra.alathraports.travelnodes.TravelNodesManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;

import java.util.*;

public class Port extends TravelNode {

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

    public List<Port> getPortsInRange() {
        ArrayList<Port> ports = new ArrayList<>();
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

    public List<Port> getReachablePorts() {
        // Implements Breadth-First Traversal to generate the "graph" of reachable ports
        if (this.isBlockaded) {
            return Collections.emptyList();
        }
        HashSet<Port> visited = new HashSet<>();
        visited.add(this);
        LinkedList<Port> queue = new LinkedList<>();
        queue.add(this);
        Port node;
        while (!queue.isEmpty()) {
            node = queue.poll();
            visited.add(node);
            for (Port port : node.getPortsInRange()) {
                if (!visited.contains(port)) {
                    queue.add(port);
                }
            }
        }
        // Don't include own port
        visited.remove(this);
        return visited.stream().toList();
    }

    public static Component getTagline() {
        return ColorParser.of("<blue>⚓⚓⚓").build();
    }
}
