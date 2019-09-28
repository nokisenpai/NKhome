package be.noki_senpai.NKhome.listeners;

import be.noki_senpai.NKhome.managers.HomeManager;
import be.noki_senpai.NKhome.managers.QueueManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import be.noki_senpai.NKhome.NKhome;
import be.noki_senpai.NKhome.data.NKPlayer;;import java.util.function.Function;

public class PlayerConnectionListener implements Listener
{
	private HomeManager homeManager = null;
	private QueueManager queueManager = null;

	public PlayerConnectionListener(HomeManager homeManager, QueueManager queueManager)
	{
		this.homeManager = homeManager;
		this.queueManager = queueManager;
	}

	@EventHandler
	public void PlayerJoinEvent(final PlayerJoinEvent event) 
	{
		queueManager.addToQueue(new Function()
		{
			@Override public Object apply(Object o)
			{
				homeManager.addPlayer(event.getPlayer());
				if(NKhome.managePlayerDb)
				{
					homeManager.addOtherServer(event.getPlayer().getName());
				}
				return null;
			}
		});
	}

	@EventHandler
	public void onPlayerQuitEvent(final PlayerQuitEvent event) 
	{
		if(NKhome.managePlayerDb)
		{
			homeManager.removeOtherServer(event.getPlayer().getName());
		}
		homeManager.delPlayer(event.getPlayer().getName());
	}
}
