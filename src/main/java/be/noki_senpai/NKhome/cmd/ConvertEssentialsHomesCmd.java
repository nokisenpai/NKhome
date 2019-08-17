package be.noki_senpai.NKhome.cmd;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import be.noki_senpai.NKhome.NKhome;
import be.noki_senpai.NKhome.data.Home;
import be.noki_senpai.NKhome.utils.CoordTask;

public class ConvertEssentialsHomesCmd implements CommandExecutor
{
	
	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) 
	{	
		// Command called by a player
		if (sender instanceof Player) 
		{
			if(!(sender.hasPermission("*") || sender.hasPermission("nkhome.*") || sender.hasPermission("nkhome.convert") || sender.hasPermission("nkhome.admin")))
			{
				// Send that the player does not have the permission
				sender.sendMessage(ChatColor.RED + " Vous n'avez pas la permission !");
				return true;
			}
			else
			{
				convertEssentialsHomes(sender);
			}
		}
		
		
		// Command called by Console
		if (sender instanceof ConsoleCommandSender)
		{
			convertEssentialsHomes(sender);
		}
		
		
		return true;
	}
	
	
	public void convertEssentialsHomes(CommandSender sender)
	{
		//File worthCfgFile = new File(Bukkit.getPluginManager().getPlugin("Essentials").getDataFolder(), "worth.yml");
		//String cwd = new File("").getAbsolutePath();
		//sender.sendMessage(ChatColor.GREEN + cwd);
		Path path = Paths.get("plugins/Essentials/userdata");
		if (Files.exists(path)) 
		{
			// Get all data files
			File f = new File(path.toString());
			ArrayList<File> files = new ArrayList<File>(Arrays.asList(f.listFiles()));
			
			sender.sendMessage("" + ChatColor.GREEN + files.size() + " fichier(s) de donnees a traiter. Cette operation peut durer un moment.");
			
			// For each data file
			new BukkitRunnable() 
			{
				@Override
				public void run() 
				{
					int traitedFile = 0;
					int ignoredFile = 0;
					int nbHome = 0;
					HashMap<String, Integer> playerList = new HashMap<String, Integer>();
					
					try 
					{
						Connection bdd = null;
						ResultSet resultat = null;
						PreparedStatement ps = null;
						String req = null;
						
						bdd = NKhome.getInstance().getConnection();
						
						req = "SELECT id, uuid FROM " + NKhome.table.get("players");
						ps = bdd.prepareStatement(req);
						resultat = ps.executeQuery();
						
						while(resultat.next()) 
						{
							playerList.putIfAbsent(resultat.getString("uuid"), resultat.getInt("id"));
						}
						ps.close();
						resultat.close();
						
						
						
						
						
						
						for(int i = 0 ; i < files.size() ; i++)
						{
							File playerFile = files.get(i);
							boolean ok = false;
							if(playerFile != null)
							{
								// Get player uuid from data file name
								UUID uuid = (UUID.fromString(playerFile.getName().replace(".yml", "")));
	
								// Check if this is a valid uuid
								if(uuid != null)
								{
									// Load data file as FileConfiguration
									FileConfiguration essentialsHomes = YamlConfiguration.loadConfiguration(playerFile);
									
									if(essentialsHomes.getKeys(false).size() != 0)
									{
										String playerName = essentialsHomes.getString("lastAccountName");
										
										if(playerName == null)
										{
											playerName = NKhome.getInstance().getServer().getOfflinePlayer(uuid).getName();
										}
										
										if(playerName != null)
										{
											int id = -1;
											if(playerList.containsKey(uuid.toString()))
											{
												id = playerList.get(uuid.toString());
											}
											else
											{
												try 
												{
													//Add new player on database	
													req = "INSERT INTO " + NKhome.table.get("players") + " ( uuid, name) VALUES ( ? , ? ) ON DUPLICATE KEY UPDATE name = name";				
													ps = bdd.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
													ps.setString(1, uuid.toString());
													ps.setString(2, playerName);
													ps.executeUpdate();  
													resultat = ps.getGeneratedKeys();	
													
													resultat.next();  
													id = resultat.getInt(1);
													
													ps.close();
													resultat.close();
												} 
												catch (SQLException e) 
												{
													NKhome.getInstance().getConsole().sendMessage(ChatColor.DARK_RED + NKhome.PName + " Error while setting a player. (Error#cmd.ConvertEssentialsHomesCmd.000)");
												}	
											}
											HashMap<String, Home> homes = new HashMap<String, Home>();
											ConfigurationSection configHomes = essentialsHomes.getConfigurationSection("homes");
											if(configHomes != null)
											{
												for(String key : configHomes.getKeys(false))
												{
													ConfigurationSection configHome = essentialsHomes.getConfigurationSection("homes." + key);
													if(!NKhome.convertIgnore.contains(configHome.getString("world")))
													{
														String server = whichServer(configHome.getString("world"));
														String name = key;
														String world = configHome.getString("world");
														double x = configHome.getDouble("x");
														double y = configHome.getDouble("y");
														double z = configHome.getDouble("z");
														float pitch = (float) configHome.getDouble("pitch");
														float yaw = (float) configHome.getDouble("yaw");
														
														homes.putIfAbsent(key, new Home(-1, -1, server, name, world, x, y, z, pitch, yaw));
													}
												}
												
												Location bedSpawnLocation = NKhome.getInstance().getServer().getOfflinePlayer(uuid).getBedSpawnLocation();
												if(bedSpawnLocation != null)
												{
													World bedWorld = null;
													if(bedSpawnLocation.isWorldLoaded())
													{
														bedWorld = bedSpawnLocation.getWorld();
													}
													if(bedWorld != null && !NKhome.convertIgnore.contains(bedWorld.getName()))
													{
														Block bedSpawnBlock = bedWorld.getBlockAt((int)CoordTask.BedNegateAdjust(bedSpawnLocation.getX()), (int)bedSpawnLocation.getY(), (int)CoordTask.BedNegateAdjust(bedSpawnLocation.getZ()));
														
														if(bedSpawnBlock != null)
														{
															Block bed = null;
															Block blockTop = null;
															Block blockBot = null;
															if(bedSpawnBlock.getRelative(BlockFace.NORTH).getBlockData() instanceof org.bukkit.block.data.type.Bed)
															{
																bed = bedSpawnBlock.getRelative(BlockFace.NORTH);
															}
															else if(bedSpawnBlock.getRelative(BlockFace.NORTH_EAST).getBlockData() instanceof org.bukkit.block.data.type.Bed)
															{
																bed = bedSpawnBlock.getRelative(BlockFace.NORTH_EAST);
															}
															else if(bedSpawnBlock.getRelative(BlockFace.EAST).getBlockData() instanceof org.bukkit.block.data.type.Bed)
															{
																bed = bedSpawnBlock.getRelative(BlockFace.EAST);
															}
															else if(bedSpawnBlock.getRelative(BlockFace.SOUTH_EAST).getBlockData() instanceof org.bukkit.block.data.type.Bed)
															{
																bed = bedSpawnBlock.getRelative(BlockFace.SOUTH_EAST);
															}
															else if(bedSpawnBlock.getRelative(BlockFace.SOUTH).getBlockData() instanceof org.bukkit.block.data.type.Bed)
															{
																bed = bedSpawnBlock.getRelative(BlockFace.SOUTH);
															}
															else if(bedSpawnBlock.getRelative(BlockFace.SOUTH_WEST).getBlockData() instanceof org.bukkit.block.data.type.Bed)
															{
																bed = bedSpawnBlock.getRelative(BlockFace.SOUTH_WEST);
															}
															else if(bedSpawnBlock.getRelative(BlockFace.WEST).getBlockData() instanceof org.bukkit.block.data.type.Bed)
															{
																bed = bedSpawnBlock.getRelative(BlockFace.WEST);
															}
															else if(bedSpawnBlock.getRelative(BlockFace.NORTH_WEST).getBlockData() instanceof org.bukkit.block.data.type.Bed)
															{
																bed = bedSpawnBlock.getRelative(BlockFace.NORTH_WEST);
															}
															else
															{
																blockTop = bedSpawnBlock.getRelative(BlockFace.UP);
																if(blockTop.getRelative(BlockFace.NORTH).getBlockData() instanceof org.bukkit.block.data.type.Bed)
																{
																	bed = blockTop.getRelative(BlockFace.NORTH);
																}
																else if(blockTop.getRelative(BlockFace.NORTH_EAST).getBlockData() instanceof org.bukkit.block.data.type.Bed)
																{
																	bed = blockTop.getRelative(BlockFace.NORTH_EAST);
																}
																else if(blockTop.getRelative(BlockFace.EAST).getBlockData() instanceof org.bukkit.block.data.type.Bed)
																{
																	bed = blockTop.getRelative(BlockFace.EAST);
																}
																else if(blockTop.getRelative(BlockFace.SOUTH_EAST).getBlockData() instanceof org.bukkit.block.data.type.Bed)
																{
																	bed = blockTop.getRelative(BlockFace.SOUTH_EAST);
																}
																else if(blockTop.getRelative(BlockFace.SOUTH).getBlockData() instanceof org.bukkit.block.data.type.Bed)
																{
																	bed = blockTop.getRelative(BlockFace.SOUTH);
																}
																else if(blockTop.getRelative(BlockFace.SOUTH_WEST).getBlockData() instanceof org.bukkit.block.data.type.Bed)
																{
																	bed = blockTop.getRelative(BlockFace.SOUTH_WEST);
																}
																else if(blockTop.getRelative(BlockFace.WEST).getBlockData() instanceof org.bukkit.block.data.type.Bed)
																{
																	bed = blockTop.getRelative(BlockFace.WEST);
																}
																else if(blockTop.getRelative(BlockFace.NORTH_WEST).getBlockData() instanceof org.bukkit.block.data.type.Bed)
																{
																	bed = blockTop.getRelative(BlockFace.NORTH_WEST);
																}
																else
																{
																	blockBot = bedSpawnBlock.getRelative(BlockFace.DOWN);
																	if(blockBot.getRelative(BlockFace.NORTH).getBlockData() instanceof org.bukkit.block.data.type.Bed)
																	{
																		bed = blockBot.getRelative(BlockFace.NORTH);
																	}
																	else if(blockBot.getRelative(BlockFace.NORTH_EAST).getBlockData() instanceof org.bukkit.block.data.type.Bed)
																	{
																		bed = blockBot.getRelative(BlockFace.NORTH_EAST);
																	}
																	else if(blockBot.getRelative(BlockFace.EAST).getBlockData() instanceof org.bukkit.block.data.type.Bed)
																	{
																		bed = blockBot.getRelative(BlockFace.EAST);
																	}
																	else if(blockBot.getRelative(BlockFace.SOUTH_EAST).getBlockData() instanceof org.bukkit.block.data.type.Bed)
																	{
																		bed = blockBot.getRelative(BlockFace.SOUTH_EAST);
																	}
																	else if(blockBot.getRelative(BlockFace.SOUTH).getBlockData() instanceof org.bukkit.block.data.type.Bed)
																	{
																		bed = blockBot.getRelative(BlockFace.SOUTH);
																	}
																	else if(blockBot.getRelative(BlockFace.SOUTH_WEST).getBlockData() instanceof org.bukkit.block.data.type.Bed)
																	{
																		bed = blockBot.getRelative(BlockFace.SOUTH_WEST);
																	}
																	else if(blockBot.getRelative(BlockFace.WEST).getBlockData() instanceof org.bukkit.block.data.type.Bed)
																	{
																		bed = blockBot.getRelative(BlockFace.WEST);
																	}
																	else if(blockBot.getRelative(BlockFace.NORTH_WEST).getBlockData() instanceof org.bukkit.block.data.type.Bed)
																	{
																		bed = blockBot.getRelative(BlockFace.NORTH_WEST);
																	}
																}
															}
															if(bed != null)
															{
																String server = whichServer(bed.getWorld().getName());
																String name = "bed";
																String world = bed.getWorld().getName();
																double x = CoordTask.BedNegateAdjust(bed.getX());
																double y = bed.getY();
																double z = CoordTask.BedNegateAdjust(bed.getZ());
																float pitch = 0;
																float yaw = 0;
																
																homes.putIfAbsent(name, new Home(-1, -1, server, name, world, x, y, z, pitch, yaw));
															}
														}
													} 
												}
												
												if(homes.size() > 0)
												{
													nbHome += homes.size();
													
													req = "INSERT INTO " + NKhome.table.get("homes") + " ( player_id, server, name, world, x, y, z, pitch, yaw) VALUES ";
													for(Home home : homes.values())
													{
														req = req 
															+"( " 
															+ id + " , '" 
															+ home.getServer() + "' , '" 
															+ home.getName() + "' , '" 
															+ home.getWorld() + "' , " 
															+ home.getX() + " , " 
															+ home.getY() + " , " 
															+ home.getZ() + " , " 
															+ home.getPitch() + " , " 
															+ home.getYaw() 
															+ " ),";
													}
													req = req.substring(0, req.length() - 1);
													req += " ON DUPLICATE KEY UPDATE server = VALUES(server), world = VALUES(world), x = VALUES(x), y = VALUES(y), z = VALUES(z), pitch = VALUES(pitch), yaw = VALUES(yaw)";
													ps = bdd.prepareStatement(req);	   
													ps.executeUpdate();
													ps.close();
												}
											}
											
											ok = true;
											//sender.sendMessage("" + ChatColor.GREEN + uuid);
										}
									}
								}
							}
							
							if(!ok)
							{
								ignoredFile++;
							}
							traitedFile++;
							if(traitedFile % 50 == 0)
							{
								sender.sendMessage("" + ChatColor.GREEN + traitedFile + "/" + files.size() + " fichier(s) de donnees traites");
							}
						}
					}
					catch (SQLException e) 
					{
						NKhome.getInstance().getConsole().sendMessage(ChatColor.DARK_RED + NKhome.PName + " Error while getting all players. (Error#cmd.ConvertEssentialsHomesCmd.000)");
						e.printStackTrace();
					}	
					sender.sendMessage("" + ChatColor.GREEN + traitedFile + "/" + files.size() + " fichier(s) de donnees traites.");
					sender.sendMessage("" + ChatColor.GREEN + "Importation terminee ! ("+ nbHome + " home(s) importes | " + ignoredFile + " fichier(s) ignores)");
				}
			}.runTaskAsynchronously(NKhome.getInstance());
		}
		else
		{
			sender.sendMessage(ChatColor.RED + "Le dossier 'userdata' d'Essentials est introuvable. Les homes des joueurs sont generalement stockes dans ce dossier. Verifiez que ce dossier ce trouve dans 'plugins/Essentials/'");
		}
		
	}
	
	public String whichServer(String world)
	{
		if(NKhome.convertGroup.containsKey(world))
		{
			return NKhome.convertGroup.get(world);
		}
		else
		{
			return NKhome.serverName;
		}
	}
}
