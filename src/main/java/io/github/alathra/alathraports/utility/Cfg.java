package io.github.alathra.alathraports.utility;

import com.github.milkdrinkers.crate.Config;
import io.github.alathra.alathraports.AlathraPorts;
import io.github.alathra.alathraports.config.ConfigHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Convenience class for accessing {@link ConfigHandler#getConfig}
 */
public abstract class Cfg {
    /**
     * Convenience method for {@link ConfigHandler#getConfig} to getConnection {@link Config}
     *
     * @return the config
     */
    @NotNull
    public static Config get() {
        return AlathraPorts.getInstance().getConfigHandler().getConfig();
    }
}
