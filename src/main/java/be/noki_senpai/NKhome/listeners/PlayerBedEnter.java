package be.noki_senpai.NKhome.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult;

import be.noki_senpai.NKhome.NKhome;
import be.noki_senpai.NKhome.data.Home;;



public class PlayerBedEnter implements Listener 
{
	@EventHandler
	public void onPlayerBedEnter(final PlayerBedEnterEvent event) 
	{
		Player player = event.getPlayer();
		if(player.hasPermission("*") || player.hasPermission("nkhome.*") || player.hasPermission("nkhome.bed") || player.hasPermission("nkhome.user") || player.hasPermission("nkhome.admin"))
		{
			if(event.getBedEnterResult() == BedEnterResult.OK)
			{
				String playerName = player.getName();
				Home homeBed = NKhome.players.get(playerName).getBed();
				Location bedLocation = event.getBed().getLocation();
		
				if(homeBed != null)
				{
					if( homeBed.getX() != bedLocation.getX() || 
						homeBed.getY() != bedLocation.getY() || 
						homeBed.getZ() != bedLocation.getZ() || 
						!(homeBed.getWorld().equals(bedLocation.getWorld().getName()))) 
					{
						if(homeBed.getServer().equals(NKhome.serverName))
						{
							if(!(NKhome.getInstance().getServer().getWorld(homeBed.getWorld()).getBlockAt((int)homeBed.getX(), (int)homeBed.getY(), (int)homeBed.getZ()).getBlockData() instanceof org.bukkit.block.data.type.Bed))
							{
								NKhome.updateHome(playerName, "bed", bedLocation);
								event.getPlayer().sendMessage(ChatColor.GREEN + " Votre home 'bed' a été mis à jour.");
							}
						}
						else
						{
							event.getPlayer().sendMessage(ChatColor.RED + " Vous possédez déjà un home 'bed' sur le serveur '" + homeBed.getServer() + "'");
						}
					}
					/*else
					{
						event.getPlayer().sendMessage(ChatColor.GREEN + " Il ne s'est rien passé.");
					}*/
				}
				else
				{
					NKhome.setHome(playerName, "bed", bedLocation);
					event.getPlayer().sendMessage(ChatColor.GREEN + " Votre home 'bed' a été créé.");
				}
			}
		}
	}
}
