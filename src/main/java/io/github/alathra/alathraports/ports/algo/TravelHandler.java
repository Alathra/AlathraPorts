package io.github.alathra.alathraports.ports.algo;

import com.github.milkdrinkers.colorparser.ColorParser;
import io.github.alathra.alathraports.AlathraPorts;
import io.github.alathra.alathraports.config.Settings;
import io.github.alathra.alathraports.ports.Port;
import io.github.alathra.alathraports.ports.PortSize;
import io.github.alathra.alathraports.ports.PortsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import java.util.*;

// cmd next on click with long "a-->b-->c-->d"
/* if player tries to use port whilst mid-travel, shows
 * "You are mid-journey. Are you sure you want to cancel?
 * Cancel travel
 *
 * For travel function, runs on a delay with the player. map of players and traveled is parsed through
 * and if the player leaves the port area, it's just reset to null & does nothing. (Cancel via distance
 * is handled by PlayerListener)
 */

/**
 * Handles all movement of players: stores journeys and teleports
 *
 * @author NinjaMandalorian
 */
public class TravelHandler {

    private static AlathraPorts instance;

	private static final HashMap<Player, List<Port>> journeys = new HashMap<Player, List<Port>>();
	private static final ArrayList<Player> enroutePlayers = new ArrayList<Player>();

	/**
	 * Starts a port journey for a player.
	 *
	 * @param player      - Player doing journey
	 * @param origin      - Start port
	 * @param destination - Destination port
	 * @param args        - Extra arguments
	 */
	public static void startJourney(Player player, Port origin, Port destination, String... args) {
        final Economy economy = AlathraPorts.getVaultHook().getEconomy();

		List<Port> playerJourney = findPath(player, origin, destination);
		if (playerJourney == null) {
			player.sendMessage(ColorParser.of("<red>There is no route to this port").build());
			return;
		}

		double cost = getJourneyCost(playerJourney);
		if (economy.getBalance(player) < cost) {
            player.sendMessage(ColorParser.of("<red>You need <gold>" + economy.format(cost) + " <red>for tickets").build());
			return;
		}

		player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
		journeys.put(player, playerJourney);
		scheduleNext(player);
	}

	/**
	 * Cancels port journey for a player.
	 *
	 * @param player - Player to cancel journey of.
	 */
	public static void cancelJourney(Player player) {
		journeys.remove(player);
		enroutePlayers.remove(player);
		player.sendMessage(ColorParser.of("<red>Journey cancelled").build());
	}

	/**
	 * Schedules next port travel.
	 *
	 * @param player - Player to schedule
	 */

