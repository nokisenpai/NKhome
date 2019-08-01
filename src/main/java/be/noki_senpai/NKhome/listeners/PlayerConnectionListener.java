package be.noki_senpai.NKhome.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import be.noki_senpai.NKhome.NKhome;
import be.noki_senpai.NKhome.data.NKPlayer;;



public class PlayerConnectionListener implements Listener 
{
    @EventHandler
    public void PlayerJoinEvent(final PlayerJoinEvent event) 
    {
    	NKhome.players.putIfAbsent(event.getPlayer().getName(),new NKPlayer(event.getPlayer().getUniqueId()));
    }

    @EventHandler
    public void onPlayerQuitEvent(final PlayerQuitEvent event) 
    {	
    	NKhome.players.remove(Bukkit.getOfflinePlayer(event.getPlayer().getUniqueId()).getName());
    }
}
