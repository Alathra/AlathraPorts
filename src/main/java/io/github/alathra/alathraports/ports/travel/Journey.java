package io.github.alathra.alathraports.ports.travel;

import com.github.milkdrinkers.colorparser.ColorParser;
import io.github.alathra.alathraports.AlathraPorts;
import io.github.alathra.alathraports.config.Settings;
import io.github.alathra.alathraports.ports.Port;
import io.github.alathra.alathraports.ports.PortSize;
import io.github.alathra.alathraports.ports.PortsManager;
import io.github.alathra.alathraports.utility.Logger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Journey {

    // All the ports required to travel through to reach the destination (inclusive)
    private List<Port> nodes = new ArrayList<>();
    // If journey parameters indicate impossible or error-prone journey
    private boolean isValid = true;
    // If the journey has been halted by an internal process, either do to an error or intentionally
    private boolean halted = false;
    // The current index of the travel nodes, the index of the port location of the player in a journey
    private int currentIndex;
    // The current bukkit task (if any) that is running, the scheduled task (delay) the next teleport in the journey
    private BukkitTask currentTravelTask;

    private final Port origin;
    private final Port destination;
    private final Player player;

    public Journey(Port origin, Port destination, Player player) {
        this.origin = origin;
        this.destination = destination;
        this.player = player;

        if (origin.getPortsInRange().contains(destination)) {
            nodes = Arrays.asList(origin, destination);
        } else {
            // Find the shortest path for journey
            nodes = AStarAlgorithm.findShortestPath(origin, destination);
        }

        // Journey must be at least 2 ports and origin and destination must be correctly in path
        if (nodes == null || nodes.isEmpty() || nodes.size() < 2 || !(origin.equals(nodes.getFirst())) || !(destination.equals(nodes.getLast()))) {
            isValid = false;
        }

        currentIndex = 0;
    }

    public void start() {
        if (!this.isValid) {
            player.sendMessage(ColorParser.of("<red>Travel Journey Failed: Internal Error").build());
            Logger.get().warn("Travel Journey Failed: {} was trying to travel from {} to {}", player.getName(), origin.getName(), destination.getName());
            TravelManager.deregisterJourney(this);
            return;
        }
        TravelManager.registerJourney(this);
        final Economy economy = AlathraPorts.getVaultHook().getEconomy();
        double cost = getTotalCost();
        if (economy.getBalance(player) < cost) {
            player.sendMessage(ColorParser.of("<red>You need <gold>" + economy.format(cost) + " <red>to travel to <green>" + destination.getName()).build());
            TravelManager.deregisterJourney(this);
            return;
        }
        for (Journey journey : TravelManager.getJourneys()) {
            if (!journey.equals(this) && journey.getPlayer().equals(this.player)) {
                // Player is already in a different journey, prevent it from starting
                return;
            }
        }

        player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        economy.withdrawPlayer(player, cost);
        TravelManager.registerJourney(this);
        travel();
    }

    // Initiate travel sequence. Will travel to each node (port) until arrived at destination
    public void travel() {
        if (!isValid  || !(TravelManager.getJourneys().contains(this)) || halted) {
            halt();
            return;
        }

        int time = getTime(nodes.get(currentIndex), nodes.get(currentIndex+1));
        currentTravelTask = Bukkit.getServer().getScheduler().runTaskLater(AlathraPorts.getInstance(), () -> {
            final Economy economy = AlathraPorts.getVaultHook().getEconomy();
            currentIndex++;
            player.teleport(nodes.get(currentIndex).getTeleportLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            player.getWorld().playSound(player, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1, 1);
            // If at destination (last node)
            if (currentIndex == nodes.size()-1) {
                player.sendMessage(ColorParser.of("You have arrived at your destination!").build());
            } else {
                player.sendMessage(ColorParser.of("Traveling to next location...").build());
                travel();
            }
        }, time * 20L);
        player.sendMessage(ColorParser.of("<green>You will be teleported in <light_purple> " + time + " <green>seconds").build());
    }

    // Halt ongoing journey
    public void halt() {
        halted = true;
        if (currentTravelTask != null) {
            currentTravelTask.cancel();
        }
        player.sendMessage(ColorParser.of("<red>Your journey has been halted").build());
    }

    // Halt ongoing journey
    public void resume() {
        halted = false;
        player.sendMessage(ColorParser.of("<green>Your journey has resumed").build());
    }

    // Get cost to travel between two nodes
    // In dollars (or whatever the currency is)
    public double getCost(Port node1, Port node2) {
        PortSize size = PortsManager.getPortSizeByTier(Math.min(node2.getSize().getTier(), node1.getSize().getTier()));
        return Settings.BASE_COST += (size != null ? size.getCost() : 0) * node1.distanceTo(node2) / 100;
    }

    // Get total cost for entire journey
    // In dollars (or whatever the currency is)
    public double getTotalCost() {
        double totalCost = 0.0;
        for (int i = 0; i < nodes.size()-1; i++) {
            totalCost += getCost(nodes.get(i), nodes.get(i+1));
        }
        return totalCost;
    }

    // Get time to travel between two nodes
    // In seconds
    public int getTime(Port node1, Port node2) {
        PortSize size = PortsManager.getPortSizeByTier(Math.min(node1.getSize().getTier(), node2.getSize().getTier()));
        return (int) (Math.round(node1.distanceTo(node2) / (size != null ? size.getSpeed() : 0)) + 5);
    }

    // Get total travel time for the entire journey
    // In seconds
    public int getTotalTime() {
        int totalTime = 0;
        for (int i = 0; i < nodes.size()-1; i++) {
            totalTime += getTime(nodes.get(i), nodes.get(i+1));
        }
        return totalTime;
    }

    public List<Port> getNodes() {
        return nodes;
    }

    public Port getOrigin() {
        return origin;
    }

    public Port getDestination() {
        return destination;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isValid() {
        return isValid;
    }
}
