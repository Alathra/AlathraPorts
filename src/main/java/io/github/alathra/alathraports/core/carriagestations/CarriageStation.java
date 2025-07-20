package io.github.alathra.alathraports.core.carriagestations;

import io.github.milkdrinkers.colorparser.ColorParser;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.github.alathra.alathraports.config.Settings;
import io.github.alathra.alathraports.core.TravelNode;
import io.github.alathra.alathraports.core.TravelNodesManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;

import java.util.*;

public class CarriageStation extends TravelNode implements Cloneable {

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
        ArrayList<TravelNode> carriageStations = new ArrayList<>();
        if (this.isBlockaded) {
            return carriageStations;
        }
        for (CarriageStation carriageStation : TravelNodesManager.getCarriageStations()) {
            if (carriageStation.equals(this) || carriageStation.isBlockaded) {
                continue;
            }
            double distance = this.distanceTo(carriageStation);
            if (distance > (double) ((CarriageStationSize) this.size).range && distance > (double) ((CarriageStationSize) carriageStation.size).getRange()) {
                continue;
            }
            for (ProtectedRegion region : Settings.getCarriageStationRegions()) {
                if (region.contains(signLocation.getBlockX(), signLocation.getBlockY(), signLocation.getBlockZ()) &&
                region.contains(carriageStation.signLocation.getBlockX(), carriageStation.signLocation.getBlockY(), carriageStation.signLocation.getBlockZ())) {
                    carriageStations.add(carriageStation);
                    break;
                }
            }
        }

        return carriageStations;
    }

    public static Component getTagline() {
        return ColorParser.of("<gold>\uD83D\uDC0E\uD83D\uDC0E\uD83D\uDC0E").build();
    }

    @Override
    public CarriageStation clone() {
        return (CarriageStation) super.clone();
    }
}
