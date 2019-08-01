package be.noki_senpai.NKhome.cmd;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DelHomeCmd implements CommandExecutor
{
	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) 
	{
				
		//if no argument
		if(args.length == 0)
		{
			// tp au premier home
			return true;
		}
		
		args[0] = args[0].toLowerCase();
		// tp home spécifié
		return true;
	}
}
