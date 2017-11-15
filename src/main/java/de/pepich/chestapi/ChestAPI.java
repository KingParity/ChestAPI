package de.pepich.chestapi;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ChestAPI
{
	private static JavaPlugin plugin = null;
	
	public static void init(JavaPlugin plugin)
	{
		ChestAPI.plugin = plugin;
	}
	
	public static JavaPlugin getPlugin()
	{
		if (plugin == null)
			plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugins()[0];
		return plugin;
	}
}
