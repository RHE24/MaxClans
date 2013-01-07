package org.maxgamer.maxclans.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PlayerCommand implements CommandExecutor{
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		/*
		ClanMember cm;
		KarmaPlayer kp;
		
		if(args.length < 1){
			if(!(sender instanceof Player)){
				sender.sendMessage(ChatColor.RED + "Usage: /player <name>");
				return true;
			}
			else{
				Player p = (Player) sender;
				cm = ClanManager.getClanMember(p);
				kp = MaxPvP.instance.getKarmaPlayer(p);
			}
		}
		else{
			cm = ClanManager.getClanMember(args[1], true);
			if(cm != null) kp = MaxPvP.instance.getKarmaPlayer(args[1], true);
		}
		
		if(cm != null && cm.getClan() != null){
			sender.sendMessage(ChatColor.GREEN + cm.getName() + " is from " + cm.getClan() + ".");
		}
		if(kp != null){
			sender.sendMessage(arg0)
		}*/
		return false;
	}
	
}