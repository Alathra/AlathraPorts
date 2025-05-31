package io.github.alathra.alathraports;

/**
 * Implemented in classes that should support being reloaded IE executing the methods during runtime after startup.
 */
public interface Reloadable {
    /**
     * On plugin load.
     */
    void onLoad(AlathraPorts plugin);

    /**
     * On plugin enable.
     */
    void onEnable(AlathraPorts plugin);

    /**
     * On plugin disable.
     */
    void onDisable(AlathraPorts plugin);
}