	public static void scheduleNext(Player player) {
            instance = AlathraPorts.getInstance();
			if (enroutePlayers.contains(player) && !journeys.containsKey(player))
				return;
			enroutePlayers.add(player);

			List<Port> journey = journeys.get(player);
			long time = getWait(journey.get(0), journey.get(1));

			Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () -> next(player), time);
			player.sendMessage(ColorParser.of("<green>You will be teleported in <light_purple> " + time / 20 + " <green>seconds").build());
	}

	/**
	 * Runs next port.
	 *
	 * @param player - Player to run next port.
	 */
	private static void next(Player player) {
        instance = AlathraPorts.getInstance();
        final Economy economy = AlathraPorts.getVaultHook().getEconomy();

		List<Port> playerJourney = journeys.get(player);
		if (playerJourney == null)
			return;

		// Economy notif & withdraw
		final double cost = getTravelCost(journeys.get(player).getFirst(), journeys.get(player).get(1));

		if(AlathraPorts.getVaultHook().getEconomy().getBalance(player) < cost){
            player.sendMessage(ColorParser.of("<red>You need <gold>" + economy.format(cost) + " <red>for tickets").build());
			cancelJourney(player);
			return;
		}
		player.sendMessage(ColorParser.of("<green>You bought a ticket for " + economy.format(cost)).build());
		economy.withdrawPlayer(player, cost);

		player.teleport(playerJourney.get(1).getTeleportLocation(), TeleportCause.PLUGIN);

        playerJourney.removeFirst();
		enroutePlayers.remove(player);

		player.getWorld().playSound(player, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1, 1);
		if (playerJourney.size() > 1) {
            Component component = ColorParser.of("<white>[<green>Next Port<white>]").build()
                .hoverEvent(HoverEvent.showText(Component.text("Click to begin travelling...")))
                .clickEvent(ClickEvent.runCommand("/implodusports:iports next"));
            player.sendMessage(component);
			journeys.put(player, playerJourney);
		} else {
			journeys.put(player, null);
            Port finalPort = playerJourney.getFirst();
            Component component = ColorParser.of("<green>You have arrived at <light_purple>" + finalPort.getName()).build()
                .hoverEvent(HoverEvent.showText(Component.text("Location: " + finalPort.getTeleportLocation().getBlockX()
                    + ", " + finalPort.getTeleportLocation().getBlockZ())));
            player.sendMessage(component);
		}
		return;
	}

	/**
	 * Finds path between two ports for a player.
	 *
	 * @param player      - Player to find path for.
	 * @param origin      - Start port.
	 * @param destination - End port.
	 * @return List of ports to go through.
	 */
	public static List<Port> findPath(Player player, Port origin, Port destination) {
		if (origin.getNearby().contains(destination))
			return Arrays.asList(origin, destination);
		return AStarAlgorithm.findShortestPath(PortsManager.getPorts(), origin, destination);
	}

	/**
	 * Gets the journey wait (in ticks) for a player.
	 *
	 * @param player - Player to check for
	 * @return Wait in ticks
	 */

	private static long getJourneyWait(Player player) {
		return getJourneyWait(journeys.get(player));
	}

	/**
	 * Gets the journey wait (in ticks) for a port list.
	 *
	 * @param journey - Port list
	 * @return Wait in ticks
	 */
	public static long getJourneyWait(List<Port> journey) {
		long cumulativeTime = 0L;
		for (int i = 1; i < journey.size(); i++) {
			cumulativeTime += getWait(journey.get(i - 1), journey.get(i));
		}
		return cumulativeTime;
	}

	/**
	 * Gets the cost for a player's journey
	 *
	 * @param playerJourney - Journey list
	 * @return Double of cost
	 */
	public static double getJourneyCost(List<Port> playerJourney) {
		double totalCost = 0.0;
		for (int i = 1; i < playerJourney.size(); i++) {
			totalCost += getTravelCost(playerJourney.get(i - 1), playerJourney.get(i));
		}

		return totalCost;
	}

	/**
	 * Gets journey cost of player
	 *
	 * @param player - Journey player
	 * @return Double of cost
	 */

	public static double getJourneyCost(Player player) {
		return getJourneyCost(journeys.get(player));
	}

	/**
	 * Gets the wait for travel between two ports.
	 *
	 * @param origin      - Start port
	 * @param destination - End port
	 * @return Long of time (ticks)
	 */

	private static long getWait(Port origin, Port destination) {
		Double distance = origin.distanceTo(destination);
		PortSize size = PortsManager.getPortSizeByTier(Math.min(origin.getSize().getTier(), destination.getSize().getTier()));
        return Math.round(distance / Objects.requireNonNull(size).getSpeed()) * 20L + 100;
	}

	/**
	 * Gets travel cost between two ports
	 *
	 * @param origin      - Start port
	 * @param destination - Emd port
	 * @return Double of cost
	 */
	private static double getTravelCost(Port origin, Port destination) {
        PortSize size = PortsManager.getPortSizeByTier(Math.min(origin.getSize().getTier(), destination.getSize().getTier()));
        return Settings.BASE_COST += Objects.requireNonNull(size).getCost() * origin.distanceTo(destination) / 100;
	}

	/**
	 * Gets if player can travel to port
	 *
	 * @param port   - Port to travel to
	 * @param player - Player travelling
	 * @return Boolean of if available
	 */
	public static boolean canTravelTo(Port port, Player player) {
		return true;
	}

	/**
	 * Gets a player's current port
	 *
	 * @param player - Player to check
	 * @return Port of player
	 */
	public static Port getCurrentPort(Player player) {
		if (journeys.get(player) == null)
			return null;
		return journeys.get(player).getFirst();
	}

}