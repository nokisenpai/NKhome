package be.noki_senpai.NKhome.listeners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import be.noki_senpai.NKhome.NKhome;

public class HomeCompleter implements TabCompleter 
{
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) 
	{
		if (sender instanceof Player) 
		{
			if(args.length==1)
			{
				final List<String> completions = new ArrayList<>();
				org.bukkit.util.StringUtil.copyPartialMatches(args[0], NKhome.players.get(sender.getName()).getHomes().keySet(), completions);
				Collections.sort(completions);
				return completions;
			}
		}
		return null;
	}
}
