package io.github.alathra.alathraports.config;

import com.github.milkdrinkers.crate.Config;
import io.github.alathra.alathraports.AlathraPorts;
import io.github.alathra.alathraports.ports.PortSize;
import io.github.alathra.alathraports.ports.PortSizeBuilder;
import io.github.alathra.alathraports.ports.exceptions.PortSizeSerialException;
import io.github.alathra.alathraports.utility.Logger;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class Settings {

    /**
     * A convenience class for interfacing ConfigHandler that stores settings in memory
     */

    public static int MINIMUM_PORT_DISTANCE;
    public static double BASE_COST;
    public static double BASE_ANIMAL_COST;
    public static Material BLOCKADE_ICON;
    public static boolean SHOW_BLOCKADED;
    public static boolean REMOVE_PORT_ON_TOWN_DELETE;
    public static Map<String, PortSize> sizes = new HashMap<>();

    public static Config getConfig() {
        return AlathraPorts.getInstance().getConfigHandler().getConfig();
    }

    public static void update() {
        MINIMUM_PORT_DISTANCE = getConfig().getOrDefault("portSettings.minimumDistance", 10);
        BASE_COST = getConfig().getOrDefault("portSettings.baseCost", 5.00);
        BASE_ANIMAL_COST = getConfig().getOrDefault("portSettings.baseAnimalCost", 5.00);
        try {
            BLOCKADE_ICON = Material.valueOf(getConfig().getOrDefault("portSettings.blockadeIcon", "SKELETON_SKULL"));
        } catch (IllegalArgumentException e) {
            BLOCKADE_ICON = Material.SKELETON_SKULL;
            Logger.get().warn("Config Error: blockadeIcon is not set to a valid material");
        }
        SHOW_BLOCKADED = getConfig().getOrDefault("portSettings.showBlockaded", true);
        REMOVE_PORT_ON_TOWN_DELETE = getConfig().getOrDefault("portSettings.removePortOnTownDelete", true);
        updateSizes();
    }

    private static void updateSizes() {
        sizes.clear();
        Map<?, ?> sizesMap = getConfig().getMap("portSettings.sizes");
        for (Map.Entry<?, ?> entry : sizesMap.entrySet()) {
            final String baseKey = "portSettings.sizes." + entry.getKey().toString() + ".";
            if (entry.getValue() instanceof Map<?, ?>) {
                try {
                String formattedName = getConfig().getString(baseKey + "name").replace(' ', '_');
                if (formattedName.isEmpty()) {
                    throw new PortSizeSerialException("Port Size Failed to Serialize: Config contains error in port size section");
                }
                sizes.put(formattedName, new PortSizeBuilder()
                    .setTier(getConfig().getInt(baseKey + "tier"))
                    .setName(getConfig().getString(baseKey + "name"))
                    .setRange(getConfig().getInt(baseKey + "range"))
                    .setCost(getConfig().getDouble(baseKey + "cost"))
                    .setSpeed(getConfig().getDouble(baseKey + "speed"))
                    .setWalkRadius(getConfig().getDouble(baseKey + "journeyHaltRadius"))
                    .setIcon(Material.valueOf(getConfig().getString(baseKey + "icon")))
                    .createPortSize());
                } catch (PortSizeSerialException | IllegalArgumentException e) {
                    Logger.get().warn(e.getMessage());
                }
            }
        }
    }
}
