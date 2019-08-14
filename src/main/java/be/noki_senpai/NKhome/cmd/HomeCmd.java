package be.noki_senpai.NKhome.cmd;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import be.noki_senpai.NKhome.NKhome;
import be.noki_senpai.NKhome.data.Home;
import be.noki_senpai.NKhome.utils.CheckType;
import be.noki_senpai.NKhome.utils.CoordTask;

public class HomeCmd implements CommandExecutor
{
	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) 
	{
		// Command called by a player
		if (sender instanceof Player) 
		{
			if(!(sender.hasPermission("*") || sender.hasPermission("nkhome.*") || sender.hasPermission("nkhome.home") || sender.hasPermission("nkhome.user") || sender.hasPermission("nkhome.admin")))
			{
				// Send that the player does not have the permission
				sender.sendMessage(ChatColor.RED + " Vous n'avez pas la permission !");
				return true;
			}
			else
			{
				Home home = null;
				boolean admin = false;
				
				//if no argument
				if(args.length == 0)
				{
					List<Home> homeList = new ArrayList<Home>(NKhome.players.get(sender.getName()).getHomes().values());
					if(homeList.size() != 0)
					{
						home = homeList.get(0);
					}
				}
				else
				{
					args[0] = args[0].toLowerCase();
					
					if(!CheckType.isAlphaNumeric(args[0]))
					{
						if(args[0].contains(":") && (sender.hasPermission("*") || sender.hasPermission("nkhome.*") || sender.hasPermission("nkhome.home.other") || sender.hasPermission("nkhome.admin")))
						{
							String[] homeArgs = args[0].split(":");
							if(homeArgs.length >= 2)
							{
								if(NKhome.players.containsKey(homeArgs[0]))
								{
									if(NKhome.players.get(homeArgs[0]).getHomes().containsKey(homeArgs[1]))
									{
										home = NKhome.players.get(homeArgs[0]).getHomes().get(homeArgs[1]);
									}
									
									else if(homeArgs[1].equals("bed"))
									{
										home = NKhome.players.get(homeArgs[0]).getBed();
										admin= true;
									}
									
									else if(CheckType.isNumber(homeArgs[1]))
									{
										List<Home> homeList = new ArrayList<Home>(NKhome.players.get(homeArgs[0]).getHomes().values());
										if(homeList.size() >= Integer.parseInt(homeArgs[1]))
										{
											home = homeList.get(Integer.parseInt(homeArgs[1])-1);
										}
									}
								}
								else
								{
									LinkedHashMap<String, Home> playerHomes = NKhome.getPlayerHomes(homeArgs[0]);
									if(playerHomes.containsKey(homeArgs[1]))
									{
										home = playerHomes.get(homeArgs[1]);
									}
									
									else if(CheckType.isNumber(homeArgs[1]))
									{
										List<Home> homeList = new ArrayList<Home>(playerHomes.values());
										if(homeList.size() >= Integer.parseInt(homeArgs[1]))
										{
											home = homeList.get(Integer.parseInt(homeArgs[1])-1);
										}
									}
								}
							}
						}
						else
						{
							sender.sendMessage(ChatColor.RED + " Vous n'avez pas la permission de vous tp au home d'un autre joueur !");
							return true;
						}
					}
					
					else if(NKhome.players.get(sender.getName()).getHomes().containsKey(args[0]))
					{
						home = NKhome.players.get(sender.getName()).getHomes().get(args[0]);
					}
					
					else if(args[0].equals("bed"))
					{
						home = NKhome.players.get(sender.getName()).getBed();
					}
					
					else if(CheckType.isNumber(args[0]))
					{
						List<Home> homeList = new ArrayList<Home>(NKhome.players.get(sender.getName()).getHomes().values());
						if(homeList.size() >= Integer.parseInt(args[0]))
						{
							home = homeList.get(Integer.parseInt(args[0])-1);
						}
					}
				}
				
				
				
				
				if(home==null)
				{
					sender.sendMessage(ChatColor.RED + " Ce home n'existe pas.");
					return true;
				}
				else
				{
					if(home.getServer().equals(NKhome.serverName))
					{
						if(home.getName().equals("bed") && !(NKhome.getInstance().getServer().getWorld(home.getWorld()).getBlockAt(CoordTask.BlockCoord(home.getX()), (int)home.getY(), CoordTask.BlockCoord(home.getZ())).getBlockData() instanceof org.bukkit.block.data.type.Bed))
						{
							if(admin)
							{
								sender.sendMessage(ChatColor.RED + " Le lit de ce joueur n'existe plus.");
							}
							else
							{
								sender.sendMessage(ChatColor.RED + " Votre lit n'existe plus.");
								NKhome.players.get(sender.getName()).delBed();
								NKhome.delHome(home.getId());
							}
						}
						else
						{
							Location safeLocation = null;
							String worldName = home.getWorld();
							double x = CoordTask.roundFive(home.getX());
							double y = home.getY();
							double z = CoordTask.roundFive(home.getZ());
							float yaw = home.getYaw();
							float pitch = home.getPitch();
							

							if(home.getName().equals("bed"))
							{
								safeLocation = NKhome.safeBedLocation(worldName, x, y, z, yaw, pitch);
							}
							else
							{
								safeLocation = NKhome.safeLocation(worldName, x, y, z, yaw, pitch);
							}
							
							if(safeLocation == null)
							{
								sender.sendMessage(ChatColor.RED + "Ce home est obstrué. Par sécurité vous n'avez pas été téléporté.");
							}
							else
							{
								((Player) sender).teleport(safeLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
							}
						}
						return true;
					}
					else
					{
						NKhome.setTpHome(NKhome.players.get(sender.getName()).getId(), home.getId());

						ByteArrayDataOutput out = ByteStreams.newDataOutput();
						out.writeUTF("Connect");
						out.writeUTF(home.getServer());

						((Player) sender).sendPluginMessage(NKhome.getInstance(), "BungeeCord", out.toByteArray());
						
						return true;
					}
				}
				
			}
		}
		
		
		// Command called by Console
		if (sender instanceof ConsoleCommandSender)
		{
			sender.sendMessage(ChatColor.RED + " Vous ne pouvez pas utiliser cette commande dans la console.");
			return true;
		}
		
		
		return true;
	}
}
