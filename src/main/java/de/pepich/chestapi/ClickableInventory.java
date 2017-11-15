package de.pepich.chestapi;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class ClickableInventory implements Listener
{
	private int open = 0;
	private final String title;
	private DefaultSize size;
	private HashMap<Integer, CallbackHandler> handlers = new HashMap<Integer, CallbackHandler>();
	private CallbackHandler background;
	private CallbackHandler fallback;
	private CallbackHandler close;
	
	private Inventory inventory;
	private final InventoryHolder holder;
	
	/** Creates a new ClickableInventory with the given name.</br>
	 * The ClickableInventory will be created with a DefaultSize as specified by {@link DefaultSize#DYNAMIC_AUTO(int, int) DYNAMIC_AUTO(9, 54)}.
	 * 
	 * @param title The name to be displayed. */
	public ClickableInventory(final String title)
	{
		this(title, DefaultSize.DYNAMIC_AUTO(9, 54));
	}
	
	/** Creates a new ClickableInventory with the given name and the specified DefaultSize.
	 * 
	 * @param title The name to be displayed.
	 * @param size The DefaultSize for the inventory to be used. */
	public ClickableInventory(final String title, final DefaultSize size)
	{
		this.size = size;
		this.title = title;
		holder = new CustomHolder(this);
		if (size.allowSquareShape() && size.getPreferredSize() == 9)
			inventory = Bukkit.createInventory(holder, InventoryType.DROPPER, title);
		else
			inventory = Bukkit.createInventory(holder, size.getPreferredSize(), title);
	}
	
	/** Displays the inventory.
	 * 
	 * @param player The player that is going to see the inventory. */
	public void show(Player player)
	{
		player.openInventory(inventory);
		if (open == 0)
			Bukkit.getPluginManager().registerEvents(this, ChestAPI.getPlugin());
		open++;
	}
	
	// ------------------------------------------------------------- //
	// -------------------- GETTERS AND SETTERS -------------------- //
	// ------------------------------------------------------------- //
	// If you want docs on these, go fork off. Getters/Setters. Duh. //
	
	public String getName()
	{
		return title;
	}
	
	public int getFirstFree()
	{
		if (handlers.size() == inventory.getSize())
			return -1;
		for (int i = 1; i <= inventory.getSize(); i++)
			if (!handlers.containsKey(i))
				return i;
		return -1;
	}
	
	public int getLastFree()
	{
		if (handlers.size() == inventory.getSize())
			return -1;
		for (int i = inventory.getSize(); i > 0; i--)
			if (!handlers.containsKey(i))
				return i;
		return -1;
	}
	
	public int getFirstUsed()
	{
		if (handlers.size() == inventory.getSize())
			return -1;
		for (int i = 1; i <= inventory.getSize(); i++)
			if (handlers.containsKey(i))
				return i;
		return -1;
	}
	
	public int getLastUsed()
	{
		if (handlers.size() == inventory.getSize())
			return -1;
		for (int i = inventory.getSize(); i > 0; i--)
			if (handlers.containsKey(i))
				return i;
		return -1;
	}
	
	protected Inventory getInventory()
	{
		return inventory;
	}
	
	// ------------------------------------------------------------- //
	// ----------------------- SETUP METHODS ----------------------- //
	// ------------------------------------------------------------- //
	
	/** Assigns an item and a handler to the given item slot.<br/>
	 * Setting a handler to null will NOT remove it, but it will flag the slot as "used", meaning that it will be skipped when searching for the first/last free spot.
	 * 
	 * @param slot An integer value representing the slot for the ItemStack. Must be any of -998, [-3:max_length].<br/>
	 *        The value -998 represents background clicks.<br/>
	 *        The value -3 represents the "close handler", which will be fired when the player closes the inventory.<br/>
	 *        The value -2 represents the "fallback handler", which will be used if no appropriate handler could be found.<br/>
	 *        The value -1 represents the last free slot, 0 points to the first free slot. If an actual number is given, the specified slot will be overwritten.<br/>
	 *        The first slot is numbered with 1, as opposed to starting at 0, due to the additional meaning that 0 has.
	 * @param item An ItemStack that will be displayed. Set to null if you want to assign functionality to an empty slot.
	 * @param handler A CallbackHandler which will be notified on click. When the handler is null then the handler will be removed, but the item will still be assigned.
	 * @throws IllegalArgumentException when the location does not exist or when one of -1 or 0 are specified and the inventory is already full. */
	public void set(int slot, final ItemStack item, final CallbackHandler handler)
	{
		if (slot > inventory.getSize())
			resize(slot);
		if (slot < -1 || slot > size.getMaxSize())
			throw new IllegalArgumentException(
					"The given slot (" + slot + ") is out of bounds (-1, 0, [1:" + size.getMaxSize() + "])");
		else
		{
			if (slot == -998)
				background = handler;
			else if (slot == -2)
				fallback = handler;
			else if (slot == -3)
				close = handler;
			else
			{
				if (slot == 0)
					slot = getFirstFree();
				else if (slot == -1)
					slot = getLastFree();
				if (slot == -1)
					throw new IllegalArgumentException("No free spot could be found!");
					
				if (item == null)
					inventory.remove(inventory.getItem(slot - 1));
				else
					inventory.setItem(slot - 1, item);
					
				handlers.put(slot, handler);
			}
		}
		if (size.doAutoResize())
			resize();
	}
	
	/** Removes an assigned action from the inventory. Most be any of -1, 0, [1:max_length].<br/>
	 * Calling this method will free up a slot again so that it can be filled up by using the "first/last free" selector.
	 * 
	 * @param slot An integer value representing the slot for the ItemStack. Must be any of -998, [-3:max_length].<br/>
	 *        The value -998 represents background clicks.<br/>
	 *        The value -3 represents the "close handler", which will be fired when the player closes the inventory.<br/>
	 *        The value -2 represents the "fallback handler", which will be used if no appropriate handler could be found.<br/>
	 *        The value -1 represents the last used slot, 0 points to the first used slot. If an actual number is given, the specified slot will be overwritten.<br/>
	 *        The first slot is numbered with 1, as opposed to starting at 0, due to the additional meaning that 0 has.
	 * @throws IllegalArgumentException when the location does not exist. */
	public void remove(int slot)
	{
		if (slot == -998)
			background = null;
		else if (slot == -2)
			fallback = null;
		else if (slot == -3)
			close = null;
		else
		{
			if (slot < -1 || slot > size.getMaxSize())
				throw new IllegalArgumentException(
						"The given slot (" + slot + ") is out of bounds (-1, 0, [1:" + size.getMaxSize() + "])");
			if (slot == 0)
				slot = getFirstUsed();
			else if (slot == -1)
				slot = getLastUsed();
			if (slot == -1)
				throw new IllegalArgumentException("No free spot could be found!");
			handlers.remove(slot);
			inventory.setItem(slot - 1, null);
			if (size.doAutoResize())
				resize();
		}
	}
	
	/** This method removes all TRAILING empty lines.<br/>
	 * This method will utilize hopper and dropper inventories if possible. Dropper inventories can be disabled by selection a RECTANGLE size constraint.<br/>
	 * Can not be used on final sized inventories.
	 * 
	 * @return the new size.
	 * @throws IllegalAccessError if the Inventories size is final. */
	public int resize()
	{
		if (size.isFinalSize())
			throw new IllegalAccessError("Can not resize an inventory with a FINAL size constraint.");
		final int target_size = getLastUsed();
		Inventory new_inventory;
		if (target_size <= 5)
			new_inventory = Bukkit.createInventory(holder, InventoryType.HOPPER, title);
		else if (target_size <= 9 && size.allowSquareShape())
			new_inventory = Bukkit.createInventory(holder, InventoryType.DROPPER, title);
		else
			new_inventory = Bukkit.createInventory(holder, target_size + ((9 - (target_size % 9)) % 9), title);
		for (int i = 0; i < new_inventory.getSize() && i < inventory.getSize(); i++)
			new_inventory.setItem(i, inventory.getItem(i));
		inventory = new_inventory;
		return inventory.getSize();
	}
	
	/** This method removes all TRAILING empty lines.<br/>
	 * This method will utilize hopper and dropper inventories if possible. Dropper inventories can be disabled by selection a RECTANGLE size constraint.<br/>
	 * Can not be used on final sized inventories.
	 * 
	 * @return the new size.
	 * @throws IllegalAccessError if the Inventories size is final. */
	private void resize(int target_size)
	{
		if (size.isFinalSize())
			throw new IllegalAccessError("Can not resize an inventory with a FINAL size constraint.");
		target_size = Math.max(target_size, getLastUsed());
		Inventory new_inventory;
		if (target_size <= 5)
			new_inventory = Bukkit.createInventory(holder, InventoryType.HOPPER, title);
		else if (target_size <= 9 && size.allowSquareShape())
			new_inventory = Bukkit.createInventory(holder, InventoryType.DROPPER, title);
		else
			new_inventory = Bukkit.createInventory(holder, target_size + ((9 - (target_size % 9)) % 9), title);
		for (int i = 0; i < new_inventory.getSize() && i < inventory.getSize(); i++)
			new_inventory.setItem(i, inventory.getItem(i));
		inventory = new_inventory;
	}
	
	// ------------------------------------------------------------- //
	// ------------------------- LISTENERS ------------------------- //
	// ------------------------------------------------------------- //
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event)
	{
		if (this.inventory.equals(event.getInventory()))
		{
			open--;
			if (open == 0)
				event.getHandlers().unregister(this);
			if (close != null)
				close.run((Player) event.getPlayer(), ClickType.UNKNOWN);
		}
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event)
	{
		if (this.inventory.equals(event.getInventory()))
		{
			event.setCancelled(true);
			final int slot = event.getSlot() + 1;
			CallbackHandler handler;
			if (slot == -998)
				handler = background;
			else
				handler = handlers.get(slot);
			if ((handler = (handler == null ? fallback : handler)) != null)
				handler.run((Player) event.getWhoClicked(), event.getClick());
		}
	}
}

class CustomHolder implements InventoryHolder
{
	ClickableInventory inv;
	
	protected CustomHolder(ClickableInventory inv)
	{
		this.inv = inv;
	}
	
	@Override
	public Inventory getInventory()
	{
		return inv.getInventory();
	}
}
