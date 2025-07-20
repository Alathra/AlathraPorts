package io.github.alathra.alathraports.core.journey;

import io.github.milkdrinkers.colorparser.ColorParser;
import io.github.alathra.alathraports.AlathraPorts;
import io.github.alathra.alathraports.api.PortsAPI;
import io.github.alathra.alathraports.config.Settings;
import io.github.alathra.alathraports.core.TravelNode;
import io.github.alathra.alathraports.core.carriagestations.CarriageStationSize;
import io.github.alathra.alathraports.core.ports.PortSize;
import io.github.alathra.alathraports.hook.Hook;
import io.github.alathra.alathraports.hook.towny.TownyHook;
import io.github.alathra.alathraports.utility.Logger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Journey {

    // All the ports required to travel through to reach the destination (inclusive)
    private List<TravelNode> nodes = new ArrayList<>();
    // If journey parameters indicate impossible or error-prone journey
    private boolean isValid = true;
    // If the journey has been halted by an internal process, either do to an error or intentionally
    private boolean halted = false;
    // The current index of the travel nodes, the index of the port location of the player in a journey
    private int currentIndex;
    // The current bukkit task (if any) that is running, the scheduled task (delay) the next teleport in the journey
    private int currentTravelTask;
    // Non-null if the player is mounted when the journey starts (i.e. horse)
    private Entity mounted;
    // The number of animals the player is bringing with them on the journey (mounted + leashed)
    private int numAnimals;

    // The start travel node
    private final TravelNode origin;
    // The end travel node
    private final TravelNode destination;
    // The player embarking on the journey
    private final Player player;
    // The type of journey it is (ports or carriage station)
    private final TravelNode.TravelNodeType type;

    public Journey(TravelNode origin, TravelNode destination, Player player) {
        this.origin = origin;
        this.destination = destination;
        this.player = player;
        this.type = origin.getType();

        if (origin.getDirectConnections().contains(destination)) {
            nodes = Arrays.asList(origin, destination);
        } else {
            // Find the shortest path for journey
            nodes = AStarAlgorithm.findShortestPath(origin, destination);
        }

        // Journey must be at least 2 ports and origin and destination must be correctly in path
        if (nodes == null || nodes.isEmpty() || nodes.size() < 2 || !(origin.equals(nodes.get(0))) || !(destination.equals(nodes.get(nodes.size()-1)))) {
            isValid = false;
        }

        // origin
        currentIndex = 0;
    }

    public void start() {
        if (!this.isValid) {
            player.sendMessage(ColorParser.of("<red>Travel Journey Failed: Internal Error").build());
            Logger.get().warn("Travel Journey Failed: {} was trying to travel from {} to {}", player.getName(), origin.getName(), destination.getName());
            JourneyManager.deregisterJourney(this);
            return;
        }
        for (Journey journey : JourneyManager.getJourneys()) {
            if (!journey.equals(this) && journey.getPlayer().equals(this.player)) {
                // Player is already in a different journey, prevent it from starting
                Logger.get().warn("Travel Journey Failed: {} was trying to travel from {} to {}. Player was already in a journey",
                    player.getName(), origin.getName(), destination.getName());
                return;
            }
        }
        final Economy economy = Hook.getVaultHook().getEconomy();
        double cost = getTotalCost();
        if (economy.getBalance(player) < cost) {
            player.sendMessage(ColorParser.of("<red>You need <gold>" + economy.format(cost) + " <red>to travel to <green>" + destination.getName()).build());
            return;
        }
        JourneyManager.registerJourney(this);
        player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        if (player.getVehicle() != null) {
            mounted = player.getVehicle();
        }
        travel();
    }

    // When a journey has been completed
    public void stop() {
        JourneyManager.deregisterJourney(this);
    }

    // Initiate travel sequence. Will travel to each node (port) until arrived at destination
    public void travel() {
        if (!isValid  || !(JourneyManager.getJourneys().contains(this)) || halted) {
            halt();
            stop();
            return;
        }

        if (Hook.getCombatLogXHook().isHookLoaded()) {
            if (Hook.getCombatLogXHook().isInCombat(player)) {
                halt();
                stop();
                return;
            }
        }

        int time = getTime(nodes.get(currentIndex), nodes.get(currentIndex+1));
        currentTravelTask = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(AlathraPorts.getInstance(), () -> {
            // Take money from player for each node traveled
            updateNumAnimals();
            final Economy economy = Hook.getVaultHook().getEconomy();
            final double cost = getCost(nodes.get(currentIndex), nodes.get(currentIndex+1));
            if (economy.getBalance(player) < cost) {
                player.sendMessage(ColorParser.of("<red>You no longer have enough funds to complete this journey").build());
                halt();
                stop();
                return;
            }
            economy.withdrawPlayer(player, cost);
            // Attempt to add tax to town bank, if applicable
            if (Hook.getTownyHook().isHookLoaded()) {
               TravelNode intermediateDestination = nodes.get(currentIndex+1);
               if (intermediateDestination.getTown() != null) {
                   double tax = getCost(nodes.get(currentIndex), intermediateDestination) * intermediateDestination.getTownFee();
                   TownyHook.addToTownBank(intermediateDestination, tax);
               }
            }
            currentIndex++;
            if (mounted != null) {
                mounted.teleport(nodes.get(currentIndex).getTeleportLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            }
            List<LivingEntity> leashedAnimals = getLeasedAnimals();
            for (LivingEntity entity : leashedAnimals) {
                entity.setLeashHolder(null);
                entity.teleport(nodes.get(currentIndex).getTeleportLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            }
            player.teleport(nodes.get(currentIndex).getTeleportLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            player.getWorld().playSound(player, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1, 1);
            for (LivingEntity entity : leashedAnimals) {
                entity.setLeashHolder(player);
            }
            // If at destination (last node)
            if (currentIndex == nodes.size()-1) {
                player.sendMessage(ColorParser.of("<green>You have arrived at your destination, <light_purple>" + destination.getName()).build());
                stop();
            } else {
                player.sendMessage(ColorParser.of("<green>Arrived at <light_purple>" + nodes.get(currentIndex).getName() + ". <green>Traveling to next location... <light_purple>" + nodes.get(currentIndex+1).getName() ).build());
                travel();
            }
            player.sendMessage(ColorParser.of("<red>-" + economy.format(cost)).build());
        }, time * 20L);
        player.sendMessage(ColorParser.of("<green>You will be teleported in <light_purple>" + time + " <green>seconds").build());
    }

    // Interrupt an ongoing journey
    public void halt() {
        halted = true;
        Bukkit.getServer().getScheduler().cancelTask(currentTravelTask);
        player.sendMessage(ColorParser.of("<red>Your journey to <light_purple>" + destination.getName() + " <red>has been halted").build());
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 1);
    }

    // Resume a journey that has been halted
    public void resume() {
        halted = false;
        player.sendMessage(ColorParser.of("<green>Your journey has resumed").build());
        travel();
    }

    // Get cost to travel between two nodes
    // In dollars (or whatever the currency is)
    public double getCost(TravelNode node1, TravelNode node2) {
        switch (type) {
            case PORT:
                PortSize portSize = PortsAPI.getPortSizeByTier(Math.min(node2.getSize().getTier(), node1.getSize().getTier()));
                // Calculate base cost based on port parameters
                double portCost = Settings.BASE_COST + (portSize != null ? portSize.getCost() : 1.0) * node1.distanceTo(node2) / 100;
                // Add animal fees (if any)
                portCost += (Settings.BASE_ANIMAL_COST * numAnimals);
                // Add town fees (if any)
                portCost += (portCost * node2.getTownFee());
                // round cost to 2 decimal places
                return (double) Math.round((portCost * 100)) / 100;
            case CARRIAGE_STATION:
                CarriageStationSize carriageStationSize = PortsAPI.getCarriageStationSizeByTier(Math.min(node2.getSize().getTier(), node1.getSize().getTier()));
                double carriageStationCost = Settings.BASE_COST + (carriageStationSize != null ? carriageStationSize.getCost() : 1.0) * node1.distanceTo(node2) / 100;
                carriageStationCost += (Settings.BASE_ANIMAL_COST * numAnimals);
                carriageStationCost += (carriageStationCost * node2.getTownFee());
                return (double) Math.round((carriageStationCost * 100)) / 100;
        }
        return 0.0;
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
    public int getTime(TravelNode node1, TravelNode node2) {
        return switch (type) {
            case PORT -> {
                PortSize portSize = PortsAPI.getPortSizeByTier(Math.min(node1.getSize().getTier(), node2.getSize().getTier()));
                yield (int) (Math.round(node1.distanceTo(node2) / (portSize != null ? portSize.getSpeed() : 1.0)) + 5);
            }
            case CARRIAGE_STATION -> {
                CarriageStationSize carriageStationSize = PortsAPI.getCarriageStationSizeByTier(Math.min(node1.getSize().getTier(), node2.getSize().getTier()));
                yield (int) (Math.round(node1.distanceTo(node2) / (carriageStationSize != null ? carriageStationSize.getSpeed() : 1.0)) + 5);
            }
        };
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

    // Get the number of animals the player currently has leaded
    public List<LivingEntity> getLeasedAnimals() {
        List<LivingEntity> leashedAnimals = new ArrayList<>();
        // Get nearby entities in 20 block radius
        for (Entity entity : player.getNearbyEntities(12,12,12)) {
            if (entity instanceof LivingEntity livingEntity) {
                try {
                    if (livingEntity.getLeashHolder() instanceof Player playerEntity) {
                        if (playerEntity.equals(player)) {
                            leashedAnimals.add(livingEntity);
                        }
                    }
                } catch (IllegalStateException ignored) {}
            }

        }
        return leashedAnimals;
    }

    // Find the animals the player is traveling with (mounted + leashed)
    public void updateNumAnimals() {
        numAnimals = 0;
        if (mounted != null) {
            numAnimals++;
        } else {
            if (player.getVehicle() != null) {
                mounted = player.getVehicle();
                numAnimals++;
            }
        }
        numAnimals += getLeasedAnimals().size();
    }

    public List<TravelNode> getNodes() {
        return nodes;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public TravelNode getOrigin() {
        return origin;
    }

    public TravelNode getDestination() {
        return destination;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isValid() {
        return isValid;
    }

    public boolean isHalted() {
        return isHalted();
    }

    public int getNumAnimals() {
        return numAnimals;
    }

    public TravelNode.TravelNodeType getType() {
        return type;
    }

    public void setNumAnimals(int numAnimals) {
        this.numAnimals = numAnimals;
    }
}
