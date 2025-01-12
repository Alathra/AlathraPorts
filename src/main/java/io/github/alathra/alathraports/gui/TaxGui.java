package io.github.alathra.alathraports.gui;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.github.milkdrinkers.colorparser.ColorParser;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import io.github.alathra.alathraports.core.TravelNode;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class TaxGui {
    public static Gui generateBase(TravelNode travelNode) {
        Gui base;
        // Set build settings
        base = Gui.gui()
            .title(ColorParser.of("<dark_purple>" + travelNode.getName() + " - Tax Menu").build())
            .rows(1)
            .disableItemPlace()
            .disableItemSwap()
            .disableItemDrop()
            .disableItemTake()
            .create();
        return base;
    }

    public static void generateCurrentTaxButton(Gui gui, TravelNode node) {
        ItemStack currentTaxItem = new ItemStack(Material.EMERALD);
        ItemMeta currentTaxMeta = currentTaxItem.getItemMeta();
        String townFeePercent = (int) (node.getTownFee() * 100) + "%";
        currentTaxMeta.displayName(ColorParser.of("<green>Current Rate: " + townFeePercent).build().decoration(TextDecoration.ITALIC, false));
        currentTaxItem.setItemMeta(currentTaxMeta);
        gui.setItem(1, 1, ItemBuilder.from(currentTaxItem).asGuiItem());
    }

    public static void generateBackButton(Gui gui, Player player, TravelNode node) {
        ItemStack backItem = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.displayName(ColorParser.of("<red>Back").build().decoration(TextDecoration.ITALIC, false));
        backItem.setItemMeta(backMeta);
        gui.setItem(1, 9, ItemBuilder.from(backItem).asGuiItem(event -> {
            GuiHandler.generateTravelGui(player, node);
        }));
    }

    public static void generateRaiseButton(Gui gui, Player player, TravelNode node) {
        ItemStack raiseButton = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta raiseButtonMeta = (SkullMeta) raiseButton.getItemMeta();
        raiseButtonMeta.displayName(ColorParser.of("<gold>Raise 1%").build().decoration(TextDecoration.ITALIC, false));
        final UUID uuid = UUID.randomUUID();
        final PlayerProfile playerProfile = Bukkit.createProfile(uuid, uuid.toString().substring(0, 16));
        final String upArrowTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzA0MGZlODM2YTZjMmZiZDJjN2E5YzhlYzZiZTUxNzRmZGRmMWFjMjBmNTVlMzY2MTU2ZmE1ZjcxMmUxMCJ9fX0=";
        playerProfile.setProperty(new ProfileProperty("textures", upArrowTexture));
        raiseButtonMeta.setPlayerProfile(playerProfile);
        raiseButton.setItemMeta(raiseButtonMeta);
        gui.setItem(1, 2, ItemBuilder.from(raiseButton).asGuiItem(event -> {
            // Checks for maxing out
            node.setTownFee(node.getTownFee() + 0.01);
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_CHIME, 1, 0.667420f);
            generateCurrentTaxButton(gui, node);
            gui.update();
        }));
    }

    public static void generateLowerButton(Gui gui, Player player, TravelNode node) {
        ItemStack lowerButton = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta lowerButtonMeta = (SkullMeta) lowerButton.getItemMeta();
        lowerButtonMeta.displayName(ColorParser.of("<gold>Lower 1%").build().decoration(TextDecoration.ITALIC, false));
        final UUID uuid = UUID.randomUUID();
        final PlayerProfile playerProfile = Bukkit.createProfile(uuid, uuid.toString().substring(0, 16));
        final String upArrowTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzQzNzM0NmQ4YmRhNzhkNTI1ZDE5ZjU0MGE5NWU0ZTc5ZGFlZGE3OTVjYmM1YTEzMjU2MjM2MzEyY2YifX19";
        playerProfile.setProperty(new ProfileProperty("textures", upArrowTexture));
        lowerButtonMeta.setPlayerProfile(playerProfile);
        lowerButton.setItemMeta(lowerButtonMeta);
        gui.setItem(1, 3, ItemBuilder.from(lowerButton).asGuiItem(event -> {
            // Checks for maxing out
            node.setTownFee(node.getTownFee() - 0.01);
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_CHIME, 1, 0.5f);
            generateCurrentTaxButton(gui, node);
            gui.update();
        }));
    }
}
