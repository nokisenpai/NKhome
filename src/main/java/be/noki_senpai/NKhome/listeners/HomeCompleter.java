package be.noki_senpai.NKhome.listeners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import be.noki_senpai.NKhome.managers.HomeManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import be.noki_senpai.NKhome.NKhome;

public class HomeCompleter implements TabCompleter 
{
	private HomeManager homeManager = null;

	public HomeCompleter(HomeManager homeManager)
	{
		this.homeManager = homeManager;
	}
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) 
	{
		if (sender instanceof Player) 
		{
			if(args.length==1)
			{
				final List<String> completions = new ArrayList<>();
				org.bukkit.util.StringUtil.copyPartialMatches(args[0], homeManager.getHomes(sender.getName()).keySet(), completions);
				Collections.sort(completions);
				return completions;
			}
		}
		return null;
	}
}
