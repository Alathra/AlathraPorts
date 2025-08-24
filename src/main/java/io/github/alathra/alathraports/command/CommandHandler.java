package io.github.alathra.alathraports.command;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import io.github.alathra.alathraports.AlathraPorts;
import io.github.alathra.alathraports.Reloadable;

/**
 * A class to handle registration of commands.
 */
public class CommandHandler implements Reloadable {
    private final AlathraPorts plugin;

    /**
     * Instantiates the Command handler.
     *
     * @param plugin the plugin
     */
    public CommandHandler(AlathraPorts plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onLoad(AlathraPorts plugin) {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(plugin)
            .shouldHookPaperReload(true)
            .silentLogs(true)
            .beLenientForMinorVersions(true)
        );
    }

    @Override
    public void onEnable(AlathraPorts plugin) {
        CommandAPI.onEnable();

        // Register commands here
        new PortsCommand();
    }

    @Override
    public void onDisable(AlathraPorts plugin) {
        CommandAPI.onDisable();
    }
}