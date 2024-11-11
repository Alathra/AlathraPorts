package io.github.alathra.alathraports.utility;


import io.github.alathra.alathraports.AlathraPorts;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;

/**
 * A class that provides shorthand access to {@link AlathraPorts#getComponentLogger}.
 */
public class Logger {
    /**
     * Get component logger. Shorthand for:
     *
     * @return the component logger {@link AlathraPorts#getComponentLogger}.
     */
    @NotNull
    public static ComponentLogger get() {
        return AlathraPorts.getInstance().getComponentLogger();
    }
}
