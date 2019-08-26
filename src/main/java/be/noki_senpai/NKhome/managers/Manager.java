package be.noki_senpai.NKhome.managers;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

import be.noki_senpai.NKhome.NKhome;

public class Manager
{
	private ConsoleCommandSender console = null;
	private ConfigManager configManager = null;
	private DatabaseManager databaseManager = null;
	private HomeManager homeManager = null;

	public Manager(NKhome instance)
	{
		console = Bukkit.getConsoleSender();
		configManager = new ConfigManager(instance.getConfig());
		databaseManager = new DatabaseManager(configManager);
		homeManager = new HomeManager();
	}

	// ######################################
	// Getters & Setters
	// ######################################

	// Console
	public ConsoleCommandSender getConsole()
	{
		return console;
	}

	// PluginManager
	public ConfigManager getConfigManager()
	{
		return configManager;
	}

	// DatabaseManager
	public DatabaseManager getDatabaseManager()
	{
		return databaseManager;
	}

	// PlayerManager
	public HomeManager getHomeManager()
	{
		return homeManager;
	}
}
