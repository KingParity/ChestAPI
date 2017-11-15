package de.pepich.chestapi;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public interface CallbackHandler
{
	public void run(Player player, ClickType type);
}
