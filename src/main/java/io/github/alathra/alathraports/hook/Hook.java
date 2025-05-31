package io.github.alathra.alathraports.hook;

import io.github.alathra.alathraports.hook.combatlogx.CombatLogXHook;
import io.github.alathra.alathraports.hook.dynmap.DynmapHook;
import io.github.alathra.alathraports.hook.placeholderapi.PAPIHook;
import io.github.alathra.alathraports.hook.towny.TownyHook;
import io.github.alathra.alathraports.hook.vault.VaultHook;
import io.github.alathra.alathraports.hook.worldguard.WorldguardHook;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum Hook {
    Vault(VaultHook.class, "Vault", true),
    PAPI(PAPIHook.class, "PlaceholderAPI", true),
    Dynmap(DynmapHook.class, "dynmap", true),
    Towny(TownyHook.class, "Towny", true),
    CombatLogX(CombatLogXHook.class, "CombatLogX", true),
    Worldguard(WorldguardHook.class, "WorldGuard", true);

    private final @NotNull Class<? extends AbstractHook> hookClass; // The hook class used by this hook
    private final @Nullable String pluginName; // The plugin name used by this hook or null if not applicable
    private final boolean optional; // Whether this hook is optional or required for the plugin to enable
    private AbstractHook loadedHook; // A pointer to the hook object instantiated by {@link HookManager}

    Hook(@NotNull Class<? extends AbstractHook> hookClass, @Nullable String pluginName, boolean optional) {
        this.hookClass = hookClass;
        this.pluginName = pluginName;
        this.optional = optional;
    }

    /**
     * Get the hook class.
     *
     * @return the hook class
     */
    @NotNull Class<? extends AbstractHook> getHookClass() {
        return hookClass;
    }

    /**
     * Get the plugin name used by this hook. Can be null for hooks that do not use a plugin to provide functionality.
     *
     * @return the plugin name
     */
    public @Nullable String getPluginName() {
        return pluginName;
    }

    /**
     * Check if this hook is required for the plugin to enable.
     *
     * @return whether this hook is required
     */
    public boolean isOptional() {
        return optional;
    }

    /**
     * Get the hook object.
     *
     * @return the hook object
     * @throws IllegalStateException if the hook has not been loaded yet
     * @implNote Cast this {@link AbstractHook} into the correct hook class.
     * @implSpec You should check {@link #isLoaded()} before using this method.
     */
    public AbstractHook get() {
        if (loadedHook == null)
            throw new IllegalStateException("Hook has not been loaded yet.");

        return loadedHook;
    }

    /**
     * Check if the hook is loaded.
     *
     * @return whether the hook is loaded
     * @implNote This check is a guarantee that the hook and its dependencies have loaded. It also checks {@link AbstractHook#isHookLoaded()}.
     */
    public boolean isLoaded() {
        if (loadedHook != null)
            return loadedHook.isHookLoaded();

        return false;
    }

    /**
     * Sets a weak reference to a hook
     *
     * @param hook the hook object
     */
    @ApiStatus.Internal
    void setHook(@Nullable AbstractHook hook) {
        this.loadedHook = hook;
    }

    /**
     * Clear the weak reference to this hook
     */
    @ApiStatus.Internal
    void clearHook() {
        this.loadedHook = null;
    }

    /**
     * Clear the weak references for hooks
     */
    @ApiStatus.Internal
    static void clearHooks() {
        for (Hook hooks : values())
            hooks.clearHook();
    }

    /**
     * Gets vault hook.
     *
     * @return the vault hook
     */
    @NotNull
    public static VaultHook getVaultHook() {
        return (VaultHook) Hook.Vault.get();
    }

    @NotNull
    public static PAPIHook getPAPIHook() {
        return (PAPIHook) Hook.PAPI.get();
    }

    @NotNull
    public static DynmapHook getDynmapHook() {
        return (DynmapHook) Hook.Dynmap.get();
    }

    @NotNull
    public static CombatLogXHook getCombatLogXHook() {
        return (CombatLogXHook) Hook.CombatLogX.get();
    }

    @NotNull
    public static TownyHook getTownyHook() {
        return (TownyHook) Hook.Towny.get();
    }

    @NotNull
    public static WorldguardHook getWorldguardHook() {
        return (WorldguardHook) Hook.Worldguard.get();
    }
}
