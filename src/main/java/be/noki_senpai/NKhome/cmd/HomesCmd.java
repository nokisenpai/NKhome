package be.noki_senpai.NKhome.cmd;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import be.noki_senpai.NKhome.data.NKPlayer;
import be.noki_senpai.NKhome.managers.ConfigManager;
import be.noki_senpai.NKhome.managers.HomeManager;
import be.noki_senpai.NKhome.managers.QueueManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import be.noki_senpai.NKhome.NKhome;
import be.noki_senpai.NKhome.data.Home;
import org.bukkit.scheduler.BukkitRunnable;

public class HomesCmd implements CommandExecutor
{
	private HomeManager homeManager = null;
	private ConfigManager configManager = null;
	private QueueManager queueManager = null;

	public HomesCmd(HomeManager homeManager, ConfigManager configManager, QueueManager queueManager)
	{
		this.homeManager = homeManager;
		this.configManager = configManager;
		this.queueManager = queueManager;
	}

	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) 
	{	
		int maxHome = 0;
		// Command called by a player
		if (sender instanceof Player) 
		{
			if(!hasHomesPermissions(sender))
			{
				// Send that the player does not have the permission
				sender.sendMessage(ChatColor.RED + " Vous n'avez pas la permission !");
				return true;
			}
			else
			{
				String homeList = ChatColor.GREEN + "---- Liste des homes de ";
				String tmpHomes = "";
				String playerName = null;

				//if no argument - display sender homes
				if(args.length == 0)
				{
					playerName = sender.getName();
					NKPlayer player = homeManager.getPlayer(playerName);
					homeList += playerName + " (" + player.getCpt() + "/";
					
					maxHome = getMaxHome(sender);
					maxHome += player.getHomeBonus();
					
					homeList += maxHome + ") ----";
					int cpt = 1;
					for (Entry<String, Home> entry : homeManager.getHomes(playerName).entrySet())
					{
						if(entry.getValue().getName().equals("bed"))
						{
							tmpHomes += "\n" + ChatColor.GREEN + "~ bed - " + ChatColor.AQUA + entry.getValue().getServer()
									+ " [" + entry.getValue().getWorld() + "] [ " + (int)entry.getValue().getX() + " / " + (int)entry.getValue().getY() + " / " + (int)entry.getValue().getZ() + " ]";
							continue;
						}
						if(entry.getValue().getCpt() > maxHome)
						{
							tmpHomes += "\n" + ChatColor.GREEN + ChatColor.STRIKETHROUGH + cpt + ". " + entry.getKey() + " - " + ChatColor.AQUA + entry.getValue().getServer()
									+ " [" + entry.getValue().getWorld() + "] [ " + (int)entry.getValue().getX() + " / " + (int)entry.getValue().getY() + " / " + (int)entry.getValue().getZ() + " ]"
									+ ChatColor.RESET;
							cpt++;
						}
						else
						{
							tmpHomes += "\n" + ChatColor.GREEN + cpt + ". " + entry.getKey() + " - " + ChatColor.AQUA + entry.getValue().getServer()
									+ " [" + entry.getValue().getWorld() + "] [ " + (int)entry.getValue().getX() + " / " + (int)entry.getValue().getY() + " / " + (int)entry.getValue().getZ() + " ]";
							cpt++;
						}
					}
					if(tmpHomes.equals(""))
					{
						sender.sendMessage(ChatColor.RED + " Vous n'avez pas de home.");
						return true;
					}
					else
					{
						sender.sendMessage(homeList + tmpHomes);
						return true;
					}
				}
				// Sender want display homes from other player
				else
				{
					if(!(hasHomesOtherPermissions(sender)))
					{
						// Send that the player does not have the permission
						sender.sendMessage(ChatColor.RED + " Vous n'avez pas la permission !");
						return true;
					}
					else
					{
						String finalHomeList = homeList;
						queueManager.addToQueue(new Function()
						{
							@Override public Object apply(Object o)
							{
								Map<String, Home> homes = homeManager.getHomes(args[0]);

								new BukkitRunnable()
								{
									@Override public void run()
									{
										String homeList = finalHomeList;
										String tmpHomes = "";
										homeList += args[0] + " ----";
										int cpt = 1;
										for (Entry<String, Home> entry : homes.entrySet())
										{
											if(!entry.getKey().equals("bed"))
											{
												tmpHomes += "\n" + ChatColor.GREEN + cpt + ". " + entry.getKey() + " - " + ChatColor.AQUA + entry.getValue().getServer()
														+ " [" + entry.getValue().getWorld() + "] [ " + (int)entry.getValue().getX() + " / " + (int)entry.getValue().getY() + " / " + (int)entry.getValue().getZ() + " ]";
												cpt++;
											}
											else
											{
												tmpHomes += "\n" + ChatColor.GREEN + "~ bed - " + ChatColor.AQUA + entry.getValue().getServer()
														+ " [" + entry.getValue().getWorld() + "] [ " + (int)entry.getValue().getX() + " / " + (int)entry.getValue().getY() + " / " + (int)entry.getValue().getZ() + " ]";
											}
										}

										if(tmpHomes.equals(""))
										{
											sender.sendMessage(ChatColor.RED + " Ce joueur n'a pas de home.");
										}
										else
										{
											sender.sendMessage(homeList + tmpHomes);
										}
									}
								}.runTask(NKhome.getPlugin());

								return null;
							}
						});






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
				sender.sendMessage(ChatColor.RED + " Vous devez spécifier le nom d'un joueur.");
				return true;
			}
			else
			{
				String finalHomeList = homeList;
				queueManager.addToQueue(new Function()
				{
					@Override public Object apply(Object o)
					{
						Map<String, Home> homes = homeManager.getHomes(args[0]);

						new BukkitRunnable()
						{
							@Override public void run()
							{
								String homeList = finalHomeList;
								String tmpHomes = "";
								homeList += args[0] + " ----";
								int cpt = 1;
								for (Entry<String, Home> entry : homes.entrySet())
								{
									if(!entry.getKey().equals("bed"))
									{
										tmpHomes += "\n" + ChatColor.GREEN + cpt + ". " + entry.getKey() + " - " + ChatColor.AQUA + entry.getValue().getServer()
												+ " [" + entry.getValue().getWorld() + "] [ " + (int)entry.getValue().getX() + " / " + (int)entry.getValue().getY() + " / " + (int)entry.getValue().getZ() + " ]";
										cpt++;
									}
									else
									{
										tmpHomes += "\n" + ChatColor.GREEN + "~ bed - " + ChatColor.AQUA + entry.getValue().getServer()
												+ " [" + entry.getValue().getWorld() + "] [ " + (int)entry.getValue().getX() + " / " + (int)entry.getValue().getY() + " / " + (int)entry.getValue().getZ() + " ]";
									}
								}

								if(tmpHomes.equals(""))
								{
									sender.sendMessage(ChatColor.RED + " Ce joueur n'a pas de home.");
								}
								else
								{
									sender.sendMessage(homeList + tmpHomes);
								}
							}
						}.runTask(NKhome.getPlugin());

						return null;
					}
				});
			}
		}
		
		
		return true;
	}

	// Get the max home that this player can have
	private int getMaxHome(CommandSender sender)
	{
		int maxHome = 0;
		if(hasAdminRankPermissions(sender))
		{
			maxHome = 100;
		}
		else
		{
			for (Entry<String, Integer> entry : configManager.getRanks().entrySet())
			{
				if(sender.hasPermission("nkhome.rank." + entry.getKey()) && entry.getValue() > maxHome)
				{
					maxHome = entry.getValue();
				}
			}
		}
		return maxHome;
	}

	private boolean hasHomesPermissions(CommandSender sender)
	{
		return sender.hasPermission("*") || sender.hasPermission("nkhome.*") || sender.hasPermission("nkhome.homes") || sender.hasPermission("nkhome.user") || sender.hasPermission("nkhome.admin");
	}

	private boolean hasHomesOtherPermissions(CommandSender sender)
	{
		return sender.hasPermission("*") || sender.hasPermission("nkhome.*") || sender.hasPermission("nkhome.homes.other") || sender.hasPermission("nkhome.admin");
	}

	private boolean hasAdminRankPermissions(CommandSender sender)
	{
		return sender.hasPermission("*") || sender.hasPermission("nkhome.*") || sender.hasPermission("nkhome.rank.*");
	}
}
