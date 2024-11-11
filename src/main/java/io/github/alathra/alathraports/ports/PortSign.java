package io.github.alathra.alathraports.ports;

import com.github.milkdrinkers.colorparser.ColorParser;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;

public class PortSign {

    private final Sign sign;

    private static final Component tagline = ColorParser.of("<blue>⚓⚓⚓").build();

    public PortSign(Sign sign, Port port) {
        this.sign = sign;
        sign.getSide(Side.FRONT).line(0, tagline);
        sign.getSide(Side.FRONT).line(1, ColorParser.of("<gold><bold>" + port.getName()).build());
        sign.getSide(Side.FRONT).line(2, ColorParser.of("<light_red>" + port.getPortSizeName()).build());
        sign.getSide(Side.FRONT).line(3, tagline);
        sign.getSide(Side.BACK).line(0, tagline);
        sign.getSide(Side.BACK).line(1, ColorParser.of("<gold><bold>" + port.getName()).build());
        sign.getSide(Side.BACK).line(2, ColorParser.of("<light_red>" + port.getPortSizeName()).build());
        sign.getSide(Side.BACK).line(3, tagline);
        sign.update();
    }

    public Sign getBase() {
        return sign;
    }

    public static Component getTagline() {
        return tagline;
    }
}
