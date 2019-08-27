package be.noki_senpai.NKhome.listeners;

import be.noki_senpai.NKhome.managers.HomeManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import be.noki_senpai.NKhome.NKhome;
import be.noki_senpai.NKhome.data.NKPlayer;
import org.bukkit.scheduler.BukkitRunnable;;

public class PlayerConnectionListener implements Listener
{
	private HomeManager homeManager;

	public PlayerConnectionListener(HomeManager homeManager)
	{
		this.homeManager = homeManager;
	}

	@EventHandler public void PlayerJoinEvent(final PlayerJoinEvent event)
	{
		new BukkitRunnable()
		{
			@Override public void run()
			{
				homeManager.addPlayer(event.getPlayer());
			}
		}.runTaskAsynchronously(NKhome.getPlugin());
	}

	@EventHandler public void onPlayerQuitEvent(final PlayerQuitEvent event)
	{
		new BukkitRunnable()
		{
			@Override public void run()
			{
				homeManager.delPlayer(event.getPlayer().getName());
			}
		}.runTaskAsynchronously(NKhome.getPlugin());
	}
}
