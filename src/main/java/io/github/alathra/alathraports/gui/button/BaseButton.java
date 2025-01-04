package io.github.alathra.alathraports.gui.button;

import com.github.milkdrinkers.colorparser.ColorParser;
import io.github.alathra.alathraports.gui.tasks.base.BaseTask;
import io.github.alathra.alathraports.gui.tasks.MessageTask;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;

/**
 * Represents a button in the Inventory Menu
 * Contains the ItemStack, BaseTask and Any other metadata.
 *
 * @author NinjaMandalorian
 */
public class BaseButton {

	private ItemStack itemStack;
	private BaseTask task;
	private HashMap<String, String> metadata;

	public static BaseButton create() {
		BaseButton button = new BaseButton();
		button.itemStack = new ItemStack(Material.GLASS);
        button.name(ColorParser.of("<gray>Empty").build());
		button.task = new MessageTask("Task-Unassigned");
		return button;
	}

	public static BaseButton create(Material material) {
		BaseButton button = create();
		button.itemStack = new ItemStack(material);
		return button;
	}

	/**
	 * Creates a blank button, ideal for empty-areas in a menu.
	 *
	 * @return Background button
	 */
	public static BaseButton background() {
		BaseButton button = new BaseButton();
		button.itemStack = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
		button = button.name(ColorParser.of("").build());
		return button;
	}

	/**
	 * Gets the button's task
	 *
     */
	public BaseTask getTask() {
		return this.task;
	}

	// Constructors for building

	/**
	 * Gets the metadata of the button
	 *
     */
	public HashMap<String, String> getMetadata() {
		return this.metadata;
	}

	/**
	 * Sets the metadata of the button
	 *
	 */
	public void setMetadata(HashMap<String, String> map) {
		this.metadata = map;
	}

	/**
	 * Gets the item stack of button
	 *
     */
	public ItemStack getItemStack() {
		return this.itemStack;
	}

	/**
	 * Makes the Button glow
	 *
     */
	public BaseButton glow() {
		this.itemStack.addUnsafeEnchantment(Enchantment.LUCK_OF_THE_SEA, 1);
		ItemMeta meta = this.itemStack.getItemMeta();
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		this.itemStack.setItemMeta(meta);
		return this;
	}

	/**
	 * Sets the itemstack of the button
	 *
	 */
	public BaseButton itemStack(ItemStack itemStack) {
		this.itemStack = itemStack;
		return this;
	}

	/**
	 * Sets the task
	 *
	 */
	public BaseButton task(BaseTask task) {
		this.task = task;
		return this;
	}

	/**
	 * Sets the quantity of the button's stack
	 *
	 */
	public BaseButton quantity(int num) {
		this.itemStack.setAmount(num);
		return this;
	}

	/**
	 * Renames the button
	 *
	 */
	public BaseButton name(Component name) {
		ItemMeta meta = this.itemStack.getItemMeta();
		meta.displayName(name);
		this.itemStack.setItemMeta(meta);
		return this;
	}

	/**
	 * Sets the button's lore
	 *
	 */
	public BaseButton lore(List<Component> lore) {
		ItemMeta meta = this.itemStack.getItemMeta();
		meta.lore(lore);
		this.itemStack.setItemMeta(meta);
		return this;
	}

	/**
	 * Runs the button task.
	 *
	 */
	public BaseButton run(InventoryClickEvent e) {
		if (this.task != null) this.task.run(e);
		return this;
	}

}
