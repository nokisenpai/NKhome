package be.noki_senpai.NKhome.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import be.noki_senpai.NKhome.managers.ConfigManager;
import be.noki_senpai.NKhome.managers.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent;

import be.noki_senpai.NKhome.NKhome;
import be.noki_senpai.NKhome.utils.CoordTask;
import org.bukkit.scheduler.BukkitRunnable;

public class NKPlayer
{
	private int id;
	private UUID playerUUID;
	private String playerName;
	private int homeBonus = 0;
	private Map<String, Home> homes = new LinkedHashMap<String, Home>();
	private Home bed = null;
	private int cpt = 0;

	public NKPlayer(UUID UUID)
	{
		setPlayerUUID(UUID);
		setPlayerName(Bukkit.getOfflinePlayer(playerUUID).getName());

		Connection bdd = null;
		ResultSet resultat = null;
		PreparedStatement ps = null;
		String req = null;
		Integer homeTp = null;
		try
		{
			bdd = DatabaseManager.getConnection();
			;

			// Get 'id', 'uuid', 'name', 'amount' and 'home_tp' from database
			req = "SELECT P.id as id, uuid, name, bonus, home_tp FROM " + DatabaseManager.common.PLAYERS + " P LEFT JOIN "
					+ DatabaseManager.table.PLAYERS_DATA + " HB ON P.id = HB.player_id WHERE uuid = ?";
			ps = bdd.prepareStatement(req);
			ps.setString(1, getPlayerUUID().toString());

			resultat = ps.executeQuery();

			// If there is a result account exist
			if(resultat.next())
			{
				setId(resultat.getInt("id"));
				String tmpName = resultat.getString("name");

				setHomeBonus(resultat.getInt("bonus"));
				if(resultat.wasNull())
				{
					ps.close();
					resultat.close();

					req = "INSERT INTO " + DatabaseManager.table.PLAYERS_DATA + " ( player_id, bonus, home_tp) VALUES ( ? , 0 , -1 )";
					ps = bdd.prepareStatement(req);
					ps.setInt(1, this.getId());
					ps.executeUpdate();

					ps.close();

					setHomeBonus(0);
					homeTp = -1;
				}
				else
				{
					// Get home id where tp player on connect
					homeTp = resultat.getInt("home_tp");
				}

				ps.close();
				resultat.close();

				if(homeTp != -1)
				{
					req = "SELECT server, name, world, x, y, z, pitch, yaw FROM " + DatabaseManager.table.HOMES + " WHERE id = ?";
					ps = bdd.prepareStatement(req);
					ps.setInt(1, homeTp);
					resultat = ps.executeQuery();
					if(resultat.next())
					{
						if(resultat.getString("server").equals(ConfigManager.SERVERNAME))
						{
							Location safeLocation = null;
							String worldName = resultat.getString("world");
							double x = CoordTask.roundFive(resultat.getDouble("x"));
							double y = resultat.getDouble("y");
							double z = CoordTask.roundFive(resultat.getDouble("z"));
							float yaw = resultat.getFloat("yaw");
							float pitch = resultat.getFloat("pitch");

							if(resultat.getString("name").equals("bed")
									&& !(Bukkit.getServer().getWorld(worldName).getBlockAt(CoordTask.BlockCoord(x), (int) y, CoordTask.BlockCoord(z)).getBlockData() instanceof org.bukkit.block.data.type.Bed))
							{
								Bukkit.getServer().getPlayer(this.getPlayerUUID()).sendMessage(
										ChatColor.RED + "Votre lit n'existe plus. \nCe home n'existe pas.");
								delTpHome(homeTp);
							}
							else
							{
								if(resultat.getString("name").equals("bed"))
								{
									safeLocation = CoordTask.safeBedLocation(worldName, x, y, z, yaw, pitch);
								}
								else
								{
									safeLocation = CoordTask.safeLocation(worldName, x, y, z, yaw, pitch);
								}

								if(safeLocation == null)
								{
									Bukkit.getServer().getPlayer(this.getPlayerUUID()).sendMessage(
											ChatColor.RED + "Ce home est obstrué. Par sécurité vous n'avez pas été téléporté.");
								}
								else
								{
									Location finalSafeLocation = safeLocation;
									new BukkitRunnable()
									{
										@Override public void run()
										{
											Bukkit.getServer().getPlayer(playerUUID).teleport(finalSafeLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
										}
									}.runTask(NKhome.getPlugin());

								}
							}
						}
					}
					ps.close();
					resultat.close();
					setTpHome(-1);
				}

				req = "SELECT id, server, name, world, x, y, z, pitch, yaw FROM " + DatabaseManager.table.HOMES + " WHERE player_id = ?";
				ps = bdd.prepareStatement(req);
				ps.setInt(1, getId());
				resultat = ps.executeQuery();

				while(resultat.next())
				{
					if(resultat.getString("name").equals("bed"))
					{
						setBed(resultat.getInt("id"), resultat.getString("server"), resultat.getString("name"), resultat.getString("world"), resultat.getDouble("x"), resultat.getDouble("y"), resultat.getDouble("z"), resultat.getFloat("pitch"), resultat.getFloat("yaw"));
					}
					else
					{
						addHome(resultat.getInt("id"), resultat.getString("server"), resultat.getString("name"), resultat.getString("world"), resultat.getDouble("x"), resultat.getDouble("y"), resultat.getDouble("z"), resultat.getFloat("pitch"), resultat.getFloat("yaw"));
					}
				}
				ps.close();
				resultat.close();
			}
			else
			{
				Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + NKhome.PNAME + " Error while setting a player. (#1)");
			}
		}
		catch(SQLException e)
		{
			Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + NKhome.PNAME + " Error while setting a player. (Error#data.Players.000)");
			e.printStackTrace();
		}
	}

	//######################################
	// Getters & Setters
	//######################################

	// Getter & Setter 'id'
	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	// Getter & Setter 'playerUUID'
	public UUID getPlayerUUID()
	{
		return playerUUID;
	}

	public void setPlayerUUID(UUID playerUUID)
	{
		this.playerUUID = playerUUID;
	}

	// Getter & Setter 'playerName'
	public String getPlayerName()
	{
		return playerName;
	}

	public void setPlayerName(String playerName)
	{
		this.playerName = playerName;
	}

	// Getter & Setter 'homeBonus'
	public int getHomeBonus()
	{
		return homeBonus;
	}

	public void setHomeBonus(int homeBonus)
	{
		this.homeBonus = homeBonus;
	}

	// Getter & Setter 'homes'
	public Map<String, Home> getHomes()
	{
		return homes;
	}

	public void setHomes(Map<String, Home> homes)
	{
		this.homes = homes;
	}

	// Getter & Setter 'bed'
	public Home getBed()
	{
		return bed;
	}

	public void setBed(int id, String server, String name, String world, double x, double y, double z, float pitch, float yaw)
	{
		this.bed = new Home(-1, id, server, name, world, x, y, z, pitch, yaw);
	}

	public void delBed()
	{
		this.bed = null;
	}

	// Getter & Setter 'cpt'
	public int getCpt()
	{
		return cpt;
	}

	public void setCpt(int cpt)
	{
		this.cpt = cpt;
	}

	//######################################
	// Homes functions
	//######################################

	public void addHome(int id, String server, String name, String world, double x, double y, double z, float pitch, float yaw)
	{
		cpt++;
		this.homes.put(name, new Home(cpt, id, server, name, world, x, y, z, pitch, yaw));
	}

	// delHome
	public void delHome(String name)
	{
		cpt--;
		this.homes.remove(name);
	}

	// Set tp home
	public void setTpHome(int homeTp)
	{
		Connection bdd = null;
		PreparedStatement ps = null;
		String req = null;

		try
		{
			bdd = DatabaseManager.getConnection();

			req = "UPDATE " + DatabaseManager.table.PLAYERS_DATA + " SET home_tp = ? WHERE player_id = ?";
			ps = bdd.prepareStatement(req);
			ps.setInt(1, homeTp);
			ps.setInt(2, getId());

			ps.executeUpdate();
			ps.close();
		}
		catch(SQLException e)
		{
			Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + NKhome.PNAME + " Error while setting tp home.");
			e.printStackTrace();
		}
	}

	// Delete tp home
	public void delTpHome(int homeTp)
	{
		Connection bdd = null;
		PreparedStatement ps = null;
		String req = null;

		try
		{
			bdd = DatabaseManager.getConnection();
			req = "DELETE FROM " + DatabaseManager.table.HOMES + " WHERE id = ?";
			ps = bdd.prepareStatement(req);
			ps.setInt(1, homeTp);
			ps.executeUpdate();

			ps.close();
		}
		catch(SQLException e)
		{
			Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + NKhome.PNAME + " Error while deleting a home.");
			e.printStackTrace();
		}
	}
}
