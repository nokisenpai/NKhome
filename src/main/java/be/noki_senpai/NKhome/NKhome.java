package be.noki_senpai.NKhome;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import be.noki_senpai.NKhome.cmd.ConvertEssentialsHomesCmd;
import be.noki_senpai.NKhome.cmd.DelHomeCmd;
import be.noki_senpai.NKhome.cmd.HomeCmd;
import be.noki_senpai.NKhome.cmd.HomesCmd;
import be.noki_senpai.NKhome.cmd.SetHomeCmd;
import be.noki_senpai.NKhome.data.Home;
import be.noki_senpai.NKhome.data.NKPlayer;
import be.noki_senpai.NKhome.listeners.HomeCompleter;
import be.noki_senpai.NKhome.listeners.PlayerBedEnter;
import be.noki_senpai.NKhome.listeners.PlayerConnectionListener;
import be.noki_senpai.NKhome.utils.CoordTask;
import be.noki_senpai.NKhome.utils.SQLConnect;


public class NKhome extends JavaPlugin
{
	public final static String PName = "[NKhome]";
	public static String prefix = "NKhome_";
	public static Map<String, String> table = new HashMap<>();
	
	// Options
	public static String serverName = "world";
	public static Map<String, Integer> ranks = new TreeMap<String, Integer>(String.CASE_INSENSITIVE_ORDER);
	public static Map<String, String> convertGroup = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
	public static List<String> convertIgnore = new ArrayList<String>();
	
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
			prefix = this.getConfig().getString("table-prefix");
			serverName = this.getConfig().getString("server-name");
			
			// Get max home per ranks
			ConfigurationSection sec = this.getConfig().getConfigurationSection("ranks");
			for(String key : sec.getKeys(false))
			{
				ranks.putIfAbsent(key, this.getConfig().getInt("ranks." + key));
			}

			if(ranks.size() == 0)
			{
				console.sendMessage(ChatColor.DARK_RED + PName + " No rank found in config.yml.");
				getServer().getPluginManager().disablePlugin(this);
				return;
			}
			
			// Get servers structure for conversion from Essentials
			ConfigurationSection sec2 = this.getConfig().getConfigurationSection("convert-group");
			for(String server : sec2.getKeys(false))
			{
				for(String world : this.getConfig().getStringList("convert-group." + server))
				{
					convertGroup.putIfAbsent(world, server);
				}
			}
			
			// Get ignored worlds for conversion from Essentials
			convertIgnore = this.getConfig().getStringList("convert-ignore");
			
