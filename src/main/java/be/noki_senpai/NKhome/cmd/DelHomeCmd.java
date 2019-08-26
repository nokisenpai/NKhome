package be.noki_senpai.NKhome.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import be.noki_senpai.NKhome.managers.HomeManager;
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
	private HomeManager homeManager = null;

	public DelHomeCmd(HomeManager homeManager)
	{
		this.homeManager = homeManager;
	}

	@Override public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args)
	{
		// Command called by a player
		if(sender instanceof Player)
		{
			String playerName = null;
			String homeName = null;
			Home home = null;
			if(!hasDelHomePermissions(sender))
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

				if(!CheckType.isAlphaNumeric(args[0]) && args[0].contains(":"))
				{
					if(hasDelHomeOtherPermissions(sender))
					{
						String[] homeArgs = args[0].split(":");
						if(homeArgs.length >= 2)
						{
							playerName = homeArgs[0];
							homeName = homeArgs[1];
						}
						else
						{
							sender.sendMessage(ChatColor.RED + " Vérifiez la syntaxe de la commande.");
							return true;
						}
					}
					else
					{
						sender.sendMessage(ChatColor.RED + " Vous n'avez pas la permission de supprimer le home d'un autre joueur !");
						return true;
					}
				}
				else
				{
					playerName = sender.getName();
					homeName = args[0];
				}
				home = homeManager.getHome(playerName, homeName);
				if(home == null)
				{
					sender.sendMessage(ChatColor.RED + " Ce home n'existe pas.");
				}
				else
				{
					homeManager.delHome(playerName, home.getName());
					sender.sendMessage(ChatColor.GREEN + " Votre home '" + home.getName() + "' a été supprimé.");
				}
			}
		}

		// Command called by Console
		if(sender instanceof ConsoleCommandSender)
		{
			sender.sendMessage(ChatColor.RED + " Vous ne pouvez pas utiliser cette commande dans la console.");
			return true;
		}

		return true;
	}

	private boolean hasDelHomePermissions(CommandSender sender)
	{
		if(sender.hasPermission("*") || sender.hasPermission("nkhome.*") || sender.hasPermission("nkhome.delhome")
				|| sender.hasPermission("nkhome.user") || sender.hasPermission("nkhome.admin"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	private boolean hasDelHomeOtherPermissions(CommandSender sender)
	{
		if(sender.hasPermission("*") || sender.hasPermission("nkhome.*") || sender.hasPermission("nkhome.delhome.other")
				|| sender.hasPermission("nkhome.admin"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
