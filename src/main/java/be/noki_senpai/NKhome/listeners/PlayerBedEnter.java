package be.noki_senpai.NKhome.listeners;

import be.noki_senpai.NKhome.managers.ConfigManager;
import be.noki_senpai.NKhome.managers.HomeManager;
import be.noki_senpai.NKhome.managers.QueueManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult;

import be.noki_senpai.NKhome.NKhome;
import be.noki_senpai.NKhome.data.Home;

import java.util.function.Function;

public class PlayerBedEnter implements Listener
{
	private HomeManager homeManager;
	private QueueManager queueManager = null;

	public PlayerBedEnter(HomeManager homeManager, QueueManager queueManager)
	{
		this.homeManager = homeManager;
		this.queueManager = queueManager;
	}

	@EventHandler
	public void onPlayerBedEnter(final PlayerBedEnterEvent event)
	{
		Player player = event.getPlayer();
		if(hasSetBedHomePermissions(player))
		{
			// Check if player success enter in his bed (night or storm)
			if(event.getBedEnterResult() == BedEnterResult.OK)
			{
				String playerName = player.getName();
				Home oldBedHome = homeManager.getPlayer(playerName).getBed();
				Location bedLocation = event.getBed().getLocation();

				// If player has already a bed home
				if(oldBedHome != null)
				{
					// Check if new bed home is different than old bed home
					if(isDifferendBed(oldBedHome, bedLocation))
					{
						// Check if old bed home is on the same server
						if(oldBedHome.getServer().equals(ConfigManager.SERVERNAME))
						{
							// Check if bed has been broken
							if(!(homeManager.isBedBlock(oldBedHome)))
							{
								queueManager.addToQueue(new Function()
								{
									@Override public Object apply(Object o)
									{
										homeManager.updateHome(playerName, "bed", bedLocation);
										return null;
									}
								});

								event.getPlayer().sendMessage(ChatColor.GREEN + " Votre home 'bed' a été mis à jour.");
							}
							// No else.
						}
						else
						{
							// We don't update bed home. If player still want update his bed home he needs to break his bed on the other world or use "/delhome bed" command before
							event.getPlayer().sendMessage(ChatColor.RED + " Vous possédez déjà un home 'bed' sur le serveur '" + oldBedHome.getServer()
									+ "'");
						}
					}
					// No else.
					/*
					 * else { event.getPlayer().sendMessage(ChatColor.GREEN +
					 * " Il ne s'est rien passé."); }
					 */
				}
				else
				{
					queueManager.addToQueue(new Function()
					{
						@Override public Object apply(Object o)
						{
							homeManager.addHome(playerName, "bed", bedLocation);
							return null;
						}
					});
					event.getPlayer().sendMessage(ChatColor.GREEN + " Votre home 'bed' a été créé.");
				}
			}
		}
	}

	// Check if new bed location is different than old bed location
	private boolean isDifferendBed(Home homeBed, Location bedLocation)
	{
		if(homeBed.getX() != bedLocation.getX() || homeBed.getY() != bedLocation.getY() || homeBed.getZ() != bedLocation.getZ()
				|| !(homeBed.getWorld().equals(bedLocation.getWorld().getName())))
		{
			return true;
		}
		return false;
	}

	private boolean hasSetBedHomePermissions(Player player)
	{
		return player.hasPermission("*") || player.hasPermission("nkhome.*") || player.hasPermission("nkhome.bed") || player.hasPermission("nkhome.user")
				|| player.hasPermission("nkhome.admin");
	}
}
