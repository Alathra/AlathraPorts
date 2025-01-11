package io.github.alathra.alathraports.travelnodes.carriagestations;

import com.github.milkdrinkers.colorparser.ColorParser;
import io.github.alathra.alathraports.travelnodes.TravelNode;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;

import java.util.*;

public class CarriageStation extends TravelNode {

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

    public static Component getTagline() {
        return ColorParser.of("<gold>\uD83D\uDC0E\uD83D\uDC0E\uD83D\uDC0E").build();
    }
}
