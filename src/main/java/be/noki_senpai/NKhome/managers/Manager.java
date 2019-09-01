package be.noki_senpai.NKhome.managers;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

import be.noki_senpai.NKhome.NKhome;

import java.util.Queue;

public class Manager
{
	private ConsoleCommandSender console = null;
	private ConfigManager configManager = null;
	private DatabaseManager databaseManager = null;
	private HomeManager homeManager = null;
	private QueueManager queueManager = null;

	public Manager(NKhome instance)
	{
		console = Bukkit.getConsoleSender();
		configManager = new ConfigManager(instance.getConfig());
		databaseManager = new DatabaseManager(configManager);
		homeManager = new HomeManager();
		queueManager = new QueueManager();
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

	// QueueManager
	public QueueManager getQueueManager()
	{
		return queueManager;
	}
}
