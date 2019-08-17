package be.noki_senpai.NKhome.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent;

import be.noki_senpai.NKhome.NKhome;
import be.noki_senpai.NKhome.utils.CoordTask;

public class NKPlayer 
{
	private int id;
	private UUID playerUUID;
	private String playerName;
	private int homeBonus = 0;
	private LinkedHashMap<String, Home> homes = new LinkedHashMap<String, Home>();
	private Home bed = null;
	int cpt = 0;
	
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
			bdd = NKhome.getInstance().getConnection();
			
			// Get 'id', 'uuid', 'name' and 'amount' from database
			req = "SELECT P.id as id, uuid, name, bonus, home_tp FROM " + NKhome.table.get("players") + " P LEFT JOIN " + NKhome.table.get("players_datas") + " HB ON P.id = HB.player_id WHERE uuid = ?";
			ps = bdd.prepareStatement(req);
			ps.setString(1, getPlayerUUID().toString());
			
			resultat = ps.executeQuery();
			
			// If there is a result account exist
			if(resultat.next()) 
			{
				setId(resultat.getInt("id"));

				setHomeBonus(resultat.getInt("bonus"));
				homeTp = resultat.getInt("home_tp");
				// If names are differents, update in database
				if(!resultat.getString("name").equals(getPlayerName()))
				{
					ps.close();
					resultat.close();
					
					req = "UPDATE " + NKhome.table.get("players") + " SET name = ? WHERE id = ?";  
					ps = bdd.prepareStatement(req);
					ps.setString(1, getPlayerName());
					ps.setInt(2, getId());
					
					ps.executeUpdate();
					ps.close();
				}
				else
				{
					ps.close();
					resultat.close();
				}
				
				if(homeTp != -1)
				{
					req = "SELECT server, name, world, x, y, z, pitch, yaw FROM " + NKhome.table.get("homes") + " WHERE id = ?";
					ps = bdd.prepareStatement(req);
					ps.setInt(1, homeTp);
					resultat = ps.executeQuery();
					if(resultat.next())
					{
						if(resultat.getString("server").equals(NKhome.serverName))
						{
							Location safeLocation = null;
							String worldName = resultat.getString("world");
							double x = CoordTask.roundFive(resultat.getDouble("x"));
							double y = resultat.getDouble("y");
							double z = CoordTask.roundFive(resultat.getDouble("z"));
							float yaw = resultat.getFloat("yaw");
							float pitch = resultat.getFloat("pitch");
							
							if(resultat.getString("name").equals("bed") && !(NKhome.getInstance().getServer().getWorld(worldName).getBlockAt(CoordTask.BlockCoord(x), (int)y, CoordTask.BlockCoord(z)).getBlockData() instanceof org.bukkit.block.data.type.Bed))
							{
								NKhome.getInstance().getServer().getPlayer(this.getPlayerUUID()).sendMessage(ChatColor.RED + "Votre lit n'existe plus. \nCe home n'existe pas.");
								NKhome.delHome(homeTp);
							}
							else
							{
								if(resultat.getString("name").equals("bed"))
								{
									safeLocation = NKhome.safeBedLocation(worldName, x, y, z, yaw, pitch);
								}
								else
								{
									safeLocation = NKhome.safeLocation(worldName, x, y, z, yaw, pitch);
								}
								
								if(safeLocation == null)
								{
									NKhome.getInstance().getServer().getPlayer(this.getPlayerUUID()).sendMessage(ChatColor.RED + "Ce home est obstrué. Par sécurité vous n'avez pas été téléporté.");
								}
								else
								{
									NKhome.getInstance().getServer().getPlayer(playerUUID).teleport(safeLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
								}
							}
						}
					}
					ps.close();
					resultat.close();
					NKhome.setTpHome(getId(), -1);
				}
				
				req = "SELECT id, server, name, world, x, y, z, pitch, yaw FROM " + NKhome.table.get("homes") + " WHERE player_id = ?";
				ps = bdd.prepareStatement(req);
				ps.setInt(1, getId());
				resultat = ps.executeQuery();
				
				while(resultat.next()) 
				{
					if(resultat.getString("name").equals("bed"))
					{
						setBed(resultat.getInt("id"), 
								resultat.getString("server"), 
								resultat.getString("name"), 
								resultat.getString("world"), 
								resultat.getDouble("x"), 
								resultat.getDouble("y"),
								resultat.getDouble("z"),
								resultat.getFloat("pitch"),
								resultat.getFloat("yaw"));
					}
					else
					{
						addHome(resultat.getInt("id"), 
								resultat.getString("server"), 
								resultat.getString("name"),
								resultat.getString("world"), 
								resultat.getDouble("x"), 
								resultat.getDouble("y"),
								resultat.getDouble("z"),
								resultat.getFloat("pitch"),
								resultat.getFloat("yaw"));
					}
				}
				ps.close();
				resultat.close();
			}
			else
			{
				//Add new player on database
				ps.close();
				resultat.close();
				
				req = "INSERT INTO " + NKhome.table.get("players") + " ( uuid, name) VALUES ( ? , ? )";				
				ps = bdd.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, getPlayerUUID().toString());
				ps.setString(2, getPlayerName());
				ps.executeUpdate();  
				resultat = ps.getGeneratedKeys();	
				
				resultat.next();  
				setId(resultat.getInt(1));
				
				ps.close();
				resultat.close();
				
				req = "INSERT INTO " + NKhome.table.get("players_datas") + " ( player_id, bonus, home_tp) VALUES ( ? , 0 , -1 )";				
				ps = bdd.prepareStatement(req);
				ps.setInt(1, this.getId());
				ps.executeUpdate();  
				
				ps.close();
			}
		} 
		catch (SQLException e) 
		{
			NKhome.getInstance().getConsole().sendMessage(ChatColor.DARK_RED + NKhome.PName + " Error while setting a player. (Error#data.Players.000)");
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
	public LinkedHashMap<String, Home> getHomes()
	{
		return homes;
	}
	public void setHomes(LinkedHashMap<String, Home> homes)
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
	// Add & Remove homes
	//######################################
	
	public void addHome(int id, String server, String name, String world, double x, double y, double z, float pitch, float yaw)
	{
		cpt++;
		this.homes.putIfAbsent(name, new Home(cpt, id, server, name, world, x, y, z, pitch, yaw));
		
	}
	
	public void removeHome(String name)
	{
		cpt--;
		this.homes.remove(name);
	}
	
	
	
	//######################################
	// Save amount
	//######################################
	
	/*public void save()
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
					/*
						INSERT INTO component_psar (tbl_id, row_nr, col_1, col_2, col_3, col_4, col_5, col_6, unit, add_info, fsar_lock)
						VALUES('2', '1', '1', '1', '1', '1', '1', '1', '1', '1', 'N')
						ON DUPLICATE KEY UPDATE col_1 = VALUES(col_1), col_2 = VALUES(col_2), col_3 = VALUES(col_3), col_4 = VALUES(col_4), col_5 = VALUES(col_5), col_6 = VALUES(col_6), unit = VALUES(unit), add_info = VALUES(add_info), fsar_lock = VALUES(fsar_lock)
					 s
					bdd = NKeconomy.getInstance().getConnection();
					
					req = "UPDATE " + NKeconomy.table.get("accounts") + " SET amount = ? WHERE uuid = ?";
					ps = bdd.prepareStatement(req);
					ps.setDouble(1, getAmount());
					ps.setString(2, getPlayerUUID().toString());
					
					ps.executeUpdate();
					ps.close();
				} 
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(NKeconomy.getInstance());
	}*/
}
