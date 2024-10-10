package io.github.alathra.alathraports.listener;

import com.github.milkdrinkers.colorparser.ColorParser;
import io.github.alathra.alathraports.AlathraPorts;
import io.github.alathra.alathraports.translation.Translation;
import io.github.alathra.alathraports.utility.Cfg;
import io.github.alathra.alathraports.updatechecker.SemanticVersion;
import io.github.alathra.alathraports.updatechecker.UpdateChecker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Sends an update message to operators if there's a plugin update available
 */
public class UpdateCheckListener implements Listener {
    @EventHandler
    @SuppressWarnings("unused")
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (AlathraPorts.getInstance().getUpdateChecker().isLatest())
            return;

        if (!Cfg.get().getOrDefault("update-checker.enable", true) || !Cfg.get().getOrDefault("update-checker.op", true))
            return;

        if (!e.getPlayer().isOp())
            return;

        String pluginName = AlathraPorts.getInstance().getUpdateChecker().getPluginName();
        SemanticVersion latestVersion = AlathraPorts.getInstance().getUpdateChecker().getLatestVersion();
        SemanticVersion currentVersion = AlathraPorts.getInstance().getUpdateChecker().getCurrentVersion();

        if (latestVersion == null || currentVersion == null)
            return;

        e.getPlayer().sendMessage(
            ColorParser.of(Translation.of("update-checker.update-found-player"))
                .parseMinimessagePlaceholder("plugin_name", pluginName)
                .parseMinimessagePlaceholder("version_current", currentVersion.getVersionFull())
                .parseMinimessagePlaceholder("version_latest", latestVersion.getVersionFull())
                .parseMinimessagePlaceholder("download_link", UpdateChecker.LATEST_RELEASE)
                .build()
        );
    }
}
