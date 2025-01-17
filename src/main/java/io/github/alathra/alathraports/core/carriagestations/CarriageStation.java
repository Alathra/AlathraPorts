package io.github.alathra.alathraports.core.carriagestations;

import com.github.milkdrinkers.colorparser.ColorParser;
import io.github.alathra.alathraports.core.TravelNode;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;

import java.util.*;

public class CarriageStation extends TravelNode implements Cloneable {

    private final Set<TravelNode> directConnections = new HashSet<>();

    public CarriageStation(UUID uuid, String name, CarriageStationSize size, Location signLocation, Location teleportLocation) {
        super(uuid, name, size, signLocation, teleportLocation);
        super.type = TravelNodeType.CARRIAGE_STATION;
    }

    public CarriageStation(String name, CarriageStationSize size, Location signLocation, Location teleportLocation) {
        super(name, size, signLocation, teleportLocation);
        super.type = TravelNodeType.CARRIAGE_STATION;
    }

    @Override
    public Sign generateNodeSign(Sign sign) {
        sign.getSide(Side.FRONT).line(0, CarriageStation.getTagline());
        sign.getSide(Side.FRONT).line(1, ColorParser.of("<dark_green><bold>" + this.name).build());
        sign.getSide(Side.FRONT).line(2, ColorParser.of("<red>" + this.size.getName()).build());
        sign.getSide(Side.FRONT).line(3, CarriageStation.getTagline());
        sign.getSide(Side.BACK).line(0, CarriageStation.getTagline());
        sign.getSide(Side.BACK).line(1, ColorParser.of("<dark_green><bold>" + this.name).build());
        sign.getSide(Side.BACK).line(2, ColorParser.of("<red>" + this.size.getName()).build());
        sign.getSide(Side.BACK).line(3, CarriageStation.getTagline());
        return sign;
    }

    @Override
    public List<TravelNode> getDirectConnections() {
        return new ArrayList<>(directConnections);
    }

    public void addDirectConnection(TravelNode node) {
        if (node.equals(this)) {
            return;
        }
        directConnections.add(node);
    }

    public void removeIfDirectlyConnected(CarriageStation carriageStation) {
        for (TravelNode node : directConnections) {
            if (node.isSimilar(carriageStation)) {
                directConnections.remove(node);
                return;
            }
        }
    }

    public static Component getTagline() {
        return ColorParser.of("<gold>\uD83D\uDC0E\uD83D\uDC0E\uD83D\uDC0E").build();
    }

    @Override
    public CarriageStation clone() {
        return (CarriageStation) super.clone();
    }
}
