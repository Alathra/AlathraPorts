package io.github.alathra.alathraports.travelnodes.journey;

import io.github.alathra.alathraports.utility.Logger;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class JourneyManager {

    // All active journeys (ones where a player is traveling or queued to travel)
    private static final Set<Journey> journeys = new HashSet<>();

    public static Set<Journey> getJourneys() {
        return journeys;
    }

    public static void pruneJourneys() {
        journeys.removeIf(journey -> !journey.isValid());
    }

    public static void registerJourney(Journey journey) {
        pruneJourneys();
        if (journeys.contains(journey)) {
            Logger.get().warn("Detected duplicate journey when attempting to register");
            return;
        }
        journeys.add(journey);
    }

    public static void deregisterJourney(Journey journey) {
        pruneJourneys();
        if (!journeys.contains(journey)) {
            Logger.get().warn("Detected missing journey when attempting to deregister");
            return;
        }
        journeys.remove(journey);
    }

    public static boolean isPlayerInOngoingJourney(Player player) {
        pruneJourneys();
        for (Journey journey : getJourneys()) {
            if (journey.getPlayer().equals(player)) {
                return true;
            }
        }
        return false;
    }

    public static Journey getJourneyFromPlayer(Player player) {
        pruneJourneys();
        for (Journey journey : getJourneys()) {
            if (journey.getPlayer().equals(player)) {
                return journey;
            }
        }
        return null;
    }


}
