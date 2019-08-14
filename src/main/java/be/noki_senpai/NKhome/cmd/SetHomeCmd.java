package be.noki_senpai.NKhome.cmd;

import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import be.noki_senpai.NKhome.NKhome;
import be.noki_senpai.NKhome.utils.CheckType;

public class SetHomeCmd implements CommandExecutor
{
	
	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) 
	{
		int maxHome = 0;
		// Command called by a player
		if (sender instanceof Player) 
		{
			if(!(sender.hasPermission("*") || sender.hasPermission("nkhome.*") || sender.hasPermission("nkhome.sethome") || sender.hasPermission("nkhome.user") || sender.hasPermission("nkhome.admin")))
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
				
				args[0] = args[0].toLowerCase();
				
				// Check valid home name
				if(!CheckType.isAlphaNumeric(args[0]) || args[0].equals("bed") || CheckType.isNumber(args[0]))
				{
					sender.sendMessage(ChatColor.RED + " '" + args[0] + "' n'est pas un nom de home valide.");
					return true;
				}
				
				if(NKhome.players.get(sender.getName()).getHomes().containsKey(args[0]))
				{
					NKhome.updateHome(sender.getName(), args[0], ((Player) sender).getLocation());
					sender.sendMessage(ChatColor.GREEN + " Votre home '" + args[0] + "' a été mis à jour.");
					return true;
				}
				
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
				
				if(maxHome <= NKhome.players.get(sender.getName()).getCpt())
				{
					sender.sendMessage(ChatColor.RED + " Vous ne pouvez pas faire plus de " + ChatColor.BOLD + maxHome + ChatColor.RESET + ChatColor.RED + " home(s).");
					return true;
					
				}
				else
				{
					NKhome.setHome(sender.getName(), args[0], ((Player) sender).getLocation());
					sender.sendMessage(ChatColor.GREEN + " Votre home '" + args[0] + "' a été créé.");
					return true;
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
