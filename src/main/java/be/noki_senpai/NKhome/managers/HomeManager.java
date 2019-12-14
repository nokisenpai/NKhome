package be.noki_senpai.NKhome.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import be.noki_senpai.NKhome.utils.CheckType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.ConsoleCommandSender;

import be.noki_senpai.NKhome.NKhome;
import be.noki_senpai.NKhome.data.Home;
import be.noki_senpai.NKhome.data.NKPlayer;
import be.noki_senpai.NKhome.utils.CoordTask;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class HomeManager
{
	// Players datas
	private Map<String, NKPlayer> players = null;
	private ConsoleCommandSender console = null;

	public HomeManager()
	{
		this.players = new TreeMap<String, NKPlayer>(String.CASE_INSENSITIVE_ORDER);
		this.console = Bukkit.getConsoleSender();
	}

	public void loadHome()
	{
		// Get all connected players
		Bukkit.getOnlinePlayers().forEach(player -> players.put(player.getDisplayName(), new NKPlayer(player.getUniqueId())));
	}

	public void unloadHome()
	{
		players.clear();
	}

	// ######################################
	// Getters & Setters
	// ######################################

	// getPlayer
	public NKPlayer getPlayer(String playerName)
	{
		return players.get(playerName);
	}

	public void addPlayer(Player player)
	{
		players.put(player.getName(), new NKPlayer(player.getUniqueId()));
	}

	public void delPlayer(String playerName)
	{
		players.remove(playerName);
	}

	// **************************************
	// **************************************
	// Homes functions
	// **************************************
	// **************************************

	// ######################################
	// updateHome
	// ######################################

	private void updateOnlineHome(Home home, Location location)
	{
		home.setServer(ConfigManager.SERVERNAME);
		home.setWorld(location.getWorld().getName());
		home.setX(location.getX());
		home.setY(location.getY());
		home.setZ(location.getZ());
		home.setPitch(location.getPitch());
		home.setYaw(location.getYaw());
	}

	private void updateOfflineHome(Home home, Location location)
	{
		Connection bdd = null;
		PreparedStatement ps = null;
		String req = null;

		try
		{
			bdd = DatabaseManager.getConnection();

			req = "UPDATE " + DatabaseManager.table.HOMES
					+ " SET server = ? , world = ? , x = ? , y = ? , z = ? , pitch = ? , yaw = ? WHERE id = ?";

			ps = bdd.prepareStatement(req);
			ps.setString(1, ConfigManager.SERVERNAME);
			ps.setString(2, location.getWorld().getName());
			ps.setDouble(3, location.getX());
			ps.setDouble(4, location.getY());
			ps.setDouble(5, location.getZ());
			ps.setDouble(6, location.getPitch());
			ps.setDouble(7, location.getYaw());
			ps.setInt(8, home.getId());

			ps.executeUpdate();
			ps.close();
		}
		catch(SQLException e)
		{
			console.sendMessage(ChatColor.DARK_RED + NKhome.PNAME + " Error while updating a home.");
			e.printStackTrace();
		}
	}

	private void updateOnlineBedHome(Home bed, Location location)
	{
		bed.setServer(ConfigManager.SERVERNAME);
		bed.setWorld(location.getWorld().getName());
		bed.setX(CoordTask.BedNegateAdjust(location.getX()));
		bed.setY(location.getY());
		bed.setZ(CoordTask.BedNegateAdjust(location.getZ()));
		bed.setPitch(location.getPitch());
		bed.setYaw(location.getYaw());
	}

	private void updateOfflineBedHome(Home bed, Location location)
	{
		Connection bdd = null;
		PreparedStatement ps = null;
		String req = null;

		try
		{
			bdd = DatabaseManager.getConnection();

			req = "UPDATE " + DatabaseManager.table.HOMES
					+ " SET server = ? , world = ? , x = ? , y = ? , z = ? , pitch = ? , yaw = ? WHERE id = ?";

			ps = bdd.prepareStatement(req);
			ps.setString(1, ConfigManager.SERVERNAME);
			ps.setString(2, location.getWorld().getName());
			ps.setDouble(3, CoordTask.BedNegateAdjust(location.getX()));
			ps.setDouble(4, location.getY());
			ps.setDouble(5, CoordTask.BedNegateAdjust(location.getZ()));
			ps.setDouble(6, location.getPitch());
			ps.setDouble(7, location.getYaw());
			ps.setInt(8, bed.getId());

			ps.executeUpdate();
			ps.close();
		}
		catch(SQLException e)
		{
			console.sendMessage(ChatColor.DARK_RED + NKhome.PNAME + " Error while updating a home.");
			e.printStackTrace();
		}
	}

	public void updateHome(String playerName, String homeName, Location location)
	{
		if(homeName.equals("bed"))
		{
			updateOnlineBedHome(players.get(playerName).getBed(), location);
			updateOfflineBedHome(players.get(playerName).getBed(), location);
		}
		else
		{
			updateOnlineHome(players.get(playerName).getHomes().get(homeName), location);
			updateOfflineHome(players.get(playerName).getHomes().get(homeName), location);
		}
	}

	// ######################################
	// addHome
	// ######################################

	private void addOnlineHome(String playerName, String homeName, int id, Location location)
	{
		players.get(playerName).addHome(id, ConfigManager.SERVERNAME, homeName, location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getPitch(), location.getYaw());
	}

	private int addOfflineHome(String playerName, String homeName, Location location)
	{
		int id = -1;
		Connection bdd = null;
		ResultSet resultat = null;
		PreparedStatement ps = null;
		String req = null;

		try
		{
			bdd = DatabaseManager.getConnection();
			req = "INSERT INTO " + DatabaseManager.table.HOMES
					+ " ( player_id, server, name, world, x, y, z, pitch, yaw ) VALUES ( ? , ? , ? , ? , ? , ? , ? , ? , ? )";

			ps = bdd.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, players.get(playerName).getId());
			ps.setString(2, ConfigManager.SERVERNAME);
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
			id = resultat.getInt(1);
			ps.close();
			resultat.close();
		}
		catch(SQLException e)
		{
			console.sendMessage(ChatColor.DARK_RED + NKhome.PNAME + " Error while setting a home.");
			e.printStackTrace();
		}
		return id;
	}

	private void addOnlineBedHome(String playerName, String homeName, int id, Location location)
	{
		players.get(playerName).setBed(id, ConfigManager.SERVERNAME, homeName, location.getWorld().getName(), CoordTask.BedNegateAdjust(location.getX()), location.getY(), CoordTask.BedNegateAdjust(location.getZ()), location.getPitch(), location.getYaw());
	}

	private int addOfflineBedHome(String playerName, String homeName, Location location)
	{
		int id = -1;
		Connection bdd = null;
		ResultSet resultat = null;
		PreparedStatement ps = null;
		String req = null;

		try
		{
			bdd = DatabaseManager.getConnection();
			req = "INSERT INTO " + DatabaseManager.table.HOMES
					+ " ( player_id, server, name, world, x, y, z, pitch, yaw ) VALUES ( ? , ? , ? , ? , ? , ? , ? , ? , ? )";

			ps = bdd.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, players.get(playerName).getId());
			ps.setString(2, ConfigManager.SERVERNAME);
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

			id = resultat.getInt(1);

			ps.close();
			resultat.close();
		}
		catch(SQLException e)
		{
			console.sendMessage(ChatColor.DARK_RED + NKhome.PNAME + " Error while setting a home.");
			e.printStackTrace();
		}
		return id;
	}

	public void addHome(String playerName, String homeName, Location location)
	{
		int id = -1;
		if(homeName.equals("bed"))
		{
			id = addOfflineBedHome(playerName, homeName, location);
			addOnlineBedHome(playerName, homeName, id, location);
		}
		else
		{
			id = addOfflineHome(playerName, homeName, location);
			addOnlineHome(playerName, homeName, id, location);
		}
	}

	// ######################################
	// delHome
	// ######################################

	private void delOnlineHome(String playerName, String homeName)
	{
		if(players.get(playerName) != null)
		{
			players.get(playerName).delHome(homeName);
		}
	}

	private void delOfflineHome(String playerName, int id)
	{
		Connection bdd = null;
		PreparedStatement ps = null;
		String req = null;

		try
		{
			bdd = DatabaseManager.getConnection();
			req = "DELETE FROM " + DatabaseManager.table.HOMES + " WHERE id = ?";
			ps = bdd.prepareStatement(req);
			ps.setInt(1, id);
			ps.executeUpdate();

			ps.close();
		}
		catch(SQLException e)
		{
			console.sendMessage(ChatColor.DARK_RED + NKhome.PNAME + " Error while deleting a home.");
			e.printStackTrace();
		}
	}

	private void delOnlineBedHome(String playerName)
	{
		if(players.get(playerName) != null)
		{
			players.get(playerName).delBed();
		}
	}

	private void delOfflineBedHome(String playerName, int id)
	{
		Connection bdd = null;
		PreparedStatement ps = null;
		String req = null;

		try
		{
			bdd = DatabaseManager.getConnection();
			req = "DELETE FROM " + DatabaseManager.table.HOMES + " WHERE id = ?";
			ps = bdd.prepareStatement(req);
			ps.setInt(1, id);
			ps.executeUpdate();

			ps.close();
		}
		catch(SQLException e)
		{
			console.sendMessage(ChatColor.DARK_RED + NKhome.PNAME + " Error while deleting a home.");
			e.printStackTrace();
		}
	}

	public void delHome(String playerName, Home home)
	{
		if(home.getName().equals("bed"))
		{
			delOfflineBedHome(playerName, home.getId());
			delOnlineBedHome(playerName);
		}
		else
		{
			delOfflineHome(playerName,  home.getId());
			delOnlineHome(playerName,  home.getName());
		}
	}

	// ######################################
	// getHomes (bed included)
	// ######################################

	private Map<String, Home> getOnlineHomes(String playerName)
	{
		Map<String, Home> homes = new LinkedHashMap<String, Home>(players.get(playerName).getHomes());
		if(players.get(playerName).getBed() != null)
		{
			homes.put("bed", players.get(playerName).getBed());
		}
		return homes;
	}

	private Map<String, Home> getOfflineHomes(String playerName)
	{
		Map<String, Home> homes = new LinkedHashMap<String, Home>();
		Home bed = null;
		Connection bdd = null;
		ResultSet resultat = null;
		PreparedStatement ps = null;
		String req = null;

		int cpt = 0;
		try
		{
			bdd = DatabaseManager.getConnection();
			req = "SELECT h.id, h.server , h.name , world , x , y , z , pitch , yaw FROM " + DatabaseManager.table.HOMES + " h LEFT JOIN "
					+ DatabaseManager.table.PLAYERS + " p ON h.player_id = p.id WHERE p.name = ?";
			ps = bdd.prepareStatement(req);
			ps.setString(1, playerName);

			resultat = ps.executeQuery();

			while(resultat.next())
			{
				if(resultat.getString("name").equals("bed"))
				{
					bed = new Home(-1, resultat.getInt("id"), resultat.getString("server"), resultat.getString("name"), resultat.getString("world"), resultat.getDouble("x"), resultat.getDouble("y"), resultat.getDouble("z"), resultat.getFloat("pitch"), resultat.getFloat("yaw"));
				}
				else
				{
					cpt++;
					homes.put(resultat.getString("name"), new Home(cpt, resultat.getInt("id"), resultat.getString("server"), resultat.getString("name"), resultat.getString("world"), resultat.getDouble("x"), resultat.getDouble("y"), resultat.getDouble("z"), resultat.getFloat("pitch"), resultat.getFloat("yaw")));
				}
			}

			if(bed != null)
			{
				homes.put("bed", bed);
			}

			ps.close();
			resultat.close();
		}
		catch(SQLException e)
		{
			console.sendMessage(ChatColor.DARK_RED + NKhome.PNAME + " Error while getting homes.");
			e.printStackTrace();
		}
		return homes;
	}

	public Map<String, Home> getHomes(String playerName)
	{
		Map<String, Home> homes = null;
		if(players.containsKey(playerName))
		{
			homes = getOnlineHomes(playerName);
		}
		else
		{
			homes = getOfflineHomes(playerName);
		}
		return homes;
	}

	// ######################################
	// getHome
	// ######################################

	public Home getHome(String playerName, String homeName)
	{
		// player is online
		if(players.containsKey(playerName))
		{
			// player has home with that name
			if(players.get(playerName).getHomes().containsKey(homeName))
			{
				return players.get(playerName).getHomes().get(homeName);
			}

			else if(homeName.equals("bed"))
			{
				return players.get(playerName).getBed();
			}

			else if(CheckType.isNumber(homeName))
			{
				int homeCpt = Integer.parseInt(homeName);
				if(homeCpt <= 0)
				{
					return null;
				}
				List<Home> homeList = new ArrayList<Home>(players.get(playerName).getHomes().values());
				if(homeList.size() >= homeCpt)
				{
					return homeList.get(homeCpt - 1);
				}
			}
		}
		else
		{
			// Player isn't online
			// Get his homes
			Map<String, Home> playerHomes = getHomes(playerName);
			if(playerHomes.containsKey(homeName))
			{
				return playerHomes.get(homeName);
			}

			else if(CheckType.isNumber(homeName))
			{
				int homeCpt = Integer.parseInt(homeName);
				if(homeCpt <= 0)
				{
					return null;
				}
				List<Home> homeList = new ArrayList<Home>(playerHomes.values());
				if(homeList.size() >= homeCpt)
				{
					return homeList.get(homeCpt - 1);
				}
			}
		}
		return null;
	}

	public boolean isBedBlock(Home homeBed)
	{
		if(Bukkit.getServer().getWorld(homeBed.getWorld()).getBlockAt((int) CoordTask.BlockCoord(homeBed.getX()), (int) homeBed.getY(), (int) CoordTask.BlockCoord(homeBed.getZ())).getBlockData() instanceof org.bukkit.block.data.type.Bed)
		{
			return true;
		}
		return false;
	}


	// Check if a player is connected in other server
	public String getOtherServer(String playername)
	{
		Connection bdd = null;
		PreparedStatement ps = null;
		ResultSet resultat = null;
		String req = null;

		try
		{
			bdd = DatabaseManager.getConnection();

			req = "SELECT server FROM " + DatabaseManager.table.PLAYERS + " WHERE name = ?";
			ps = bdd.prepareStatement(req);
			ps.setString(1, playername);

			resultat = ps.executeQuery();

			if(resultat.next())
			{
				String server = resultat.getString("server");

				resultat.close();
				ps.close();

				return server;
			}
			resultat.close();
			ps.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	// Add a player entry on sql DB with his server
	public void addOtherServer(String playername)
	{
		Connection bdd = null;
		PreparedStatement ps = null;
		String req = null;

		try
		{
			bdd = DatabaseManager.getConnection();

			req = "UPDATE " + DatabaseManager.table.PLAYERS + " SET server = ? WHERE name = ?";
			ps = bdd.prepareStatement(req);
			ps.setString(1, ConfigManager.SERVERNAME);
			ps.setString(2, playername);

			ps.execute();
			ps.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	// Remove a player entry on sql DB with his server
	public void removeOtherServer(String playername)
	{
		Connection bdd = null;
		PreparedStatement ps = null;
		String req = null;

		try
		{
			bdd = DatabaseManager.getConnection();

			req = "UPDATE " + DatabaseManager.table.PLAYERS + " SET server = NULL WHERE name = ?";
			ps = bdd.prepareStatement(req);
			ps.setString(1, playername);

			ps.execute();
			ps.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
}
