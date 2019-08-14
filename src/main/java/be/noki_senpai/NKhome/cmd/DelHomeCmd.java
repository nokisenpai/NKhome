package be.noki_senpai.NKhome.cmd;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import be.noki_senpai.NKhome.NKhome;
import be.noki_senpai.NKhome.data.Home;
import be.noki_senpai.NKhome.utils.CheckType;

public class DelHomeCmd implements CommandExecutor
{
	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) 
	{
		// Command called by a player
		if (sender instanceof Player) 
		{
			Home homeDel = null;
			if(!(sender.hasPermission("*") || sender.hasPermission("nkhome.*") || sender.hasPermission("nkhome.delhome") || sender.hasPermission("nkhome.user") || sender.hasPermission("nkhome.admin")))
			{
				// Send that the player does not have the permission
				sender.sendMessage(ChatColor.RED + " Vous n'avez pas la permission !");
				return true;
			}
			else
			{
				//if no argument
				if(args.length == 0)
				{
					sender.sendMessage(ChatColor.RED + " Vous devez spécifier un nom de home.");
					return true;
				}
				else
				{
					args[0] = args[0].toLowerCase();
					
					if(!CheckType.isAlphaNumeric(args[0]))
					{
						if(args[0].contains(":") && (sender.hasPermission("*") || sender.hasPermission("nkhome.*") || sender.hasPermission("nkhome.delhome.other") || sender.hasPermission("nkhome.admin")))
						{
							String[] homeArgs = args[0].split(":");
							if(homeArgs.length >= 2)
							{
								if(NKhome.players.containsKey(homeArgs[0]))
								{
									if(NKhome.players.get(homeArgs[0]).getHomes().containsKey(homeArgs[1]))
									{
										homeDel = NKhome.players.get(homeArgs[0]).getHomes().get(homeArgs[1]);
										NKhome.players.get(homeArgs[0]).removeHome(homeArgs[1]);
									}
									
									else if(homeArgs[1].equals("bed"))
									{
										homeDel = NKhome.players.get(homeArgs[0]).getBed();
										NKhome.players.get(homeArgs[0]).delBed();
									}
									
									else if(CheckType.isNumber(homeArgs[1]))
									{
										List<Home> homeList = new ArrayList<Home>(NKhome.players.get(homeArgs[0]).getHomes().values());
										if(homeList.size() >= Integer.parseInt(homeArgs[1]))
										{
											homeDel = homeList.get(Integer.parseInt(homeArgs[1])-1);
											NKhome.players.get(homeArgs[0]).removeHome(homeList.get(Integer.parseInt(homeArgs[1])-1).getName());
										}
									}
								}
								else
								{
									LinkedHashMap<String, Home> playerHomes = NKhome.getPlayerHomes(homeArgs[0]);
									if(playerHomes.containsKey(homeArgs[1]))
									{
										homeDel = playerHomes.get(homeArgs[1]);
									}
									
									else if(CheckType.isNumber(homeArgs[1]))
									{
										List<Home> homeList = new ArrayList<Home>(playerHomes.values());
										if(homeList.size() >= Integer.parseInt(homeArgs[1]))
										{
											homeDel = homeList.get(Integer.parseInt(homeArgs[1])-1);
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
						homeDel = NKhome.players.get(sender.getName()).getHomes().get(args[0]);
						NKhome.players.get(sender.getName()).removeHome(args[0]);
					}
					
					else if(args[0].equals("bed"))
					{
						homeDel = NKhome.players.get(sender.getName()).getBed();
						NKhome.players.get(sender.getName()).delBed();
					}
					
					else if(CheckType.isNumber(args[0]))
					{
						List<Home> homeList = new ArrayList<Home>(NKhome.players.get(sender.getName()).getHomes().values());
						if(homeList.size() >= Integer.parseInt(args[0]))
						{
							NKhome.players.get(sender.getName()).removeHome(homeList.get(Integer.parseInt(args[0])-1).getName());
							homeDel = homeList.get(Integer.parseInt(args[0])-1);
						}
					}
				}
				if(homeDel == null)
				{
					sender.sendMessage(ChatColor.RED + " Ce home n'existe pas.");
				}
				else
				{
					sender.sendMessage(ChatColor.GREEN + " Votre home '" + homeDel.getName() + "' a été supprimé.");
					NKhome.delHome(homeDel.getId());
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
