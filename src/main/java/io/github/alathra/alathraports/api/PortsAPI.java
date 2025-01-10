package io.github.alathra.alathraports.api;

import io.github.alathra.alathraports.travelnodes.ports.Port;
import io.github.alathra.alathraports.travelnodes.TravelNodesManager;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public class PortsAPI {

    public static @Nullable Port getPortByName(String name) {
        return TravelNodesManager.getPortByName(name);
    }

    public static @Nullable Port getPortByUUID(UUID uuid) {
        return TravelNodesManager.getPortByID(uuid);
    }

    public static @Nullable Port getPortFromSign(Block block) {
        return TravelNodesManager.getPortFromSign(block);
    }

    // All active (registered ports)
    public static Set<Port> getAllPorts() {
        return TravelNodesManager.getPorts();
    }

    public static void setBlockade(Port port, boolean blockade) {
        port.setBlockaded(blockade);
    }

    public static boolean isBlockaded(Port port) {
        return port.isBlockaded();
    }

}
