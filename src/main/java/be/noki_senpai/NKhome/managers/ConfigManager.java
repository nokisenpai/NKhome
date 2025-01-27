package be.noki_senpai.NKhome.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import be.noki_senpai.NKhome.NKhome;

public class ConfigManager
{
	private ConsoleCommandSender console = null;
	private FileConfiguration config = null;

	private String dbHost = null;
	private int dbPort = 3306;
	private String dbName = null;
	private String dbUser = null;
	private String dbPassword = null;

	public static String PREFIX = null;
	public static String SERVERNAME = null;

	private Map<String, Integer> ranks = new TreeMap<String, Integer>(String.CASE_INSENSITIVE_ORDER);
	private Map<String, String> convertGroup = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
	private List<String> convertIgnore = new ArrayList<String>();

	// Constructor
	public ConfigManager(FileConfiguration config)
	{
		this.console = Bukkit.getConsoleSender();
		this.config = config;
	}

	public boolean loadConfig()
	{
		// Check if "use-mysql" is to true. Plugin only use MySQL database.
		if(!config.getBoolean("use-mysql"))
		{
			console.sendMessage(ChatColor.DARK_RED + NKhome.PNAME
					+ " Disabled because this plugin only use MySQL database. Please set to true the 'use-mysql' field in config.yml");
			return false;
		}

		// Get database access informations
		dbHost = config.getString("host");
		dbPort = config.getInt("port");
		dbName = config.getString("dbName");
		dbUser = config.getString("user");
		dbPassword = config.getString("password");

		// Get prefix used for table name on database
		PREFIX = config.getString("table-prefix", "NKhome_");

		// Get server name gave to bungeecord config
		SERVERNAME = config.getString("server-name", "world");

		// Get max home per ranks
		ConfigurationSection rankSection = config.getConfigurationSection("ranks");

		for(String key : rankSection.getKeys(false))
		{
			ranks.put(key, config.getInt("ranks." + key));
		}

		if(ranks.size() == 0)
		{
			console.sendMessage(ChatColor.DARK_RED + NKhome.PNAME + " No rank found in config.yml.");
		}

		// Get servers structure for conversion from Essentials
		ConfigurationSection convertGroupSection = config.getConfigurationSection("convert-group");

		for(String server : convertGroupSection.getKeys(false))
		{
			for(String world : config.getStringList("convert-group." + server))
			{
				convertGroup.put(world, server);
			}
		}

		// Get ignored worlds for conversion from Essentials
		convertIgnore = config.getStringList("convert-ignore");

		return true;
	}

	// ######################################
	// Getters (only)
	// ######################################

	public String getDbHost()
	{
		return dbHost;
	}

	public int getDbPort()
	{
		return dbPort;
	}

	public String getDbName()
	{
		return dbName;
	}

	public String getDbUser()
	{
		return dbUser;
	}

	public String getDbPassword()
	{
		return dbPassword;
	}

	public Map<String, Integer> getRanks()
	{
		return ranks;
	}

	public Map<String, String> getConvertGroup()
	{
		return convertGroup;
	}

	public List<String> getConvertIgnore()
	{
		return convertIgnore;
	}
}
