package org.maxgamer.maxclans.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxclans.MaxClans;
import org.maxgamer.maxclans.clan.Clan;
import org.maxgamer.maxclans.clan.ClanManager;
import org.maxgamer.maxclans.clan.ClanMember;

public class ClanAdminCommand implements CommandExecutor{
	private HashMap<String, BaseCommand> commands = new HashMap<String, BaseCommand>(10);
	
	public ClanAdminCommand(){
		//delete command
		BaseCommand delete = new BaseCommand(){
			@Override
			public void onRun(CommandSender sender, String[] args) {
				StringBuilder sb = new StringBuilder(args[1]);
				for(int i = 2; i < args.length; i++){
					sb.append(" " + args[i]);
				}
				String name = sb.toString();
				
				Clan clan = ClanManager.getClan(name, true);
				if(clan == null){
					sender.sendMessage(ChatColor.RED + "No such clan found: " + name);
					return;
				}
				
				clan.sendMessage(ChatColor.RED + "Clan has been deleted by admin.");
				clan.delete();
				sender.sendMessage(ChatColor.RED + "You have deleted " + clan.getName());
			}
			
		};
		delete.console = true;
		delete.needsClan = false;
		delete.num_args = 1;
		delete.perm = "maxclans.admin.delete";
		delete.usage = "/clan delete";
		delete.description = "Delete a clan!";
		
		commands.put("delete", delete);
		
		//join command
		BaseCommand join = new BaseCommand(){
			@Override
			public void onRun(CommandSender sender, String[] args) {
				Player p = (Player) sender;
				StringBuilder sb = new StringBuilder(args[1]);
				for(int i = 2; i < args.length; i++){
					sb.append(" " + args[i]);
				}
				String name = sb.toString();
				
				Clan clan = ClanManager.getClan(name, true);
				if(clan == null){
					sender.sendMessage(ChatColor.RED + "No such clan found: " + name);
					return;
				}
				
				ClanMember cm = ClanManager.getClanMember(p);
				if(cm == null) cm = ClanManager.createClanMember(p);
				clan.add(cm);
				while(clan.promote(cm));
				sender.sendMessage(ChatColor.RED + "Force joined [And promoted to Master]: " + clan.getName());
				cm.update();
			}
			
		};
		join.console = false;
		join.needsClan = false;
		join.noClan = true;
		join.num_args = 1;
		join.perm = "maxclans.admin.join";
		join.usage = "/clan join";
		join.description = "Force enter a clan!";
		
		commands.put("join", join);
		
		//setname command
		BaseCommand setname = new BaseCommand(){
			@Override
			public void onRun(CommandSender sender, String[] args) {
				Player p = (Player) sender;
				StringBuilder sb = new StringBuilder(args[1]);
				for(int i = 2; i < args.length; i++){
					sb.append(" " + args[i]);
				}
				String name = sb.toString();
				
				Clan clan = ClanManager.getClan(name, true);
				if(clan != null){
					sender.sendMessage(ChatColor.RED + "A clan with that name already exists!");
					return;
				}
				
				ClanMember cm = ClanManager.getClanMember(p);
				cm.getClan().setName(name);
				
				p.sendMessage(ChatColor.GREEN + "Renamed your clan to: " + name);
			}
		};
		setname.console = false;
		setname.needsClan = true;
		setname.num_args = 1;
		setname.perm = "maxclans.admin.setname";
		setname.usage = "/clan setname";
		setname.description = "Force changes a clan name!";
		
		commands.put("setname", setname);
		
		//invite
		BaseCommand invite = new BaseCommand(){
			@Override
			public void onRun(CommandSender sender, String[] args) {
				Player p = (Player) sender;
				ClanMember inviter = ClanManager.getClanMember(p.getName());
				if(inviter == null || inviter.getClan() == null){
					sender.sendMessage(ChatColor.RED + "You have no clan!");
					return;
				}
				
				
				ClanMember invited = ClanManager.getClanMember(args[1]);
				if(invited == null){
					sender.sendMessage(ChatColor.RED + "No such clan user found: " + args[1]);
					return;
				}
				
				if(invited.getClan() == null){
					invited.getClan().kick(invited);
				}
				
				inviter.getClan().add(invited);
				
				invited.update();
				
				Player inv = invited.getPlayer();
				if(inv != null) inv.sendMessage(ChatColor.YELLOW + "You have been forced to join " + inviter.getClan().getName() + " by an admin.");
				
				p.sendMessage(ChatColor.GREEN + "Success. Forced " + invited.getName() + " into " + inviter.getClan().getName() + ".");
			}
			
		};
		invite.console = false;
		invite.needsClan = true;
		invite.num_args = 2;
		invite.perm = "maxclans.admin.invite";
		invite.usage = "/clan invite user";
		invite.description = "Forces a user to join your clan!";
		
		commands.put("setname", setname);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		try{
			BaseCommand com = null;
			if(args.length > 0){
				com = this.commands.get(args[0]);
			}
			if(com == null){
				sendHelp(sender);
			}
			else{
				com.run(sender, args);
			}
		}
		catch(Exception e){
			if(MaxClans.debug){
				MaxClans.error(e);
			}
		}
		catch(Error e){
			if(MaxClans.debug){
				MaxClans.error(e);
			}
		}
		
		return true;
	}
	
	public void sendHelp(CommandSender sender){
		sender.sendMessage(ChatColor.GREEN + "Available Commands: ");
		
		//We only want unique commands - Ignore aliases.
		ArrayList<BaseCommand> cmds = new ArrayList<BaseCommand>(this.commands.size());
		for(BaseCommand cmd : this.commands.values()){
			if(cmds.contains(cmd) || !cmd.show) continue;
			cmds.add(cmd);
		}
		
		//Sort it (According to the usage field)
		Collections.sort(cmds);
		
		//Spit them out to the player.
		for(BaseCommand cmd : cmds){
			if(!cmd.canUse(sender)) continue;
			sender.sendMessage(ChatColor.GREEN + cmd.usage + (cmd.description.isEmpty() ? "" : " - " + ChatColor.AQUA + cmd.description));
		}
	}
	
	public String format(double amount){
		if(getEcon() == null) return ""+amount;
		return getEcon().format(amount);
	}
	public double getBalance(String name){
		if(getEcon() == null) return 0;
		return getEcon().getBalance(name);
	}
	public Economy getEcon(){
		return MaxClans.instance.getEcon();
	}
}