package be.noki_senpai.NKhome.cmd;

import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import be.noki_senpai.NKhome.NKhome;
import be.noki_senpai.NKhome.data.Home;

public class HomesCmd implements CommandExecutor
{
	
	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) 
	{	
		int maxHome = 0;
		// Command called by a player
		if (sender instanceof Player) 
		{
			if(!(sender.hasPermission("*") || sender.hasPermission("nkhome.*") || sender.hasPermission("nkhome.homes") || sender.hasPermission("nkhome.user") || sender.hasPermission("nkhome.admin")))
			{
				// Send that the player does not have the permission
				sender.sendMessage(ChatColor.RED + " Vous n'avez pas la permission !");
				return true;
			}
			else
			{
				String homeList = ChatColor.GREEN + "---- Liste des homes de ";
				//if no argument
				if(args.length == 0)
				{
					homeList += sender.getName() + " (" + NKhome.players.get(sender.getName()).getCpt() + "/";
					
					if(sender.hasPermission("*") || sender.hasPermission("nkhome.*") || sender.hasPermission("nkhome.rank.*"))
					{
						maxHome = 100;
					}
					else
					{
						for (Entry<String, Integer> entry : NKhome.ranks.entrySet()) 
						{
							if(sender.hasPermission("nkhome.rank." + entry.getKey()) && entry.getValue() > maxHome)
							{
								maxHome = entry.getValue();
							}
						}
					}
					maxHome += NKhome.players.get(sender.getName()).getHomeBonus();
					
					homeList += maxHome + ") ----";
					
					for (Entry<String, Home> entry : NKhome.players.get(sender.getName()).getHomes().entrySet()) 
					{
						if(entry.getValue().getCpt() > maxHome)
						{
							homeList += "\n" + ChatColor.GREEN + ChatColor.STRIKETHROUGH + entry.getValue().getCpt() + ". " + entry.getKey() + " - " + ChatColor.AQUA + entry.getValue().getServer() 
									+ " [" + entry.getValue().getWorld() + "] [ " + (int)entry.getValue().getX() + " / " + (int)entry.getValue().getY() + " / " + (int)entry.getValue().getZ() + " ]"
									+ ChatColor.RESET;
						}
						else
						{
							homeList += "\n" + ChatColor.GREEN + entry.getValue().getCpt() + ". " + entry.getKey() + " - " + ChatColor.AQUA + entry.getValue().getServer() 
									+ " [" + entry.getValue().getWorld() + "] [ " + (int)entry.getValue().getX() + " / " + (int)entry.getValue().getY() + " / " + (int)entry.getValue().getZ() + " ]";
						}
						
					}
					Home bed = NKhome.players.get(sender.getName()).getBed();
					if(bed!=null)
					{
						homeList += "\n" + ChatColor.GREEN + "~ bed - " + ChatColor.AQUA + bed.getServer() 
						+ " [" + bed.getWorld() + "] [ " + (int)bed.getX() + " / " + (int)bed.getY() + " / " + (int)bed.getZ() + " ]";
					}
					sender.sendMessage(homeList);
					return true;
				}
				else
				{
					if(!(sender.hasPermission("*") || sender.hasPermission("nkhome.*") || sender.hasPermission("nkhome.homes.other") || sender.hasPermission("nkhome.admin")))
					{
						// Send that the player does not have the permission
						sender.sendMessage(ChatColor.RED + " Vous n'avez pas la permission !");
						return true;
					}
					else
					{
						homeList += args[0] + " ----";
						String bed = "";
						int i = 1;
						for (Entry<String, Home> entry : NKhome.getPlayerHomes(args[0]).entrySet()) 
						{
							if(!entry.getKey().equals("bed"))
							{
								homeList += "\n" + ChatColor.GREEN + i + ". " + entry.getKey() + " - " + ChatColor.AQUA + entry.getValue().getServer() 
										+ " [" + entry.getValue().getWorld() + "] [ " + (int)entry.getValue().getX() + " / " + (int)entry.getValue().getY() + " / " + (int)entry.getValue().getZ() + " ]";
								i++;
							}
							else
							{
								bed = "\n" + ChatColor.GREEN + "~ bed - " + ChatColor.AQUA + entry.getValue().getServer() 
										+ " [" + entry.getValue().getWorld() + "] [ " + (int)entry.getValue().getX() + " / " + (int)entry.getValue().getY() + " / " + (int)entry.getValue().getZ() + " ]";
							}
						}
						if(i==1 && bed.equals(""))
						{
							sender.sendMessage(ChatColor.RED + " Ce joueur n'a pas de home.");
							return true;
						}
						else
						{
							homeList += bed;
							sender.sendMessage(homeList);
							return true;
						}
					}
				}
			}
		}
		
		
		// Command called by Console
		if (sender instanceof ConsoleCommandSender)
		{
			String homeList = ChatColor.GREEN + "\n---- Liste des homes de ";
			//if no argument
			if(args.length == 0)
			{
				sender.sendMessage(ChatColor.RED + " Vous devez specifier le nom d'un joueur.");
				return true;
			}
			else
			{
				homeList += args[0] + " ----";
				String bed = "";
				int i = 1;
				for (Entry<String, Home> entry : NKhome.getPlayerHomes(args[0]).entrySet()) 
				{
					if(!entry.getKey().equals("bed"))
					{
						homeList += "\n" + ChatColor.GREEN + i + ". " + entry.getKey() + " - " + ChatColor.AQUA + entry.getValue().getServer() 
								+ " [" + entry.getValue().getWorld() + "] [ " + (int)entry.getValue().getX() + " / " + (int)entry.getValue().getY() + " / " + (int)entry.getValue().getZ() + " ]";
						i++;
					}
					else
					{
						bed = "\n" + ChatColor.GREEN + "~ bed - " + ChatColor.AQUA + entry.getValue().getServer() 
								+ " [" + entry.getValue().getWorld() + "] [ " + (int)entry.getValue().getX() + " / " + (int)entry.getValue().getY() + " / " + (int)entry.getValue().getZ() + " ]";
					}
				}
				if(i==1 && bed.equals(""))
				{
					sender.sendMessage(ChatColor.RED + " Ce joueur n'a pas de home.");
					return true;
				}
				else
				{
					homeList += bed;
					sender.sendMessage(homeList);
					return true;
				}
			}
		}
		
		
		return true;
	}
}
