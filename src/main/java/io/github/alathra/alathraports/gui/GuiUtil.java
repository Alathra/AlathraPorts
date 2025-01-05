package io.github.alathra.alathraports.gui;

import com.github.milkdrinkers.colorparser.ColorParser;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import io.github.alathra.alathraports.ports.Port;
import io.github.alathra.alathraports.ports.algo.TravelHandler;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class GuiUtil {

    public static PaginatedGui generatePaginatedBase() {
        PaginatedGui base;
        // Set build settings
        base = Gui.paginated()
            .title(ColorParser.of("<blue>Port Menu").build())
            .rows(6)
            .disableItemPlace()
            .disableItemSwap()
            .disableItemDrop()
            .disableItemTake()
            .create();

        // Apply gray glass pane border
        ItemStack grayBorder = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta grayBorderItemMeta = grayBorder.getItemMeta();
        grayBorderItemMeta.displayName(ColorParser.of("").build());
        grayBorder.setItemMeta(grayBorderItemMeta);
        base.getFiller().fillBorder(ItemBuilder.from(grayBorder).asGuiItem());

        // Create page nav buttons
        ItemStack nextPage = new ItemStack(Material.ARROW);
        ItemMeta nextPageMeta = nextPage.getItemMeta();
        nextPageMeta.displayName(ColorParser.of("<yellow>Next Page").build().decoration(TextDecoration.ITALIC, false));
        nextPage.setItemMeta(nextPageMeta);
        base.setItem(6, 6, ItemBuilder.from(nextPage).asGuiItem(event -> {
            base.next();
        }));

        ItemStack prevPage = new ItemStack(Material.ARROW);
        ItemMeta prevPageMeta = prevPage.getItemMeta();
        prevPageMeta.displayName(ColorParser.of("<yellow>Previous Page").build().decoration(TextDecoration.ITALIC, false));
        prevPage.setItemMeta(prevPageMeta);
        base.setItem(6, 4, ItemBuilder.from(prevPage).asGuiItem(event -> {
            base.previous();
        }));

        return base;
    }

    public static void generatePortButtons(PaginatedGui gui, Player player, Port port) {
        for (Port reachablePort : port.getReachable() ) {
            List<Port> journey = TravelHandler.findJourney(port, reachablePort);
            ItemStack portItem = new ItemStack(port.getSize().getIcon());
            ItemMeta portItemMeta = portItem.getItemMeta();
            portItemMeta.displayName(ColorParser.of("<blue><bold>" + port.getName()).build().decoration(TextDecoration.ITALIC, false));
            portItemMeta.displayName();
            portItemMeta.lore(List.of(
               ColorParser.of("<gold>Size: <red>" + reachablePort.getSize().getName()).build().decoration(TextDecoration.ITALIC, false),
                ColorParser.of("<gold>Cost: " + TravelHandler.getJourneyCost(journey)).build().decoration(TextDecoration.ITALIC, false),
                // TODO: CURRENTLY IN TICKS
                ColorParser.of("<gold>Travel Time: " + TravelHandler.getJourneyWait(journey)).build().decoration(TextDecoration.ITALIC, false),
                ColorParser.of("").build(),
                ColorParser.of("<green>Click to Travel").build().decoration(TextDecoration.ITALIC, false)
            ));
            portItem.setItemMeta(portItemMeta);
            gui.addItem(ItemBuilder.from(portItem).asGuiItem(event -> {
                TravelHandler.startJourney(player, port, reachablePort);
                gui.close(player);
            }));
        }
    }

}
