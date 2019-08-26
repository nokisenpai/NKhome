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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
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
import org.bukkit.scheduler.BukkitRunnable;

import be.noki_senpai.NKhome.NKhome;
import be.noki_senpai.NKhome.data.Home;
import be.noki_senpai.NKhome.managers.ConfigManager;
import be.noki_senpai.NKhome.managers.DatabaseManager;
import be.noki_senpai.NKhome.utils.CoordTask;

public class ConvertEssentialsHomesCmd implements CommandExecutor
{
	ConfigManager configManager = null;
	private ConsoleCommandSender console = null;

	public ConvertEssentialsHomesCmd(ConfigManager configManager)
	{
		this.configManager = configManager;
		this.console = Bukkit.getConsoleSender();
	}

	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args)
	{
		if(hasConvertEssentialsHomesPermissions(sender))
		{
			convertEssentialsHomes(sender);
		}
		else
		{
			// Send that the player does not have the permission
			sender.sendMessage(ChatColor.RED + " Vous n'avez pas la permission !");
		}
		return true;
	}

	public void convertEssentialsHomes(CommandSender sender)
	{
		Path path = Paths.get("plugins/Essentials/userdata");
		if(Files.exists(path))
		{
			// Get all data files
			File f = new File(path.toString());
			List<File> files = new ArrayList<File>(Arrays.asList(f.listFiles()));

			sender.sendMessage("" + ChatColor.GREEN + files.size() + " fichier(s) de données a traiter. Cette operation peut durer un moment.");

			// For each data file
			new BukkitRunnable()
			{
				@Override
				public void run()
				{
					int traitedFile = 0;
					int ignoredFile = 0;
					int nbHome = 0;
					Map<String, Integer> playerList = new HashMap<String, Integer>();

					try
					{
						Connection bdd = null;
						ResultSet resultat = null;
						PreparedStatement ps = null;
						String req = null;

						bdd = DatabaseManager.getConnection();

						// Get all current players stored on database
						req = "SELECT id, uuid FROM " + DatabaseManager.table.get("players");
						ps = bdd.prepareStatement(req);
						resultat = ps.executeQuery();

						while(resultat.next())
						{
							playerList.put(resultat.getString("uuid"), resultat.getInt("id"));
						}
						ps.close();
						resultat.close();

						// Browse each Essentials file from userdata directory
						for(int i = 0 ; i < files.size() ; i++)
						{
							if(traitedFile != 0 && traitedFile % 50 == 0)
							{
								sender.sendMessage("" + ChatColor.GREEN + traitedFile + "/" + files.size() + " fichier(s) de données traités");
							}

							traitedFile++;

							// *******************************
							// Files operation
							// *******************************

							File playerFile = files.get(i);

							// Check if file exist
							if(playerFile == null)
							{
								ignoredFile++;
								continue;
							}

							// Get player uuid from data file name
							UUID uuid = (UUID.fromString(playerFile.getName().replace(".yml", "")));

							// Check if this is a valid uuid
							if(uuid == null)
							{
								ignoredFile++;
								continue;
							}

							// Load data file as FileConfiguration
							FileConfiguration essentialsHomes = YamlConfiguration.loadConfiguration(playerFile);

							// If file does not have keys
							if(essentialsHomes.getKeys(false).size() == 0)
							{
								ignoredFile++;
								continue;
							}

							// *******************************
							// Saving players operation
							// *******************************

							String playerName = essentialsHomes.getString("lastAccountName");

							// Check if playerName is null
							if(playerName == null)
							{
								// Get playerName from Mojang
								playerName = Bukkit.getServer().getOfflinePlayer(uuid).getName();
							}

							// Check if playerName is still null
							if(playerName == null)
							{
								ignoredFile++;
								continue;
							}

							int id = -1;

							// If player is already in database, get his id
							if(playerList.containsKey(uuid.toString()))
							{
								id = playerList.get(uuid.toString());
							}
							else
							{
								// Insert this player on database
								try
								{
									req = "INSERT INTO " + DatabaseManager.table.get("players")
											+ " ( uuid, name) VALUES ( ? , ? ) ON DUPLICATE KEY UPDATE name = name";
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
								catch(SQLException e)
								{
									console.sendMessage(ChatColor.DARK_RED + NKhome.PNAME
											+ " Error while setting a player. (Error#cmd.ConvertEssentialsHomesCmd.000)");
								}
							}

							// *******************************
							// Saving homes operation
							// *******************************

							Map<String, Home> homes = new HashMap<String, Home>();
							ConfigurationSection configHomes = essentialsHomes.getConfigurationSection("homes");

							// If there are homes in file
							if(configHomes != null)
							{
								// Get each homes
								for(String key : configHomes.getKeys(false))
								{
									ConfigurationSection configHome = essentialsHomes.getConfigurationSection("homes." + key);
									if(!configManager.getConvertIgnore().contains(configHome.getString("world")))
									{
										String server = whichServer(configHome.getString("world"));
										String name = key;
										String world = configHome.getString("world");
										double x = configHome.getDouble("x");
										double y = configHome.getDouble("y");
										double z = configHome.getDouble("z");
										float pitch = (float) configHome.getDouble("pitch");
										float yaw = (float) configHome.getDouble("yaw");

										homes.put(key, new Home(-1, -1, server, name, world, x, y, z, pitch, yaw));
									}
								}
							}

							// *******************************
							// Saving bed homes operation
							// *******************************

							// Get bed spawn location from server
							Location bedSpawnLocation = Bukkit.getServer().getOfflinePlayer(uuid).getBedSpawnLocation();

							// Check if there are a bed spawn location
							if(bedSpawnLocation != null)
							{
								World bedWorld = null;

								// Check if bed world is loaded
								if(bedSpawnLocation.isWorldLoaded())
								{
									bedWorld = bedSpawnLocation.getWorld();
								}

								// Check if bed world exist
								if(bedWorld != null && !configManager.getConvertIgnore().contains(bedWorld.getName()))
								{
									// Get bed spawn block
									Block bedSpawnBlock = bedWorld.getBlockAt((int) CoordTask.BedNegateAdjust(bedSpawnLocation.getX()), (int) bedSpawnLocation.getY(), (int) CoordTask.BedNegateAdjust(bedSpawnLocation.getZ()));

									// Check if bed spawn block exist
									if(bedSpawnBlock != null)
									{
										// Check if there is a bed block around this location
										Block bed = null;
										Block blockTop = null;
										Block blockBot = null;
										if(bedSpawnBlock.getRelative(BlockFace.NORTH).getBlockData() instanceof org.bukkit.block.data.type.Bed)
											bed = bedSpawnBlock.getRelative(BlockFace.NORTH);
										else if(bedSpawnBlock.getRelative(BlockFace.NORTH_EAST).getBlockData() instanceof org.bukkit.block.data.type.Bed)
											bed = bedSpawnBlock.getRelative(BlockFace.NORTH_EAST);
										else if(bedSpawnBlock.getRelative(BlockFace.EAST).getBlockData() instanceof org.bukkit.block.data.type.Bed)
											bed = bedSpawnBlock.getRelative(BlockFace.EAST);
										else if(bedSpawnBlock.getRelative(BlockFace.SOUTH_EAST).getBlockData() instanceof org.bukkit.block.data.type.Bed)
											bed = bedSpawnBlock.getRelative(BlockFace.SOUTH_EAST);
										else if(bedSpawnBlock.getRelative(BlockFace.SOUTH).getBlockData() instanceof org.bukkit.block.data.type.Bed)
											bed = bedSpawnBlock.getRelative(BlockFace.SOUTH);
										else if(bedSpawnBlock.getRelative(BlockFace.SOUTH_WEST).getBlockData() instanceof org.bukkit.block.data.type.Bed)
											bed = bedSpawnBlock.getRelative(BlockFace.SOUTH_WEST);
										else if(bedSpawnBlock.getRelative(BlockFace.WEST).getBlockData() instanceof org.bukkit.block.data.type.Bed)
											bed = bedSpawnBlock.getRelative(BlockFace.WEST);
										else if(bedSpawnBlock.getRelative(BlockFace.NORTH_WEST).getBlockData() instanceof org.bukkit.block.data.type.Bed)
											bed = bedSpawnBlock.getRelative(BlockFace.NORTH_WEST);
										else
										{
											blockTop = bedSpawnBlock.getRelative(BlockFace.UP);
											if(blockTop.getRelative(BlockFace.NORTH).getBlockData() instanceof org.bukkit.block.data.type.Bed)
												bed = blockTop.getRelative(BlockFace.NORTH);
											else if(blockTop.getRelative(BlockFace.NORTH_EAST).getBlockData() instanceof org.bukkit.block.data.type.Bed)
												bed = blockTop.getRelative(BlockFace.NORTH_EAST);
											else if(blockTop.getRelative(BlockFace.EAST).getBlockData() instanceof org.bukkit.block.data.type.Bed)
												bed = blockTop.getRelative(BlockFace.EAST);
											else if(blockTop.getRelative(BlockFace.SOUTH_EAST).getBlockData() instanceof org.bukkit.block.data.type.Bed)
												bed = blockTop.getRelative(BlockFace.SOUTH_EAST);
											else if(blockTop.getRelative(BlockFace.SOUTH).getBlockData() instanceof org.bukkit.block.data.type.Bed)
												bed = blockTop.getRelative(BlockFace.SOUTH);
											else if(blockTop.getRelative(BlockFace.SOUTH_WEST).getBlockData() instanceof org.bukkit.block.data.type.Bed)
												bed = blockTop.getRelative(BlockFace.SOUTH_WEST);
											else if(blockTop.getRelative(BlockFace.WEST).getBlockData() instanceof org.bukkit.block.data.type.Bed)
												bed = blockTop.getRelative(BlockFace.WEST);
											else if(blockTop.getRelative(BlockFace.NORTH_WEST).getBlockData() instanceof org.bukkit.block.data.type.Bed)
												bed = blockTop.getRelative(BlockFace.NORTH_WEST);
											else
											{
												blockBot = bedSpawnBlock.getRelative(BlockFace.DOWN);
												if(blockBot.getRelative(BlockFace.NORTH).getBlockData() instanceof org.bukkit.block.data.type.Bed)
													bed = blockBot.getRelative(BlockFace.NORTH);
												else if(blockBot.getRelative(BlockFace.NORTH_EAST).getBlockData() instanceof org.bukkit.block.data.type.Bed)
													bed = blockBot.getRelative(BlockFace.NORTH_EAST);
												else if(blockBot.getRelative(BlockFace.EAST).getBlockData() instanceof org.bukkit.block.data.type.Bed)
													bed = blockBot.getRelative(BlockFace.EAST);
												else if(blockBot.getRelative(BlockFace.SOUTH_EAST).getBlockData() instanceof org.bukkit.block.data.type.Bed)
													bed = blockBot.getRelative(BlockFace.SOUTH_EAST);
												else if(blockBot.getRelative(BlockFace.SOUTH).getBlockData() instanceof org.bukkit.block.data.type.Bed)
													bed = blockBot.getRelative(BlockFace.SOUTH);
												else if(blockBot.getRelative(BlockFace.SOUTH_WEST).getBlockData() instanceof org.bukkit.block.data.type.Bed)
													bed = blockBot.getRelative(BlockFace.SOUTH_WEST);
												else if(blockBot.getRelative(BlockFace.WEST).getBlockData() instanceof org.bukkit.block.data.type.Bed)
													bed = blockBot.getRelative(BlockFace.WEST);
												else if(blockBot.getRelative(BlockFace.NORTH_WEST).getBlockData() instanceof org.bukkit.block.data.type.Bed)
													bed = blockBot.getRelative(BlockFace.NORTH_WEST);
											}
										}

										// Check if a bed block has found
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

											homes.put(name, new Home(-1, -1, server, name, world, x, y, z, pitch, yaw));
										}
									}
								}
							}

							// Check if there is atleast one home found
							if(homes.size() > 0)
							{
								nbHome += homes.size();

								// Insert each home from this player on database
								req = "INSERT INTO " + DatabaseManager.table.get("homes")
										+ " ( player_id, server, name, world, x, y, z, pitch, yaw) VALUES ";
								for(Home home : homes.values())
								{
									req = req + "( " + id + " , '" + home.getServer() + "' , '" + home.getName() + "' , '" + home.getWorld() + "' , "
											+ home.getX() + " , " + home.getY() + " , " + home.getZ() + " , " + home.getPitch() + " , "
											+ home.getYaw() + " ),";
								}
								req = req.substring(0, req.length() - 1);
								// If a home already exist with same name we update it.
								req += " ON DUPLICATE KEY UPDATE server = VALUES(server), world = VALUES(world), x = VALUES(x), y = VALUES(y), z = VALUES(z), pitch = VALUES(pitch), yaw = VALUES(yaw)";
								ps = bdd.prepareStatement(req);
								ps.executeUpdate();
								ps.close();
							}
						}
					}
					catch(SQLException e)
					{
						console.sendMessage(ChatColor.DARK_RED + NKhome.PNAME
								+ " Error while getting all players. (Error#cmd.ConvertEssentialsHomesCmd.000)");
						e.printStackTrace();
					}
					sender.sendMessage("" + ChatColor.GREEN + traitedFile + "/" + files.size() + " fichier(s) de donnees traites.");
					sender.sendMessage("" + ChatColor.GREEN + "Importation terminée ! (" + nbHome + " home(s) importés | " + ignoredFile
							+ " fichier(s) ignores)");
				}
			}.runTaskAsynchronously(NKhome.getPlugin());

		}
		else
		{
			sender.sendMessage(ChatColor.RED
					+ "Le dossier 'userdata' d'Essentials est introuvable. Les homes des joueurs sont généralement stockés dans ce dossier. Vérifiez que ce dossier ce trouve dans 'plugins/Essentials/'");
		}

	}

	// For know which server name save for a home (depending server groups)
	public String whichServer(String world)
	{
		if(configManager.getConvertGroup().containsKey(world))
		{
			return configManager.getConvertGroup().get(world);
		}
		else
		{
			return ConfigManager.SERVERNAME;
		}
	}

	public boolean hasConvertEssentialsHomesPermissions(CommandSender sender)
	{
		return sender.hasPermission("*") || sender.hasPermission("nkhome.*") || sender.hasPermission("nkhome.convert")
				|| sender.hasPermission("nkhome.admin");
	}
}
