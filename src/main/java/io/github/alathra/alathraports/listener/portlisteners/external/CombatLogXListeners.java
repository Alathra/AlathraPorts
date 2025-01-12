package io.github.alathra.alathraports.listener.portlisteners.external;

import com.github.sirblobman.combatlogx.api.event.PlayerTagEvent;
import io.github.alathra.alathraports.core.journey.Journey;
import io.github.alathra.alathraports.core.journey.JourneyManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CombatLogXListeners implements Listener {

    // When a player is tagged in combat, halt their ongoing journey (if applicable)
    @EventHandler
    public void onCombatTag(PlayerTagEvent event) {
        Player player = event.getPlayer();
        if (JourneyManager.isPlayerInOngoingJourney(player)) {
            Journey journey = JourneyManager.getJourneyFromPlayer(player);
            if (journey != null) {
                journey.halt();
                journey.stop();
            }
        }
    }
}
