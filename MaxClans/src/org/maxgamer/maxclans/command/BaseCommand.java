package org.maxgamer.maxclans.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxclans.clan.ClanManager;
import org.maxgamer.maxclans.clan.ClanMember;

public abstract class BaseCommand implements Comparable<BaseCommand>{
	/** The permission node to use this command. Empty if none required. */
	protected String perm = "";
	/** The usage for this command. Eg.  /kill Monsters */
	protected String usage = "Invalid usage.";
	/** The description for this command. Eg. 'Kills monsters' */
	protected String description = "";
	/** True if the console can use this. Defaults to false */
	protected boolean console = false;
	/** True if the player needs a clan to do this. Defaults to false */
	protected boolean needsClan = false;
	/** Whether or not this command requires the player to have no clan */
	protected boolean noClan = false;
	/** Minimum size of String[] args for this command */
	protected int num_args = 0;
	/** Minimum clan rank to execute this command */
	protected int rank = 0;
	/** Whether to show the command in the help list */
	protected boolean show = true; 
	
	public int compareTo(BaseCommand o){
		return usage.compareTo(o.usage);
	}
	
	public void run(CommandSender sender, String[] args){
		if(!perm.isEmpty() && !sender.hasPermission(perm)){
			sender.sendMessage(ChatColor.RED + "You don't have permission: " + perm);
			return;
		}
		if(!console && !(sender instanceof Player)){
			sender.sendMessage(ChatColor.RED + "Only players may use this command.");
			return;
		}
		if(needsClan){
			Player p = (Player) sender;
			ClanMember cm = ClanManager.getClanMember(p.getName());
			if(cm == null || cm.getClan() == null){
				p.sendMessage(ChatColor.RED + "You are not a member of a clan.");
				return;
			}
			if(cm.getRank() < rank){
				p.sendMessage(ChatColor.RED + "You do not have a high enough rank to do that.");
				return;
			}
		}
		else if(noClan){
			Player p = (Player) sender;
			ClanMember cm = ClanManager.getClanMember(p.getName());
			if(cm != null && cm.getClan() != null){
				p.sendMessage(ChatColor.RED + "You can't use that command in a clan.");
				return;
			}
		}
		
		if(num_args >= args.length){
			sender.sendMessage(ChatColor.RED + usage);
			return;
		}
		
		onRun(sender, args);
	}
	
	public boolean canUse(CommandSender sender){
		if(!perm.isEmpty() && !sender.hasPermission(perm)){
			return false;
		}
		if(!console && !(sender instanceof Player)){
			return false;
		}
		if(needsClan){
			Player p = (Player) sender;
			ClanMember cm = ClanManager.getClanMember(p.getName());
			if(cm == null || cm.getClan() == null){
				return false;
			}
			if(cm.getRank() < rank){
				return false;
			}
		}
		else if(noClan){
			Player p = (Player) sender;
			ClanMember cm = ClanManager.getClanMember(p.getName());
			if(cm != null && cm.getClan() != null){
				return false;
			}
		}
		return true;
	}
	
	public abstract void onRun(CommandSender sender, String[] args);
}