			// Save table name
			table.put("homes", prefix + "homes");
			table.put("players_datas", prefix + "players_datas");
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
				return;
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
				return;
			}
			
			// On command
			getServer().getPluginManager().registerEvents(new PlayerConnectionListener(), this);
			getServer().getPluginManager().registerEvents(new PlayerBedEnter(), this);
			this.getCommand("home").setTabCompleter(new HomeCompleter());
			this.getCommand("delhome").setTabCompleter(new HomeCompleter());
			getCommand("home").setExecutor(new HomeCmd());
			getCommand("sethome").setExecutor(new SetHomeCmd());
			getCommand("delhome").setExecutor(new DelHomeCmd());
			getCommand("homes").setExecutor(new HomesCmd());
			getCommand("convertessentialshomes").setExecutor(new ConvertEssentialsHomesCmd());
			
			// Data exchange between servers
			this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
			
			// Get all connected players
			Bukkit.getOnlinePlayers().forEach(player -> players.putIfAbsent(player.getDisplayName(), new NKPlayer(player.getUniqueId())));
			
			console.sendMessage(ChatColor.WHITE + "	  .--. ");
			console.sendMessage(ChatColor.WHITE + "	  |   '.   " + ChatColor.GREEN + PName + " by NoKi_senpai - successfully enabled !");
			console.sendMessage(ChatColor.WHITE + "'-..____.-'");
		}
		else
		{
			console.sendMessage(ChatColor.DARK_RED + PName + " Disabled because this plugin only use MySQL database. Please set to true the 'use-mysql' field in config.yml");
			getServer().getPluginManager().disablePlugin(this);
			return;
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
			if(!bdd.isValid(1))
			{
				if(!bdd.isClosed())
				{
					bdd.close();
				}
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
	
	public static void updateHome(String playerName, String homeName, Location location)
	{
		new BukkitRunnable() 
		{
			@Override
			public void run() 
			{
				Connection bdd = null;
				PreparedStatement ps = null;
				String req = null;
				
				try
				{
					
					
					
					if(homeName.equals("bed"))
					{
						bdd = NKhome.getInstance().getConnection();
						
						req = "UPDATE " + NKhome.table.get("homes") + " SET server = ? , world = ? , x = ? , y = ? , z = ? , pitch = ? , yaw = ? WHERE id = ?";  
						ps = bdd.prepareStatement(req);
						ps.setString(1, NKhome.serverName);
						ps.setString(2, location.getWorld().getName());
						ps.setDouble(3, CoordTask.BedNegateAdjust(location.getX()));
						ps.setDouble(4, location.getY());
						ps.setDouble(5, CoordTask.BedNegateAdjust(location.getZ()));
						ps.setDouble(6, location.getPitch());
						ps.setDouble(7, location.getYaw());
						
						NKhome.players.get(playerName).getBed().setServer(NKhome.serverName);
						NKhome.players.get(playerName).getBed().setWorld(location.getWorld().getName());
						NKhome.players.get(playerName).getBed().setX(CoordTask.BedNegateAdjust(location.getX()));
						NKhome.players.get(playerName).getBed().setY(location.getY());
						NKhome.players.get(playerName).getBed().setZ(CoordTask.BedNegateAdjust(location.getZ()));
						NKhome.players.get(playerName).getBed().setPitch(location.getPitch());
						NKhome.players.get(playerName).getBed().setYaw(location.getYaw());
						
						ps.setInt(7, NKhome.players.get(playerName).getBed().getId());
					}
					else
					{
						bdd = NKhome.getInstance().getConnection();
						
						req = "UPDATE " + NKhome.table.get("homes") + " SET server = ? , world = ? , x = ? , y = ? , z = ? , pitch = ? , yaw = ? WHERE id = ?";  
						ps = bdd.prepareStatement(req);
						ps.setString(1, NKhome.serverName);
						ps.setString(2, location.getWorld().getName());
						ps.setDouble(3, location.getX());
						ps.setDouble(4, location.getY());
						ps.setDouble(5, location.getZ());
						ps.setDouble(6, location.getPitch());
						ps.setDouble(7, location.getYaw());
						
						NKhome.players.get(playerName).getHomes().get(homeName).setServer(NKhome.serverName);
						NKhome.players.get(playerName).getHomes().get(homeName).setWorld(location.getWorld().getName());
						NKhome.players.get(playerName).getHomes().get(homeName).setX(location.getX());
						NKhome.players.get(playerName).getHomes().get(homeName).setY(location.getY());
						NKhome.players.get(playerName).getHomes().get(homeName).setZ(location.getZ());
						NKhome.players.get(playerName).getHomes().get(homeName).setPitch(location.getPitch());
						NKhome.players.get(playerName).getHomes().get(homeName).setYaw(location.getYaw());
						
						ps.setInt(7, NKhome.players.get(playerName).getHomes().get(homeName).getId());
					}
					
					
					
					
					
					ps.executeUpdate();
					ps.close();				
				}
				catch (SQLException e) 
				{
					NKhome.getInstance().getConsole().sendMessage(ChatColor.DARK_RED + NKhome.PName + " Error while setting a home.");
					e.printStackTrace();
				}	
			}
		}.runTaskAsynchronously(NKhome.getInstance());
	}
	
	
	
	public static void setHome(String playerName, String homeName, Location location)
	{
		new BukkitRunnable() 
		{
			@Override
			public void run() 
			{
				Connection bdd = null;
				ResultSet resultat = null;
				PreparedStatement ps = null;
				String req = null;
				
				try
				{
					if(homeName.equals("bed"))
					{
						bdd = NKhome.getInstance().getConnection();
						req = "INSERT INTO " + NKhome.table.get("homes") + " ( player_id, server, name, world, x, y, z, pitch, yaw ) VALUES ( ? , ? , ? , ? , ? , ? , ? , ? , ? )";				
						ps = bdd.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);	   
						ps.setInt(1, NKhome.players.get(playerName).getId());
						ps.setString(2, NKhome.serverName);
						ps.setString(3, homeName);
						ps.setString(4, location.getWorld().getName());
						ps.setDouble(5, CoordTask.BedNegateAdjust(location.getX()));
						ps.setDouble(6, location.getY());
						ps.setDouble(7, CoordTask.BedNegateAdjust(location.getZ()));
						ps.setDouble(8, location.getPitch());
						ps.setDouble(9, location.getYaw());
						
						ps.executeUpdate();  
						resultat = ps.getGeneratedKeys();	
						
						resultat.next();  
						
						NKhome.players.get(playerName).setBed(
								resultat.getInt(1), 
								NKhome.serverName, 
								homeName, 
								location.getWorld().getName(),
								CoordTask.BedNegateAdjust(location.getX()), 
								location.getY(),
								CoordTask.BedNegateAdjust(location.getZ()),
								location.getPitch(),
								location.getYaw());
					}
					else
					{
						bdd = NKhome.getInstance().getConnection();
						req = "INSERT INTO " + NKhome.table.get("homes") + " ( player_id, server, name, world, x, y, z, pitch, yaw ) VALUES ( ? , ? , ? , ? , ? , ? , ? , ? , ? )";				
						ps = bdd.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);	   
						ps.setInt(1, NKhome.players.get(playerName).getId());
						ps.setString(2, NKhome.serverName);
						ps.setString(3, homeName);
						ps.setString(4, location.getWorld().getName());
						ps.setDouble(5, location.getX());
						ps.setDouble(6, location.getY());
						ps.setDouble(7, location.getZ());
						ps.setDouble(8, location.getPitch());
						ps.setDouble(9, location.getYaw());
						
						ps.executeUpdate();  
						resultat = ps.getGeneratedKeys();	
						
						resultat.next();  
						NKhome.players.get(playerName).addHome(
								resultat.getInt(1), 
								NKhome.serverName, 
								homeName, 
								location.getWorld().getName(),
								location.getX(), 
								location.getY(),
								location.getZ(),
								location.getPitch(),
								location.getYaw());
					}
					
					ps.close();
					resultat.close();
				}
				catch (SQLException e) 
				{
					NKhome.getInstance().getConsole().sendMessage(ChatColor.DARK_RED + NKhome.PName + " Error while setting a home.");
					e.printStackTrace();
				}	
			}
		}.runTaskAsynchronously(NKhome.getInstance());
	}
	
	public static void delHome(int homeId)
	{
		new BukkitRunnable() 
		{
			@Override
			public void run() 
			{
				Connection bdd = null;
				PreparedStatement ps = null;
				String req = null;
				
				try
				{
					bdd = NKhome.getInstance().getConnection();
					req = "DELETE FROM " + NKhome.table.get("homes") + " WHERE id = ?";				
					ps = bdd.prepareStatement(req);	   
					ps.setInt(1, homeId);
					ps.executeUpdate();	

					ps.close();
				}
				catch (SQLException e) 
				{
					NKhome.getInstance().getConsole().sendMessage(ChatColor.DARK_RED + NKhome.PName + " Error while deleting a home.");
					e.printStackTrace();
				}	
			}
		}.runTaskAsynchronously(NKhome.getInstance());
	}

	public static LinkedHashMap<String, Home> getPlayerHomes(String playerName)
	{
		LinkedHashMap<String, Home> homes = new LinkedHashMap<String, Home>();
		if(NKhome.players.containsKey(playerName))
		{
			homes = new LinkedHashMap<String, Home>(NKhome.players.get(playerName).getHomes());
			if(NKhome.players.get(playerName).getBed() != null)
			{
				homes.putIfAbsent("bed", NKhome.players.get(playerName).getBed());
			}
		}
		else
		{
			Connection bdd = null;
			ResultSet resultat = null;
			PreparedStatement ps = null;
			String req = null;
			
			int cpt = 0;
			try 
			{
				bdd = NKhome.getInstance().getConnection();
				req = "SELECT h.id, server , h.name , world , x , y , z , pitch , yaw FROM " + NKhome.table.get("homes") + " h LEFT JOIN " + NKhome.table.get("players") + " p ON h.player_id = p.id WHERE p.name = ?";
				ps = bdd.prepareStatement(req);
				ps.setString(1, playerName);
				
				resultat = ps.executeQuery();
				
				while(resultat.next()) 
				{
					homes.putIfAbsent(resultat.getString("name"), new Home(cpt,
							resultat.getInt("id"), 
							resultat.getString("server"), 
							resultat.getString("name"), 
							resultat.getString("world"), 
							resultat.getDouble("x"), 
							resultat.getDouble("y"),
							resultat.getDouble("z"),
							resultat.getFloat("pitch"),
							resultat.getFloat("yaw")));
				}
				ps.close();
				resultat.close();
			} 
			catch (SQLException e) 
			{
				NKhome.getInstance().getConsole().sendMessage(ChatColor.DARK_RED + NKhome.PName + " Error while getting homes.");
				e.printStackTrace();
			}		
		}
		return homes;
	}

	public static void setTpHome(int playerId, int homeTp)
	{
		Connection bdd = null;
		PreparedStatement ps = null;
		String req = null;
		
		try
		{
			bdd = NKhome.getInstance().getConnection();
			
			req = "UPDATE " + NKhome.table.get("players_datas") + " SET home_tp = ? WHERE player_id = ?";  
			ps = bdd.prepareStatement(req);
			ps.setInt(1, homeTp);
			ps.setInt(2, playerId);
			
			ps.executeUpdate();
			ps.close();				
		}
		catch (SQLException e) 
		{
			NKhome.getInstance().getConsole().sendMessage(ChatColor.DARK_RED + NKhome.PName + " Error while setting tp home.");
			e.printStackTrace();
		}	
		
	}
	
	//######################################
	// Utils functions
	//######################################
	
	@SuppressWarnings("deprecation")
	public static Location safeLocation(String worldName, double x, double y, double z, float yaw, float pitch )
	{
		World world = NKhome.getInstance().getServer().getWorld(worldName);
		if(world != null)
		{
			// Block location
			if(world.getBlockAt(CoordTask.BlockCoord(x), (int)y + 1, CoordTask.BlockCoord(z)).getType().isTransparent())
			{
				return new Location( world, x, y, z, yaw, pitch );
			}
		}
		return null;
	}
	
	@SuppressWarnings("deprecation")
	public static Location safeBedLocation(String worldName, double x, double y, double z, float yaw, float pitch )
	{
		
		World world = NKhome.getInstance().getServer().getWorld(worldName);
		if(world != null)
		{
			//droite
			if(world.getBlockAt(CoordTask.BlockCoord(x) + 1, (int)y, CoordTask.BlockCoord(z)).getType().isTransparent() && 
					world.getBlockAt(CoordTask.BlockCoord(x) + 1, (int)y + 1, CoordTask.BlockCoord(z)).getType().isTransparent())
			{
				return new Location( world, x+1, y, z, yaw, pitch );
			}
			//gauche
			if(world.getBlockAt(CoordTask.BlockCoord(x) - 1, (int)y, CoordTask.BlockCoord(z)).getType().isTransparent() && 
					world.getBlockAt(CoordTask.BlockCoord(x) - 1, (int)y + 1, CoordTask.BlockCoord(z)).getType().isTransparent())
			{
				return new Location( world, x-1, y, z, yaw, pitch );
			}
			//haut
			if(world.getBlockAt(CoordTask.BlockCoord(x), (int)y, CoordTask.BlockCoord(z) + 1).getType().isTransparent() && 
					world.getBlockAt(CoordTask.BlockCoord(x), (int)y + 1, CoordTask.BlockCoord(z) + 1).getType().isTransparent())
			{
				return new Location( world, x, y, z+1, yaw, pitch );
			}
			//bas
			if(world.getBlockAt(CoordTask.BlockCoord(x), (int)y, CoordTask.BlockCoord(z) - 1).getType().isTransparent() && 
					world.getBlockAt(CoordTask.BlockCoord(x), (int)y + 1, CoordTask.BlockCoord(z) - 1).getType().isTransparent())
			{
				return new Location( world, x, y, z-1, yaw, pitch );
			}
			// Bed location
			if(world.getBlockAt(CoordTask.BlockCoord(x), (int)y + 1, CoordTask.BlockCoord(z)).getType().isTransparent())
			{
				return new Location( world, x, y, z, yaw, pitch );
			}
		}
		return null;
	}
	
}
