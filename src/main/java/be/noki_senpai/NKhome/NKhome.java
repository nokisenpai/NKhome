package be.noki_senpai.NKhome;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import be.noki_senpai.NKhome.cmd.DelHomeCmd;
import be.noki_senpai.NKhome.cmd.HomeCmd;
import be.noki_senpai.NKhome.cmd.HomesCmd;
import be.noki_senpai.NKhome.cmd.SetHomeCmd;
import be.noki_senpai.NKhome.data.NKPlayer;
import be.noki_senpai.NKhome.listeners.HomeCompleter;
import be.noki_senpai.NKhome.listeners.PlayerConnectionListener;
import be.noki_senpai.NKhome.utils.SQLConnect;


public class NKhome extends JavaPlugin
{
	public final static String PName = "[NKhome]";
	public static String prefix = "NKhome_";
	public static Map<String, String> table = new HashMap<>();
	
	// Options
	public static String serverName = "world";
	public static Map<String, Integer> ranks = new TreeMap<String, Integer>(String.CASE_INSENSITIVE_ORDER);
	
	// Players datas
	public static Map<String, NKPlayer> players = new TreeMap<String, NKPlayer>(String.CASE_INSENSITIVE_ORDER);
	
	private static NKhome instance;
	private static Connection bdd = null;
	private ConsoleCommandSender console = getServer().getConsoleSender();
	
	
	// Fired when plugin is first enabled
	@Override
    public void onEnable() 
	{
		instance = this;
			
		this.saveDefaultConfig();
		
		if(this.getConfig().getBoolean("use-mysql"))
		{
			serverName = this.getConfig().getString("server-name");
			
			ConfigurationSection sec = this.getConfig().getConfigurationSection("ranks");
	        for(String key : sec.getKeys(false))
	        {
	            ranks.putIfAbsent(key, this.getConfig().getInt("ranks." + key));
	            console.sendMessage(ChatColor.BLUE + PName + " Just a test : " + key + " => " + this.getConfig().getInt("ranks." + key));
	        }
	        getServer().getPluginManager().disablePlugin(this);
	        if(ranks.size() == 0)
	        {
	        	console.sendMessage(ChatColor.DARK_RED + PName + " No rank found in config.yml.");
				getServer().getPluginManager().disablePlugin(this);
	        }
			
			
			// Save table name
			table.put("homes", prefix + "homes");
			table.put("home_bonus", prefix + "home_bonus");
			table.put("players", "NK_players");

			// Setting database informations		
			SQLConnect.setInfo(this.getConfig().getString("host"), this.getConfig().getInt("port"), this.getConfig().getString("dbName"), this.getConfig().getString("user"), this.getConfig().getString("password"));
			try
			{
				bdd = SQLConnect.getHikariDS().getConnection();
			} 
			catch (SQLException e1)
			{
				bdd = null;
				console.sendMessage(ChatColor.DARK_RED + PName + " Error while attempting database connexion. Verify your access informations in config.yml");
				getServer().getPluginManager().disablePlugin(this);
				e1.printStackTrace();
			}
			
			try 
			{
				// Creating database structure if not exist
				Storage.createTable(this.getConfig().getString("dbName"), prefix, table);
			} 
			catch (SQLException e) 
			{
				console.sendMessage(ChatColor.DARK_RED + PName + " Error while creating database structure. (Error#A.2.002)");
				getServer().getPluginManager().disablePlugin(this);
			}
			
			// On command
			getServer().getPluginManager().registerEvents(new PlayerConnectionListener(), this);
			this.getCommand("home").setTabCompleter(new HomeCompleter());
			this.getCommand("delhome").setTabCompleter(new HomeCompleter());
			getCommand("home").setExecutor(new HomeCmd());
			getCommand("sethome").setExecutor(new SetHomeCmd());
			getCommand("delhome").setExecutor(new DelHomeCmd());
			getCommand("homes").setExecutor(new HomesCmd());
			
			// Data exchange between servers
			this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
			
		    // Get all connected players
			Bukkit.getOnlinePlayers().forEach(player -> players.putIfAbsent(player.getDisplayName(), new NKPlayer(player.getUniqueId())));
			
			console.sendMessage(ChatColor.WHITE + "      .--. ");
			console.sendMessage(ChatColor.WHITE + "      |   '.   " + ChatColor.GREEN + PName + " by NoKi_senpai - successfully enabled !");
			console.sendMessage(ChatColor.WHITE + "'-..____.-'");
		}
		else
		{
			console.sendMessage(ChatColor.DARK_RED + PName + " Disabled because this plugin only use MySQL database. Please set to true the 'use-mysql' field in config.yml");
			getServer().getPluginManager().disablePlugin(this);
		}
		
    }
	
	
    // Fired when plugin is disabled
    @Override
    public void onDisable() 
    {
    	if(bdd != null)
    	{
    		players.clear();
        	try
    		{
    			bdd.close();
    		} 
        	catch (SQLException e)
    		{
    			e.printStackTrace();
    		}
    	}
    	console.sendMessage(ChatColor.GREEN + PName + " has been disable.");
    }
    

    
	//######################################
	// Getters & Setters
	//######################################
    
    // Getter 'instance'
	public static NKhome getInstance()
	{
		return instance;
	}
	
	// Getter 'bdd'
	public Connection getConnection()
	{
		try
		{
			if(bdd.isClosed())
			{
				bdd = SQLConnect.getHikariDS().getConnection();
			}
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return bdd;
	}
		
	// Getter & setter 'console'
	public ConsoleCommandSender getConsole()
	{
		return console;
	}
	public void setConsole(ConsoleCommandSender console)
	{
		this.console = console;
	}
	
	
	
	//######################################
	// Disable this plugin
	//######################################
	
	public void disablePlugin()
	{
		getServer().getPluginManager().disablePlugin(this);
	}
	
	
	
	//######################################
	// Homes functions
	//######################################
	
	
	
	//######################################
	// Utils functions
	//######################################
	
	
	
}
