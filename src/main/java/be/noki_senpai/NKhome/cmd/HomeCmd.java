package be.noki_senpai.NKhome.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import be.noki_senpai.NKhome.managers.ConfigManager;
import be.noki_senpai.NKhome.managers.HomeManager;
import be.noki_senpai.NKhome.managers.QueueManager;
import org.bukkit.Bukkit;
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
import org.bukkit.scheduler.BukkitRunnable;

public class HomeCmd implements CommandExecutor
{
	private HomeManager homeManager = null;
	private QueueManager queueManager = null;

	public HomeCmd(HomeManager homeManager, QueueManager queueManager)
	{
		this.homeManager = homeManager;
		this.queueManager = queueManager;
	}

	@Override public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args)
	{
		String playerName = null;
		String homeName = null;
		// Command called by a player
		if(sender instanceof Player)
		{
			if(!hasHomePermissions(sender))
			{
				// Send that the player does not have the permission
				sender.sendMessage(ChatColor.RED + " Vous n'avez pas la permission !");
				return true;
			}
			else
			{
				boolean admin = false;

				//if no argument
				if(args.length == 0)
				{
					// Get the name of the first player's home
					List<Home> homeList = new ArrayList<Home>(homeManager.getHomes(sender.getName()).values());
					if(homeList.size() != 0)
					{
						playerName = sender.getName();
						homeName = homeList.get(0).getName();
					}
					else
					{
						sender.sendMessage(ChatColor.RED + " Vous n'avez pas de home");
						return true;
					}
				}
				else
				{
					args[0] = args[0].toLowerCase();

					if(!CheckType.isAlphaNumeric(args[0]) && args[0].contains(":"))
					{
						if(hasHomeOtherPermissions(sender))
						{
							String[] homeArgs = args[0].split(":");
							if(homeArgs.length >= 2)
							{
								playerName = homeArgs[0];
								homeName = homeArgs[1];
								admin = true;
							}
							else
							{
								sender.sendMessage(ChatColor.RED + " Vérifiez la syntaxe de la commande.");
								return true;
							}
						}
						else
						{
							sender.sendMessage(ChatColor.RED + " Vous n'avez pas la permission de vous tp au home d'un autre joueur !");
							return true;
						}
					}
					else
					{
						playerName = sender.getName();
						homeName = args[0];
					}
				}

				String finalPlayerName = playerName;
				String finalHomeName = homeName;
				boolean finalAdmin = admin;

				queueManager.addToQueue(new Function()
				{
					@Override public Object apply(Object o)
					{
						// Get target home
						Home home = homeManager.getHome(finalPlayerName, finalHomeName);
						if(home != null)
						{
							// Home is on other server
							if(!home.getServer().equals(ConfigManager.SERVERNAME))
							{
								homeManager.getPlayer(sender.getName()).setTpHome(home.getId());

								new BukkitRunnable()
								{
									@Override public void run()
									{
										// If home is not on this server
										if(!home.getServer().equals(ConfigManager.SERVERNAME))
										{
											ByteArrayDataOutput out = ByteStreams.newDataOutput();
											out.writeUTF("Connect");
											out.writeUTF(home.getServer());

											((Player) sender).sendPluginMessage(NKhome.getPlugin(), "BungeeCord", out.toByteArray());
										}
									}
								}.runTask(NKhome.getPlugin());

								return null;
							}

							// Home is on this server
							new BukkitRunnable()
							{
								@Override public void run()
								{
									if(home.getName().equals("bed")
											&& !(Bukkit.getServer().getWorld(home.getWorld()).getBlockAt(CoordTask.BlockCoord(home.getX()), (int) home.getY(), CoordTask.BlockCoord(home.getZ())).getBlockData() instanceof org.bukkit.block.data.type.Bed))
									{
										if(finalAdmin)
										{
											sender.sendMessage(ChatColor.RED + " Le lit de ce joueur n'existe plus.");
											return;
										}
										else
										{
											sender.sendMessage(ChatColor.RED + " Votre lit n'existe plus.");
											queueManager.addToQueue(new Function()
											{
												@Override public Object apply(Object o)
												{
													homeManager.delHome(sender.getName(), home);
													return null;
												}
											});
											return;
										}
									}

									Location safeLocation = null;
									String worldName = home.getWorld();
									double x = CoordTask.roundFive(home.getX());
									double y = home.getY();
									double z = CoordTask.roundFive(home.getZ());
									float yaw = home.getYaw();
									float pitch = home.getPitch();

									if(home.getName().equals("bed"))
									{
										safeLocation = CoordTask.safeBedLocation(worldName, x, y, z, yaw, pitch);
									}
									else
									{
										safeLocation = CoordTask.safeLocation(worldName, x, y, z, yaw, pitch);
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
							}.runTask(NKhome.getPlugin());
						}
						else
						{
							sender.sendMessage(ChatColor.RED + " Ce home n'existe pas.");
						}
						return null;
					}
				});

				return true;
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

	private boolean hasHomePermissions(CommandSender sender)
	{
		return sender.hasPermission("*") || sender.hasPermission("nkhome.*") || sender.hasPermission("nkhome.home")
				|| sender.hasPermission("nkhome.user") || sender.hasPermission("nkhome.admin");
	}

	private boolean hasHomeOtherPermissions(CommandSender sender)
	{
		return sender.hasPermission("*") || sender.hasPermission("nkhome.*") || sender.hasPermission("nkhome.home.other")
				|| sender.hasPermission("nkhome.admin");
	}
}
