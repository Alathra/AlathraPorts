package io.github.alathra.alathraports.api;

import io.github.alathra.alathraports.ports.Port;
import io.github.alathra.alathraports.ports.PortsManager;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public class PortsAPI {

    public static @Nullable Port getPortByName(String name) {
        return PortsManager.getPortByName(name);
    }

    public static @Nullable Port getPortByUUID(UUID uuid) {
        return PortsManager.getPortByID(uuid);
    }

    public static @Nullable Port getPortFromSign(Block block) {
        return PortsManager.getPortFromSign(block);
    }

    // All active (registered ports)
    public static Set<Port> getAllPorts() {
        return PortsManager.getPorts();
    }

    public static void setBlockade(Port port, boolean blockade) {
        port.setBlockaded(blockade);
    }

    public static boolean isBlockaded(Port port) {
        return port.isBlockaded();
    }

}
