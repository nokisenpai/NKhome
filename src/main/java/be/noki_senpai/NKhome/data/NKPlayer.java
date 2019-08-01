package be.noki_senpai.NKhome.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import be.noki_senpai.NKhome.NKhome;

public class NKPlayer 
{
	private int id;
	private UUID playerUUID;
	private String playerName;
	private int homeBonus = 0;
	private Map<String, Home> homes = new TreeMap<String, Home>(String.CASE_INSENSITIVE_ORDER);
	int cpt = 0;
	
	public NKPlayer(UUID UUID)
	{
		setPlayerUUID(UUID);
		setPlayerName(Bukkit.getOfflinePlayer(playerUUID).getName());
		
		Connection bdd = null;
		ResultSet resultat = null;
		PreparedStatement ps = null;
		String req = null;
		
		try 
		{
			bdd = NKhome.getInstance().getConnection();
			
			// Get 'id', 'uuid', 'name' and 'amount' from database
			req = "SELECT id, uuid, name, bonus FROM " + NKhome.table.get("players") + " P LEFT JOIN " + NKhome.table.get("home_bonus") + " HB ON P.id = HB.player_id WHERE uuid = ?";
			ps = bdd.prepareStatement(req);
			ps.setString(1, getPlayerUUID().toString());
			
	        resultat = ps.executeQuery();
	        
	        // If there is a result account exist
	        if(resultat.next()) 
	        {
	        	setId(resultat.getInt("id"));
	        	setHomeBonus(resultat.getInt("bonus"));
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
	        	
	        	req = "SELECT id, server, name, x, y, z, facing, rotation, tp FROM " + NKhome.table.get("homes") + " WHERE player_id = ?";
				ps = bdd.prepareStatement(req);
				ps.setInt(1, getId());
				
				while(resultat.next()) 
		        {
					addHome(resultat.getInt("id"), 
							resultat.getString("server"), 
							resultat.getString("name"), 
							resultat.getDouble("x"), 
							resultat.getDouble("y"),
							resultat.getDouble("z"),
							resultat.getDouble("facing"),
							resultat.getDouble("rotation"));
					if(resultat.getBoolean("tp"))
					{
						// fonction de tp en indiquant le home => homes.get(resultat.getString("name"))
						
					}
		        }
				
		        resultat = ps.executeQuery();
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
	public Map<String, Home> getHomes()
	{
		return homes;
	}
	public void setHomes(Map<String, Home> homes)
	{
		this.homes = homes;
	}

	
	
	//######################################
	// Add & Remove homes
	//######################################
	
	public void addHome(int id, String server, String name, double x, double y, double z, double facing, double rotation)
	{
		this.homes.putIfAbsent(name, new Home(cpt, id, server, name, x, y, z, facing, rotation));
		cpt++;
	}
	
	public void removeHome(String name)
	{
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
