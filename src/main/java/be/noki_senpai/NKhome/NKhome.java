package be.noki_senpai.NKhome;

import be.noki_senpai.NKhome.cmd.*;
import be.noki_senpai.NKhome.listeners.HomeCompleter;
import be.noki_senpai.NKhome.listeners.PlayerBedEnter;
import be.noki_senpai.NKhome.listeners.PlayerConnectionListener;
import be.noki_senpai.NKhome.managers.Manager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class NKhome extends JavaPlugin
{
	public final static String PNAME = "[NKhome]";
	private Manager manager = null;
	private ConsoleCommandSender console = null;
	private static NKhome plugin = null;

	// Fired when plugin is first enabled
	@Override public void onEnable()
	{
		plugin = this;
		System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "WARN");
		this.saveDefaultConfig();

		console = Bukkit.getConsoleSender();
		manager = new Manager(this);

		if(!checkNKmanager())
		{
			console.sendMessage(ChatColor.DARK_RED + PNAME + " NKmanager in not enabled !");
			disablePlugin();
			return;
		}

		// Load configuration
		if(!manager.getConfigManager().loadConfig())
		{
			disablePlugin();
			return;
		}

		// Load database connection (with check)
		if(!manager.getDatabaseManager().loadDatabase())
		{
			disablePlugin();
			return;
		}

		// Load homes for online players
		manager.getHomeManager().loadHome();

		// Register listeners
		getServer().getPluginManager().registerEvents(new PlayerConnectionListener(manager.getHomeManager(), manager.getQueueManager()), this);
		getServer().getPluginManager().registerEvents(new PlayerBedEnter(manager.getHomeManager(), manager.getQueueManager()), this);

		// Set tabulation completers
		getCommand("home").setTabCompleter(new HomeCompleter(manager.getHomeManager()));
		getCommand("delhome").setTabCompleter(new HomeCompleter(manager.getHomeManager()));

		// Register commands
		getCommand("home").setExecutor(new HomeCmd(manager.getHomeManager(), manager.getQueueManager()));
		getCommand("sethome").setExecutor(new SetHomeCmd(manager.getHomeManager(), manager.getConfigManager(), manager.getQueueManager()));
		getCommand("delhome").setExecutor(new DelHomeCmd(manager.getHomeManager(), manager.getQueueManager()));
		getCommand("homes").setExecutor(new HomesCmd(manager.getHomeManager(), manager.getConfigManager(), manager.getQueueManager()));
		getCommand("convertessentialshomes").setExecutor(new ConvertEssentialsHomesCmd(manager.getConfigManager()));

		// Subscribe to BungeeCord channel
		getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

		console.sendMessage(ChatColor.WHITE + "     .--. ");
		console.sendMessage(ChatColor.WHITE + "     |   '.   " + ChatColor.GREEN + PNAME + " by NoKi_senpai - successfully enabled !");
		console.sendMessage(ChatColor.WHITE + "'-..____.-'");
	}

	// Fired when plugin is disabled
	@Override public void onDisable()
	{
		manager.getDatabaseManager().unloadDatabase();
		manager.getHomeManager().unloadHome();
		console.sendMessage(ChatColor.GREEN + PNAME + " has been disable.");
	}

	// ######################################
	// Getters & Setters
	// ######################################

	// Getter 'plugin'
	public static NKhome getPlugin()
	{
		return plugin;
	}

	// ######################################
	// Disable this plugin
	// ######################################

	public void disablePlugin()
	{
		getServer().getPluginManager().disablePlugin(this);
	}

	// ######################################
	// Check if NKmanager is enabled
	// ######################################

	public boolean checkNKmanager()
	{
		return getServer().getPluginManager().getPlugin("NKmanager").isEnabled();
	}
}
