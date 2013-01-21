package org.maxgamer.maxclans.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.maxgamer.MaxPvP.KarmaPlayer;
import org.maxgamer.MaxPvP.MaxPvP;
import org.maxgamer.maxclans.MaxClans;
import org.maxgamer.maxclans.clan.Clan;
import org.maxgamer.maxclans.clan.ClanChest;
import org.maxgamer.maxclans.clan.ClanManager;
import org.maxgamer.maxclans.clan.ClanMember;
import org.maxgamer.maxclans.util.Util;

public class ClanCommand implements CommandExecutor, Listener{
	private HashMap<String, BaseCommand> commands = new HashMap<String, BaseCommand>(10);
	private HashMap<String, String> aliases = new HashMap<String, String>(10);
	
	private HashMap<Player, Invite> invites = new HashMap<Player, Invite>(3); //Safe to do so since we listen for playerQuitEvent
	private HashMap<Clan, AllyRequest> allyRequests = new HashMap<Clan, AllyRequest>(3);
	private ArrayList<String> createMessages = new ArrayList<String>();
	private Random r = new Random();
	@EventHandler
	public void onLogout(PlayerQuitEvent e){
		Invite inv = invites.remove(e.getPlayer());
		if(inv == null || inv.hasExpired()) return;
		inv.getInviter().getPlayer().sendMessage(ChatColor.RED + inv.getName() + " has declined your invite.");
	}
	
	@EventHandler
	public void onChestClose(InventoryCloseEvent e){
		ClanMember cm = ClanManager.getClanMember(e.getPlayer().getName());
		if(cm == null || cm.getClan() == null) return; //You don't have a clan chest!
		cm.getClan().getClanChest().removeViewer(cm.getPlayer());
	}
	
	public void addCommand(BaseCommand base, String name, String... aliases){
		name = name.toLowerCase();
		commands.put(name, base);
		
		addAlias(name, aliases);
	}
	
	public ClanCommand(){
		createMessages.add(ChatColor.RED + "%1$s "+ChatColor.GREEN+"has brought rise to "+ChatColor.RED+"%2$s"+ChatColor.GREEN+"!");
		createMessages.add(ChatColor.RED + "%1$s "+ChatColor.GREEN+"has forged "+ChatColor.RED+"%2$s"+ChatColor.GREEN+"!");
		createMessages.add(ChatColor.RED + "%1$s "+ChatColor.GREEN+"has given life to "+ChatColor.RED+"%2$s"+ChatColor.GREEN+"!");
		createMessages.add(""+ChatColor.RED+"%2$s "+ChatColor.GREEN+"has risen by the hand of "+ChatColor.RED + "%1$s "+ChatColor.GREEN+"!");
		
		//Info command
		BaseCommand info = new BaseCommand(){
			@Override
			public void onRun(CommandSender sender, String[] args) {
				Clan clan = null;
				
				if(!(sender instanceof Player)){
					if(args.length < 2){
						sender.sendMessage("Usage: /clan info <clan>");
						return;
					}
					else{
						StringBuilder sb = new StringBuilder(args[1]);
						for(int i = 2; i < args.length; i++){
							sb.append(" " + args[i]);
						}
						String name = sb.toString();
						Player p = Bukkit.getPlayer(name);
						if(p != null){
							ClanMember cm = ClanManager.getClanMember(p);
							if(cm != null && cm.getClan() != null){
								clan = cm.getClan();
							}
						}
						if(clan == null){
							clan = ClanManager.getClan(name, true);
						}
					}
				}
				else{
					Player p = (Player) sender;
					if(args.length < 2){
						ClanMember cm = ClanManager.getClanMember(p);
						if(cm == null || cm.getClan() == null){
							sender.sendMessage("Usage: /clan info <clan>");
							return;
						}
						else{
							clan = cm.getClan();
						}
					}
					else{
						StringBuilder sb = new StringBuilder(args[1]);
						for(int i = 2; i < args.length; i++){
							sb.append(" " + args[i]);
						}
						
						String name = sb.toString();
						Player pl = Bukkit.getPlayer(name);
						if(pl != null){
							ClanMember cm = ClanManager.getClanMember(pl);
							if(cm != null && cm.getClan() != null){
								clan = cm.getClan();
							}
						}
						if(clan == null){
							clan = ClanManager.getClan(name, true);
						}
					}
				}
				
				if(clan == null){
					sender.sendMessage(ChatColor.RED + "No such clan found.");
					return;
				}
				else{
					sender.sendMessage(ChatColor.GREEN + "Clan: " + clan.getName());
					sender.sendMessage((getEcon() == null ? "" : ChatColor.GOLD + "Cash: " + ChatColor.GREEN + format(clan.getBalance()) + " ") + ChatColor.GOLD + "Levels: " + ChatColor.GREEN + clan.getLevels() + ChatColor.GOLD + ", Kills: " + ChatColor.GREEN + clan.getKills() + ChatColor.GOLD + ", Deaths: " + ChatColor.GREEN + clan.getDeaths() + ChatColor.GOLD + ", Rating: " + ChatColor.GREEN + clan.getRating());
					sender.sendMessage(ChatColor.GOLD + "Allies ("+clan.getAllies().size()+"): " + clan.getFormattedAllies());
					sender.sendMessage(ChatColor.GOLD + "Enemies ("+clan.getEnemies().size()+"): " + clan.getFormattedEnemies());
					
					if(clan.getGuests().isEmpty()){
						sender.sendMessage(ChatColor.RED + "Clan has no members somehow.");
						MaxClans.error(clan.getName() + " has no members.");
					}
					else{
						StringBuilder sb = new StringBuilder();
						for(ClanMember cm : clan.getGuests()){
							sb.append((cm.getPlayer() == null ? ChatColor.GRAY : ChatColor.GREEN) + cm.getName() + " [" + cm.getRank() + "]" + ChatColor.GRAY + ", ");
						}
						sb.replace(sb.length() - 2, sb.length(), "");
						sender.sendMessage(ChatColor.GREEN + "Players ("+clan.getGuests().size()+"): " + sb.toString());
					}
					
					int count = 0;
					StringBuilder onlineAllies = new StringBuilder();
					for(Clan ally : clan.getAllies()){
						for(ClanMember am : ally.getOnline().values()){
							count++;
							onlineAllies.append(am.getPlayer().getName() + ", ");
						}
					}
					
					if(count > 0){
						onlineAllies.replace(onlineAllies.length() - 2, onlineAllies.length(), "");
						sender.sendMessage(ChatColor.GREEN + "Online Allies (" + count + "): " + onlineAllies.toString());
					}
				}
				
			}
			
		};
		info.console = true;
		info.needsClan = false;
		info.num_args = 0;
		info.perm = "maxclans.clan.info";
		info.usage = "/clan info";
		info.description = "Lookup clans and find information!";
		
		commands.put("info", info);
		addAlias("info", "who", "whois", "lookup", "i");
		
		//Create command
		BaseCommand create = new BaseCommand(){
			@Override
			public void onRun(CommandSender sender, String[] args) {
				Player p = (Player) sender;
				
				if(getEcon() != null && getBalance(p.getName()) < MaxClans.instance.clanCost){
					sender.sendMessage(ChatColor.RED + "Clans cost " + format(MaxClans.instance.clanCost) + " to create.");
					return;
				}
				
				StringBuilder sb = new StringBuilder(args[1]);
				for(int i = 2; i < args.length; i++){
					sb.append(" " + args[i]);
				}
				String name = sb.toString();
				
				ClanMember cm = ClanManager.getClanMember(p.getName());
				if(cm == null){
					cm = ClanManager.createClanMember(p);
				}
				
				if(name.length() > 20){
					sender.sendMessage(ChatColor.RED + "That name is too long. Max 20 chars!");
					return;
				}
				
				String invalid = Util.getInvalidChars(name);
				if(!invalid.isEmpty()){
					sender.sendMessage(ChatColor.RED + "The clan name may not contain the following: '" + invalid + "'.");
					return;
				}
				
				if(ClanManager.getClan(name) != null){
					sender.sendMessage(ChatColor.RED + "The name " + name + " is already taken.");
					return;
				}
				
				getEcon().withdrawPlayer(p.getName(), MaxClans.instance.clanCost);
				
				Clan clan = ClanManager.createClan(name);
				clan.add(cm);
				while(clan.promote(cm));
				cm.update();
				
				sender.sendMessage(ChatColor.GREEN + "Successfully created & joined clan: " + clan.getName() + ". " + format(MaxClans.instance.clanCost) + " withdrawn.");
				Bukkit.broadcastMessage(String.format(createMessages.get(r.nextInt(createMessages.size())), cm.getName(), clan.getName()));
			}
			
		};
		create.console = false;
		create.needsClan = false;
		create.noClan = true;
		create.num_args = 1;
		create.perm = "maxclans.clan.create";
		create.usage = "/clan create <ClanName>";
		create.description = "Creates a new clan with the given name.";
		
		commands.put("create", create);
		addAlias("create", "new");
		
		//Delete command
		BaseCommand delete = new BaseCommand(){
			@Override
			public void onRun(CommandSender sender, String[] args) {
				Player p = (Player) sender;
				
				ClanMember cm = ClanManager.getClanMember(p.getName());
				
				StringBuilder sb = new StringBuilder(args[1]);
				for(int i = 2; i < args.length; i++){
					sb.append(" " + args[i]);
				}
				String name = sb.toString();
				
				Clan clan = ClanManager.getClan(name);
				if(clan != cm.getClan()){ //Null clans are removed above
					sender.sendMessage(ChatColor.RED + "Please type /clan delete " + ChatColor.GREEN + "<name of your clan>" + ChatColor.RED + " to delete it.");
					return;
				}
				if(getEcon() != null && clan.getBalance() > 0){
					cm.clanWithdraw(clan.getBalance());
					sender.sendMessage(ChatColor.GREEN + "Withdrew all clan funds.");
				}
				
				//Give the player whatever is in the clan chest.
				LinkedList<ItemStack> drop = clan.getClanChest().giveContents(cm.getPlayer());
				for(ItemStack iStack : drop){
					p.getWorld().dropItem(p.getLocation(), iStack);
				}
				
				clan.delete();
				clan.sendMessage(ChatColor.YELLOW + "Your clan has been disbanded.");
			}
		};
		delete.console = false;
		delete.needsClan = true;
		delete.num_args = 1;
		delete.perm = "maxclans.clan.delete";
		delete.usage = "/clan delete <ClanName>";
		delete.description = "Deletes your current clan.";
		delete.rank = 3;
		
		commands.put("delete", delete);
		addAlias("delete", "del");
		
		//Owner command
		BaseCommand owner = new BaseCommand(){
			@Override
			public void onRun(CommandSender sender, String[] args) {
				Player p = (Player) sender;
				
				ClanMember cm = ClanManager.getClanMember(p.getName());
				ClanMember cmTarg = ClanManager.getClanMember(args[1]);
				if(cmTarg == null || cmTarg.getClan() != cm.getClan()){
					sender.sendMessage(ChatColor.RED + args[1] + " is not in your clan. You may not give them your clan.");
					return;
				}
				
				if(cm == cmTarg){
					sender.sendMessage(ChatColor.RED + "You may not transfer ownershipp to yourself!");
					return;
				}
				
				cm.getClan().demote(cm);
				while(cmTarg.getRank() < 3){
					cm.getClan().promote(cmTarg);
				}
				
				sender.sendMessage(ChatColor.RED + "You have transferred clan ownership to " + cmTarg.getName() + ". You are no longer the Clan Master.");
				cm.getClan().sendMessage(ChatColor.GREEN + cm.getName() + " has given " + cmTarg.getName() + " full control of the clan and resigned from Clan Master.", 0, cm);
			}
		};
		owner.console = false;
		owner.needsClan = true;
		owner.num_args = 1;
		owner.perm = "maxclans.clan.owner";
		owner.usage = "/clan owner <Member>";
		owner.description = "Makes someone else the owner of the clan.";
		owner.rank = 3;
		
		commands.put("owner", owner);
		addAlias("owner", "setowner");
		
		//Leave command
		BaseCommand leave = new BaseCommand(){
			@Override
			public void onRun(CommandSender sender, String[] args) {
				Player p = (Player) sender;
				
				ClanMember cm = ClanManager.getClanMember(p.getName());
				Clan c = cm.getClan();
				
				if(c.size(2)){
					//There will be at least 1 other member in the clan
					if(cm.getRank() >= 3 && c.getMasters().size() <= 1){
						sender.sendMessage(ChatColor.RED + "You are the only master in your clan.");
						sender.sendMessage(ChatColor.RED + "Either use /clan delete or /clan owner first.");
						return;
					}
					else{
						c.kick(cm);
						cm.update();
						c.sendMessage(ChatColor.YELLOW + cm.getName() + " has left " + ChatColor.GREEN + c.getName()); //No need to exclude, player already gone.
					}
				}
				else{
					//Nobody will be left. Delete the clan.
					if(getEcon() != null && c.getBalance() > 0){
						cm.clanWithdraw(c.getBalance());
						sender.sendMessage(ChatColor.GREEN + "Withdrew all clan funds.");
					}
					
					//Give the player whatever is in the clan chest.
					LinkedList<ItemStack> drop = c.getClanChest().giveContents(cm.getPlayer());
					for(ItemStack iStack : drop){
						p.getWorld().dropItem(p.getLocation(), iStack);
					}
					
					c.delete();
					sender.sendMessage(ChatColor.RED + "Your clan was deleted because nobody else was left.");
				}
				sender.sendMessage(ChatColor.YELLOW + "You have left " + ChatColor.GREEN + c.getName());
			}
		};
		leave.console = false;
		leave.needsClan = true;
		leave.num_args = 0;
		leave.perm = "maxclans.clan.leave";
		leave.usage = "/clan leave";
		leave.description = "Leaves your current clan";
		
		commands.put("leave", leave);
		addAlias("leave", "quit", "l");
		
		
		//Invite command
		BaseCommand invite = new BaseCommand(){
			@Override
			public void onRun(CommandSender sender, String[] args) {
				Player p = (Player) sender;
				
				ClanMember cm = ClanManager.getClanMember(p.getName());
				Clan c = cm.getClan();
				
				Player targ = Bukkit.getPlayer(args[1]);
				if(targ == null){
					sender.sendMessage(ChatColor.RED + "No player found: " + args[1]);
					return;
				}
				ClanMember invited = ClanManager.getClanMember(targ.getName());
				if(invited != null && invited.hasClan()){
					targ.sendMessage(ChatColor.RED + "You were invited to " + ChatColor.GREEN + c.getName() + ChatColor.RED + " but are already in the clan " + ChatColor.GREEN + invited.getClan().getName() + ChatColor.RED + ".");
					sender.sendMessage(ChatColor.RED + "That player is a member of another clan.");
					return;
				}
				
				Invite old = invites.get(targ);
				if(old != null && !old.hasExpired() && old.getClan() == c){
					sender.sendMessage(ChatColor.RED + "You may not send another invite yet.");
					return;
				}
				
				Invite inv = new Invite(cm, targ.getName());
				invites.put(targ, inv);
				targ.sendMessage(ChatColor.GREEN + "You have been invited to join " + ChatColor.YELLOW + c.getName() + ChatColor.GREEN + " by " + ChatColor.YELLOW + p.getName() + ChatColor.GREEN + ". Type " + ChatColor.YELLOW + "/clan accept" + ChatColor.GREEN + " to join!");
				sender.sendMessage(ChatColor.GREEN + "Invite sent to " + targ.getName() + " to join " + c.getName() + ".");
			}
		};
		invite.console = false;
		invite.needsClan = true;
		invite.num_args = 1;
		invite.perm = "maxclans.clan.invite";
		invite.usage = "/clan invite <name>";
		invite.description = "Invites a player to your clan.";
		invite.rank = 1;
		
		commands.put("invite", invite);
		addAlias("invite", "add");
		
		//Accept command
		BaseCommand accept = new BaseCommand(){
			@Override
			public void onRun(CommandSender sender, String[] args) {
				Player p = (Player) sender;
				if(!invites.containsKey(p)){
					sender.sendMessage(ChatColor.RED + "You were not invited to any clans.");
					return;
				}
				Invite inv = invites.get(p);
				if(inv.hasExpired()){
					sender.sendMessage(ChatColor.RED + "That invite has expired.");
					return;
				}
				ClanMember cm = ClanManager.getClanMember(p.getName());
				if(cm == null){
					cm = ClanManager.createClanMember(p);
				}
				else if(cm.hasClan()){
					p.sendMessage(ChatColor.RED + "You are already a member of another clan: " + ChatColor.GREEN + cm.getClan().getName() + ChatColor.RED+".  Try /clan leave.");
					return;
				}
				invites.remove(p);
				
				Clan clan = inv.getClan();
				clan.add(cm);
				cm.update();
				
				p.sendMessage(ChatColor.GREEN + "Invite accepted. Welcome to " + clan.getName() + ".");
				clan.sendMessage(ChatColor.GREEN + p.getName() + ChatColor.YELLOW + " has accepted " + inv.getInviter().getName() + "'s invite to the clan!", 0, cm);
			}
		};
		accept.console = false;
		accept.needsClan = false;
		accept.noClan = true;
		accept.num_args = 0;
		accept.perm = "maxclans.clan.accept";
		accept.usage = "/clan accept";
		accept.description = "Accepts an invite to someone elses clan.";
		accept.show = false; //We don't need to show this every time someone does /clan.
		
		commands.put("accept", accept);
		
		//Promote command
		BaseCommand promote = new BaseCommand(){
			@Override
			public void onRun(CommandSender sender, String[] args) {
				Player p = (Player) sender;
				
				ClanMember promoter = ClanManager.getClanMember(p.getName());
				ClanMember promoted = ClanManager.getClanMember(args[1], true);
				
				if(promoted == null){
					sender.sendMessage(ChatColor.RED + "No such player found: " + args[1]);
					return;
				}
				
				if(promoted.getClan() != promoter.getClan()){
					p.sendMessage(ChatColor.RED + promoted.getName() + " is not a member of " + ChatColor.GREEN + promoter.getClan().getName() + ChatColor.RED + ".");
					return;
				}
				
				if(promoter.getRank() <= promoted.getRank() + 1){
					p.sendMessage(ChatColor.RED + "You may not promote " + promoted.getName() + " any further.");
					return;
				}
				
				promoter.getClan().promote(promoted);
				promoted.update();
				p.sendMessage(ChatColor.GREEN + "You have promoted " + promoted.getName() + " to " + promoter.getClan().getRankName(promoted.getRank()));

				promoted.send(ChatColor.GREEN + "You have been promoted by " + promoter.getName() + " to " + promoter.getClan().getRankName(promoted.getRank()) + "!");
			}
		};
		promote.console = false;
		promote.needsClan = true;
		promote.rank = 2;
		promote.num_args = 1;
		promote.perm = "maxclans.clan.promote";
		promote.usage = "/clan promote <member>";
		promote.description = "Increases a users rank";
		
		commands.put("promote", promote);
		addAlias("promote", "promo");
		
		
		//Demote command
		BaseCommand demote = new BaseCommand(){
			@Override
			public void onRun(CommandSender sender, String[] args) {
				Player p = (Player) sender;
				
				ClanMember demoter = ClanManager.getClanMember(p.getName());
				ClanMember demoted = ClanManager.getClanMember(args[1], true);
				
				if(demoted == null){
					sender.sendMessage(ChatColor.RED + "No such player found: " + args[1]);
					return;
				}
				
				if(demoted == null || demoted.getClan() != demoter.getClan()){
					p.sendMessage(ChatColor.RED + (demoted == null ? args[1] : demoted.getName()) + " is not a member of " + ChatColor.GREEN + demoter.getClan().getName() + ChatColor.RED + ".");
					return;
				}
				
				if(demoter.getRank() <= demoted.getRank()){
					p.sendMessage(ChatColor.RED + "You may not demote your equals or superiors.");
					return;
				}
				
				if(!demoter.getClan().demote(demoted)){
					p.sendMessage(ChatColor.RED + "You may not demote " + demoted + " any further.");
					return;
				}
				demoted.update();
				p.sendMessage(ChatColor.GREEN + "You have demoted " + demoted.getName() + " to " + demoter.getClan().getRankName(demoted.getRank()));
				
				demoted.send(ChatColor.RED + "You have been demoted by " + demoter.getName() + " to " + demoter.getClan().getRankName(demoted.getRank()));
			}
		};
		demote.console = false;
		demote.needsClan = true;
		demote.rank = 2;
		demote.num_args = 1;
		demote.perm = "maxclans.clan.demote";
		demote.usage = "/clan demote <member>";
		demote.description = "Demotes a member in your clan.";
		
		commands.put("demote", demote);
		addAlias("demote", "demo");
		
		
		//List command
		BaseCommand list = new BaseCommand(){
			@Override
			public void onRun(CommandSender sender, String[] args) {
				int i = 0;
				int page = 1;
				if(args.length > 1){
					try{
						page = Integer.parseInt(args[1]);
						if(page < 1){
							sender.sendMessage(ChatColor.RED + "Invalid page number: " + args[1]);
							return;
						}
						
						i = page * -50 + 50;
					}
					catch(NumberFormatException e){
						sender.sendMessage(ChatColor.RED + "Invalid page number: " + args[1]);
					}
				}
				
				StringBuilder sb = new StringBuilder();
				for(Clan clan : ClanManager.getClans().values()){
					i++;
					if(i >= 0){
						sb.append(clan.getName() + ", ");
						if(i > 50) break; //Too many.
					}
				}
				if(sb.length() < 2){
					sender.sendMessage(ChatColor.RED + "Invalid page number: " + args[1]);
					return;
				}
				sb.replace(sb.length() - 2, sb.length(), "");
				
				sender.sendMessage(ChatColor.GREEN + "Clans [Page " + page + "]");
				sender.sendMessage(sb.toString());
			}
		};
		list.console = true;
		list.needsClan = false;
		list.num_args = 0;
		list.perm = "maxclans.clan.list";
		list.usage = "/clan list [page]";
		list.description = "Lists all clans.";
		
		commands.put("list", list);
		
		//Kick command
		BaseCommand kick = new BaseCommand(){
			@Override
			public void onRun(CommandSender sender, String[] args) {
				Player p = (Player) sender;
				
				ClanMember cmKicker = ClanManager.getClanMember(p.getName());
				
				ClanMember cmKicked = ClanManager.getClanMember(args[1], true);
				if(cmKicked == null){
					Player targ = Bukkit.getPlayer(args[1]);
					if(targ == null){
						sender.sendMessage(ChatColor.RED + "No such player found: " + args[1]);
						return;
					}
					cmKicked = ClanManager.getClanMember(targ.getName());
					if(cmKicked == null){
						sender.sendMessage(ChatColor.RED + targ.getName() + " has no clan.");
						return;
					}
				}
				
				if(cmKicker.getClan() != cmKicked.getClan()){
					sender.sendMessage(ChatColor.RED + cmKicked.getName() + " is not in the same clan as you.");
					return;
				}
				
				if(cmKicker.getRank() <= cmKicked.getRank()){
					p.sendMessage(ChatColor.RED + "You may not kick your superiors or equals.");
					return;
				}
				
				cmKicker.getClan().kick(cmKicked);
				cmKicked.update();
				
				sender.sendMessage(ChatColor.GREEN + "Kicked " + cmKicked.getName() + " from " + cmKicker.getClan().getName());
				cmKicker.getClan().sendMessage(ChatColor.GREEN + cmKicker.getName() + " kicked " + cmKicked.getName() + " from the clan.", 0, cmKicker);
				cmKicked.send(ChatColor.RED + "Kicked by " + cmKicker.getName() + " from " + cmKicker.getClan().getName());
			}
		};
		kick.console = false;
		kick.needsClan = true;
		kick.num_args = 1;
		kick.rank = 2;
		kick.perm = "maxclans.clan.kick";
		kick.usage = "/clan kick <Member>";
		kick.description = "Boots a member from your clan.";
		
		commands.put("kick", kick);
		addAlias("kick", "boot");
		
		//SetSpawn command
		BaseCommand setSpawn = new BaseCommand(){
			@Override
			public void onRun(CommandSender sender, String[] args) {
				Player p = (Player) sender;
				
				if(MaxClans.instance.isBlacklisted(p.getWorld())){
					sender.sendMessage(ChatColor.RED + "You can't do that in this world.");
					return;
				}
				
				ClanMember setter = ClanManager.getClanMember(p.getName());
				
				Location l = p.getLocation();
				setter.getClan().setSpawn(l.getWorld().getName(), l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
				setter.getClan().update();
				p.sendMessage(ChatColor.GREEN + "Spawn set successfully.");
			}
		};
		setSpawn.console = false;
		setSpawn.needsClan = true;
		setSpawn.num_args = 0;
		setSpawn.rank = 2;
		setSpawn.perm = "maxclans.clan.setspawn";
		setSpawn.usage = "/clan setspawn";
		setSpawn.description = "Changes the clan's spawn point.";
		
		commands.put("setspawn", setSpawn);
		
		//Spawn/Home command
		BaseCommand spawn = new BaseCommand(){
			@Override
			public void onRun(CommandSender sender, String[] args) {
				Player p = (Player) sender;
				
				ClanMember spawner = ClanManager.getClanMember(p.getName());

				Location loc = spawner.getClan().getSpawn();
				if(loc == null){
					p.sendMessage(ChatColor.RED + "Your clan has no spawn set yet.");
					return;
				}
				
				p.teleport(loc, TeleportCause.COMMAND);
			}
		};
		spawn.console = false;
		spawn.needsClan = true;
		spawn.num_args = 0;
		spawn.perm = "maxclans.clan.spawn";
		spawn.usage = "/clan spawn";
		spawn.description = "Teleports you to the clans spawn point.";
		
		commands.put("spawn", spawn);
		addAlias("spawn", "home");
		
		//Friendlyfire toggle command
		BaseCommand friendlyfire = new BaseCommand(){
			@Override
			public void onRun(CommandSender sender, String[] args) {
				Player p = (Player) sender;
				
				ClanMember spawner = ClanManager.getClanMember(p.getName());
				
				Clan clan = spawner.getClan();
				
				if(args.length > 1){
					String state = args[1].toLowerCase();
					if(state.equals("on") || state.equals("enable")){
						clan.setFriendlyFire(true);
						clan.update();
					}
					else if(state.equals("off") || state.equals("disable")){
						clan.setFriendlyFire(false);
						clan.update();
					}
					else{
						sender.sendMessage(ChatColor.RED + "Valid options: On|Off Enable|Disable.");
						return;
					}
				}
				else{
					//No on/off given. Just toggle it.
					clan.setFriendlyFire(!clan.hasFriendlyFire());
					clan.update();
				}
				clan.sendMessage(ChatColor.GREEN + "Friendly fire is now " + (clan.hasFriendlyFire() ? "on" : "off"));
			}
		};
		friendlyfire.console = false;
		friendlyfire.needsClan = true;
		friendlyfire.num_args = 0;
		friendlyfire.rank = 2;
		friendlyfire.perm = "maxclans.clan.friendlyfire";
		friendlyfire.usage = "/clan friendlyfire";
		friendlyfire.description = "Toggle hurting allies";
		
		commands.put("friendlyfire", friendlyfire);
		addAlias("friendlyfire", "ff");
		
		//Top command
		BaseCommand top = new BaseCommand(){
			@Override
			public void onRun(CommandSender sender, String[] args) {
				
				int results = 10;
				String catagory = "score";
				if(args.length > 1){
					catagory = args[1].toLowerCase();
					try{
						if(args.length > 2){
							results = Integer.parseInt(args[2]);
							if(results > 50){
								sender.sendMessage(ChatColor.RED + "No more than the top 50 may be displayed.");
								return;
							}
						}
					}
					catch(NumberFormatException e){
						//Nothing
					}
					
					try{
						results = Integer.parseInt(catagory);
						if(results > 50){
							sender.sendMessage(ChatColor.RED + "No more than the top 50 may be displayed.");
							return;
						}
						catagory = "score";
					}
					catch(NumberFormatException e){
						//Normal catagory
					}
				}
				

				
				if(catagory.equals("score") || catagory.equals("rating")){
					Clan[] top = ClanManager.getHighestRatings(results);
					int rating = 1;
					
					for(Clan clan : top){
						if(clan == null) break;
						sender.sendMessage(ChatColor.GOLD + "" + rating++ + ". " + ChatColor.GREEN + "Rating: " + ChatColor.GOLD + clan.getRating() + ChatColor.GREEN + " - " + ChatColor.GOLD + clan.getName());
					}
				}
				else if(catagory.equals("skills") || catagory.equals("stats") || catagory.equals("levels") || catagory.equals("mcmmo")){
					Clan[] top = ClanManager.getHighestLevels(results);
					int rating = 1;
					
					for(Clan clan : top){
						if(clan == null) break;
						sender.sendMessage(ChatColor.GOLD + "" + rating++ + ". " + ChatColor.GREEN + "Levels: " + ChatColor.GOLD + clan.getLevels() + ChatColor.GREEN + " - " + ChatColor.GOLD + clan.getName());
					}
				}
				else if(catagory.equals("kills") || catagory.equals("pvp")){
					Clan[] top = ClanManager.getHighestKills(results);
					int rating = 1;
					
					for(Clan clan : top){
						if(clan == null) break;
						sender.sendMessage(ChatColor.GOLD + "" + rating++ + ". " + ChatColor.GREEN + "Kills: " + ChatColor.GOLD + clan.getKills() + ChatColor.GREEN + " - " + ChatColor.GOLD + clan.getName());
					}
				}
				else if(catagory.equals("deaths")){
					Clan[] top = ClanManager.getHighestDeaths(results);
					int rating = 1;
					
					for(Clan clan : top){
						if(clan == null) break;
						sender.sendMessage(ChatColor.GOLD + "" + rating++ + ". " + ChatColor.GREEN + "Deaths: " + ChatColor.GOLD + clan.getDeaths() + ChatColor.GREEN + " - " + ChatColor.GOLD + clan.getName());
					}
				}
				else if(catagory.equals("money") || catagory.equals("cash") || catagory.equals("$") || catagory.equals("balance")){
					Clan[] top = ClanManager.getHighestCash(results);
					int rating = 1;
					
					for(Clan clan : top){
						if(clan == null) break;
						sender.sendMessage(ChatColor.GOLD + "" + rating++ + ". " + ChatColor.GREEN + "Balance: " + ChatColor.GOLD + format(clan.getBalance()) + ChatColor.GREEN + " - " + ChatColor.GOLD + clan.getName());
					}
				}
				else if(catagory.equals("size") || catagory.equals("players") || catagory.equals("#")){
					Clan[] top = ClanManager.getHighestPlayers(results);
					int rating = 1;
					
					for(Clan clan : top){
						if(clan == null) break;
						sender.sendMessage(ChatColor.GOLD + "" + rating++ + ". " + ChatColor.GREEN + "Players: " + ChatColor.GOLD + clan.getGuests().size() + ChatColor.GREEN + " - " + ChatColor.GOLD + clan.getName());
					}
				}
				else{
					sender.sendMessage(ChatColor.RED + "Valid catagories: Rating|Levels|Kills|Deaths|Money|Size");
				}
			}
		};
		top.console = true;
		top.needsClan = false;
		top.num_args = 0;
		top.perm = "maxclans.clan.top";
		top.usage = "/clan top";
		top.description = "Lists the top clans";
		
		commands.put("top", top);
		
		//Enemy command
		BaseCommand enemy = new BaseCommand(){
			@Override
			public void onRun(CommandSender sender, String[] args) {
				Player p = (Player) sender;
				
				ClanMember user = ClanManager.getClanMember(p.getName());
				Clan clan = user.getClan();
				
				StringBuilder sb = new StringBuilder(args[1]);
				for(int i = 2; i < args.length; i++){
					sb.append(" " + args[i]);
				}
				Clan enemy = ClanManager.getClan(sb.toString(), true);
				
				if(enemy == null){
					sender.sendMessage(ChatColor.RED + "No such clan found: " + sb.toString());
					return;
				}
				
				if(enemy == clan){
					sender.sendMessage(ChatColor.RED + "You cannot enemy yourself!");
					return;
				}
				
				if(clan.getEnemies().contains(enemy)){
					sender.sendMessage(ChatColor.RED + "They are already an enemy!");
					return;
				}
				
				if(clan.getAllies().contains(enemy)){
					clan.removeAlly(enemy);
				}
				
				
				clan.addEnemy(enemy);
				if(enemy.getAllies().contains(clan)){
					enemy.removeAlly(clan);
				}
				enemy.sendMessage(ChatColor.RED + clan.getName() + " has declared war on " + enemy.getName());
				clan.sendMessage(ChatColor.RED + user.getName() + " has declared war on " + enemy.getName());
			}
		};
		enemy.console = false;
		enemy.needsClan = true;
		enemy.num_args = 1;
		enemy.rank = 2;
		enemy.perm = "maxclans.clan.enemy";
		enemy.usage = "/clan enemy";
		enemy.description = "Sets another clan as an enemy";
		
		commands.put("enemy", enemy);
		addAlias("enemy", "war");
		
		//Ally command
		BaseCommand ally = new BaseCommand(){
			@Override
			public void onRun(CommandSender sender, String[] args) {
				Player p = (Player) sender;
				
				ClanMember user = ClanManager.getClanMember(p.getName());
				Clan clan = user.getClan();
				
				StringBuilder sb = new StringBuilder(args[1]);
				for(int i = 2; i < args.length; i++){
					sb.append(" " + args[i]);
				}
				Clan ally = ClanManager.getClan(sb.toString(), true);
				
				if(ally == null){
					sender.sendMessage(ChatColor.RED + "No such clan found: " + sb.toString());
					return;
				}
				
				if(ally == clan){
					sender.sendMessage(ChatColor.RED + "You cannot ally yourself!");
					return;
				}
				
				//Try approve an old ally invite first
				AllyRequest req = allyRequests.get(clan);
				if(req != null){
					if(req.hasExpired()){
						allyRequests.remove(clan);
					}
					else if(req.getAsker() == ally){
						if(clan.getEnemies().contains(ally)){
							clan.removeEnemy(ally);
						}
						if(ally.getEnemies().contains(clan)){
							ally.removeEnemy(clan);
						}
						
						clan.addAlly(ally);
						ally.addAlly(clan);
						
						allyRequests.remove(clan);
						
						req.getAsked().sendMessage(ChatColor.GREEN + "You are now allied with " + req.getAsker());
						req.getAsker().sendMessage(ChatColor.GREEN + "You are now allied with " + req.getAsked());
						return;
					}
				}
				
				if(clan.getAllies().contains(ally)){
					sender.sendMessage(ChatColor.RED + "They are already an ally!");
					return;
				}
				
				if(ally.getOnline(2).isEmpty()){
					sender.sendMessage(ChatColor.RED + "Nobody in " + ally.getName() + " who is online has a high enough rank to accept - try later!");
					return;
				}
				
				AllyRequest old = allyRequests.get(ally);
				if(old != null && !old.hasExpired()){
					sender.sendMessage(ChatColor.RED + "Your previous ally request has not expired yet.");
					return;
				}
				
				AllyRequest request = new AllyRequest(clan, ally);
				allyRequests.put(ally, request);
				clan.sendMessage(ChatColor.GREEN + user.getName() + " has sent an ally request to " + ally.getName());
				ally.sendMessage(ChatColor.GREEN + clan.getName() + " has sent an ally request. To accept, type /clan ally "+clan.getName(), 2);
			}
		};
		ally.console = false;
		ally.needsClan = true;
		ally.num_args = 1;
		ally.rank = 2;
		ally.perm = "maxclans.clan.ally";
		ally.usage = "/clan ally";
		ally.description = "Sets another clan as an ally";
		
		commands.put("ally", ally);
		
		//Neutral command
		BaseCommand neutral = new BaseCommand(){
			@Override
			public void onRun(CommandSender sender, String[] args) {
				Player p = (Player) sender;
				
				ClanMember user = ClanManager.getClanMember(p.getName());
				Clan clan = user.getClan();
				
				StringBuilder sb = new StringBuilder(args[1]);
				for(int i = 2; i < args.length; i++){
					sb.append(" " + args[i]);
				}
				
				Clan neutral = ClanManager.getClan(sb.toString(), true);
				
				if(neutral == null){
					sender.sendMessage(ChatColor.RED + "No such clan found: " + sb.toString());
					return;
				}
				
				if(neutral == clan){
					sender.sendMessage(ChatColor.RED + "You cannot neutral yourself!");
					return;
				}
				
				if(clan.isNeutral(neutral)){
					sender.sendMessage(ChatColor.RED + "You are already neutral towards them!");
					return;
				}
				
				if(clan.getAllies().contains(neutral)){
					clan.removeAlly(neutral);
				}
				if(clan.getEnemies().contains(neutral)){
					clan.removeEnemy(neutral);
				}
				
				if(neutral.getAllies().contains(clan)){
					neutral.removeAlly(clan);
				}

				clan.sendMessage(ChatColor.GREEN + user.getName() + " has set the clans stance against " + neutral + " to neutral.");
				neutral.sendMessage(ChatColor.GREEN + clan.getName() + " has set their stance towards " + neutral + " to neutral.");
			}
		};
		neutral.console = false;
		neutral.needsClan = true;
		neutral.num_args = 1;
		neutral.rank = 2;
		neutral.perm = "maxclans.clan.neutral";
		neutral.usage = "/clan neutral <clan>";
		neutral.description = "Sets another clan as neutral";
		
		commands.put("neutral", neutral);
		
		//deposit command
		BaseCommand deposit = new BaseCommand(){
			@Override
			public void onRun(CommandSender sender, String[] args) {
				Player p = (Player) sender;
				
				double amount;
				try{
					amount = Double.parseDouble(args[1]);
				}
				catch(NumberFormatException e){
					sender.sendMessage(ChatColor.RED + "Invalid amount specified.");
					return;
				}
				if(amount < 0){
					sender.sendMessage(ChatColor.RED + "Amounts must be positive.");
					return;
				}
				ClanMember user = ClanManager.getClanMember(p.getName());
				if(user.clanDeposit(amount)){
					sender.sendMessage(ChatColor.GREEN + "Deposited " + format(amount) + " into clan bank.");
					user.getClan().sendMessage(ChatColor.GREEN + user.getName() + " deposited " + format(amount) + " into the clan bank.", 0, user);
				}
				else{
					sender.sendMessage(ChatColor.RED + "Failed to deposit " + format(amount) + " into clan bank. Do you have enough money?");
				}
				
			}
		};
		deposit.console = false;
		deposit.needsClan = true;
		deposit.num_args = 1;
		deposit.rank = 0;
		deposit.perm = "maxclans.clan.deposit";
		deposit.usage = "/clan deposit <amount>";
		deposit.description = "Deposits an amount into the clan bank";
		deposit.show = getEcon() != null;
		
		commands.put("deposit", deposit);
		
		//Withdraw command
		BaseCommand withdraw = new BaseCommand(){
			@Override
			public void onRun(CommandSender sender, String[] args) {
				Player p = (Player) sender;
				
				double amount;
				try{
					amount = Double.parseDouble(args[1]);
				}
				catch(NumberFormatException e){
					sender.sendMessage(ChatColor.RED + "Invalid amount specified.");
					return;
				}
				if(amount < 0){
					sender.sendMessage(ChatColor.RED + "Amounts must be positive.");
					return;
				}
				ClanMember user = ClanManager.getClanMember(p.getName());
				if(user.clanWithdraw(amount)){
					sender.sendMessage(ChatColor.GREEN + "Withdrew " + format(amount) + " from the clan bank.");
					user.getClan().sendMessage(ChatColor.GREEN + user.getName() + " withdrew " + format(amount) + " from the clan bank.", 0, user);
				}
				else{
					sender.sendMessage(ChatColor.RED + "Failed to withdraw " + format(amount) + " from clan bank. Does the clan have enough money?");
				}
			}
		};
		withdraw.console = false;
		withdraw.needsClan = true;
		withdraw.num_args = 1;
		withdraw.rank = 2;
		withdraw.perm = "maxclans.clan.withdraw";
		withdraw.usage = "/clan withdraw <amount>";
		withdraw.description = "Withdraws cash from the clan bank";
		withdraw.show = getEcon() != null;
		
		commands.put("withdraw", withdraw);
		
		//money command
		BaseCommand money = new BaseCommand(){
			@Override
			public void onRun(CommandSender sender, String[] args) {
				Player p = (Player) sender;
				ClanMember cm = ClanManager.getClanMember(p);
				p.sendMessage(ChatColor.DARK_GREEN + "Clan Balance: " + ChatColor.WHITE + format(cm.getClan().getBalance()));
			}
		};
		money.console = false;
		money.needsClan = true;
		money.num_args = 0;
		money.rank = 0;
		money.perm = "maxclans.clan.money";
		money.usage = "/clan money";
		money.description = "View clan balance";
		money.show = false;
		
		commands.put("money", money);
		addAlias("money", "$", "balance");
		
		//Open command
		BaseCommand open = new BaseCommand(){
			@Override
			public void onRun(CommandSender sender, String[] args) {
				Player p = (Player) sender;
				
				ClanMember user = ClanManager.getClanMember(p);
				Clan clan = user.getClan();
				
				if(args.length > 1){
					String state = args[1].toLowerCase();
					if(state.equals("on") || state.equals("enable")){
						clan.setOpen(true);
						clan.update();
					}
					else if(state.equals("off") || state.equals("disable")){
						clan.setOpen(false);
						clan.update();
					}
					else{
						sender.sendMessage(ChatColor.RED + "Invalid state given. Valid: On|Off or Enable|Disable.");
						return;
					}
				}
				else{
					clan.setOpen(!clan.isOpen());
					clan.update();
				}
				clan.sendMessage(ChatColor.GREEN + user.getName() + " has set the clan's state to " + (clan.isOpen() ? "OPEN. Anyone may join." : "CLOSED. Only invited players may join."));
			}
		};
		open.console = false;
		open.needsClan = true;
		open.num_args = 0;
		open.rank = 2;
		open.perm = "maxclans.clan.open";
		open.usage = "/clan open On|Off";
		open.description = "Let players join using /clan join or not"; 
		open.show = true;
		
		commands.put("open", open);
		
		//Join command
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
				if(!clan.isOpen()){
					sender.sendMessage(ChatColor.RED + "You may not join that clan. It is invite only.");
					return;
				}
				
				ClanMember user = ClanManager.getClanMember(p);
				if(user == null) user = ClanManager.createClanMember(p);
				else if(user.getClan() != null){
					sender.sendMessage(ChatColor.RED + "You are already a member of a clan.");
					return;
				}
				
				clan.add(user);
				clan.sendMessage(ChatColor.YELLOW + user.getName() + " has joined " + ChatColor.GREEN + clan.getName() + ChatColor.YELLOW + "!");
				user.update();
			}
		};
		join.console = false;
		join.needsClan = false;
		join.noClan = true;
		join.num_args = 1;
		join.rank = 0;
		join.perm = "maxclans.clan.join";
		join.usage = "/clan join <ClanName>";
		join.description = "Joins a clan if it is open.";
		join.show = true;
		
		commands.put("join", join);
		addAlias("join", "j");
		
		//motd command
		BaseCommand motd = new BaseCommand(){
			@Override
			public void onRun(CommandSender sender, String[] args) {
				Player p = (Player) sender;
				
				ClanMember cm = ClanManager.getClanMember(p);
				if(cm.getRank() < 2 || args.length < 2){
					p.sendMessage(ChatColor.AQUA + "Motd: " + cm.getClan().getMotd());
					return;
				}
				
				StringBuilder sb = new StringBuilder(args[1]);
				for(int i = 2; i < args.length; i++){
					sb.append(" " + args[i]);
				}
				
				String motd = sb.toString();
				cm.getClan().setMotd(motd);
				cm.getClan().sendMessage(ChatColor.AQUA + "Motd: " + cm.getClan().getMotd());
				cm.getClan().update();
			}
		};
		motd.console = false;
		motd.needsClan = true;
		motd.num_args = 0;
		motd.rank = 0;
		motd.perm = "maxclans.clan.motd";
		motd.usage = "/clan motd [new Motd]";
		motd.description = "Changes clan login message.";
		motd.show = true;
		
		commands.put("motd", motd);
		addAlias("motd", "motto");
		
		//Chest command
		BaseCommand chest = new BaseCommand(){
			@Override
			public void onRun(CommandSender sender, String[] args) {
				Player p = (Player) sender;
				
				if(MaxClans.instance.isBlacklisted(p.getWorld())){
					sender.sendMessage(ChatColor.RED + "You can't do that in this world.");
					return;
				}
				
				KarmaPlayer kp = MaxPvP.instance.getKarmaPlayer(p);
				if(kp != null && kp.isHostile()){
					p.sendMessage(ChatColor.RED + "You can't do that while hostile!");
					return;
				}
				
				ClanMember cm = ClanManager.getClanMember(p);
				ClanChest cc = cm.getClan().getClanChest();
				cc.addViewer(p);
				cc.setChanged();
				p.openInventory(cc.getInventory());
			}
		};
		chest.console = false;
		chest.needsClan = true;
		chest.num_args = 0;
		chest.rank = 2;
		chest.perm = "maxclans.clan.chest";
		chest.usage = "/clan chest";
		chest.description = "Opens your clans chest.";
		chest.show = true;
		
		commands.put("chest", chest);
		
		//Respawn command
		BaseCommand respawn = new BaseCommand(){
			@Override
			public void onRun(CommandSender sender, String[] args) {
				Player p = (Player) sender;
				Clan clan = ClanManager.getClanMember(p).getClan();
				if(args.length > 1){
					String subArg = args[1].toLowerCase();
					if(subArg.equals("on") || subArg.equals("enable") || subArg.equals("true")){
						clan.setRespawn(true);
					}
					else if(subArg.equals("off") || subArg.equals("disable") || subArg.equals("false")){
						clan.setRespawn(false);
					}
					else{
						sender.sendMessage(ChatColor.RED + "Invalid option supplied. Valid: On|Off, Enable|Disable, True|False");
						return;
					}
				}
				else{
					clan.setRespawn(!clan.respawn()); //Toggle
				}
				if(clan.respawn()){
					sender.sendMessage(ChatColor.GREEN + "Clan members who die greater than 30m from your clan spawn will now respawn at your clan spawn.");
				}
				else{
					sender.sendMessage(ChatColor.GREEN + "Clan members who die will no longer respawn at your clan spawn.");
				}
				clan.update();
			}
		};
		respawn.console = false;
		respawn.needsClan = true;
		respawn.num_args = 0;
		respawn.rank = 2;
		respawn.perm = "maxclans.clan.respawn";
		respawn.usage = "/clan respawn On|Off";
		respawn.description = "Lets players go to clan spawn on death";
		respawn.show = true;
		
		commands.put("respawn", respawn);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		try{
			BaseCommand com = null;			
			if(args.length > 0){
				args[0] = args[0].toLowerCase();
				com = this.commands.get(args[0]);
				if(com == null){
					String cmdName = this.aliases.get(args[0]);
					if(cmdName != null){
						//Has alias
						com = this.commands.get(cmdName);
					}
				}
			}
			if(com == null){
				sendHelp(sender);
			}
			else{
				com.run(sender, args);
			}
		}
		catch(Exception e){
			sender.sendMessage(ChatColor.RED + "Something went wrong executing your command.");
			if(MaxClans.debug){
				StringBuilder sb = new StringBuilder(label);
				for(int i = 0; i < args.length; i++){
					sb.append(" " + args[i]);
				}
				MaxClans.error("Exception executing: " + sb.toString());
				MaxClans.error(e);
			}
		}
		catch(Error e){
			if(MaxClans.debug){
				StringBuilder sb = new StringBuilder(label);
				for(int i = 0; i < args.length; i++){
					sb.append(" " + args[i]);
				}
				MaxClans.error("Error executing: " + sb.toString());
				MaxClans.error(e);
				sender.sendMessage(ChatColor.RED + "Something went wrong with your command.");
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
	
	private void addAlias(String command, String... aliases){
		command = command.toLowerCase();
		for(String s : aliases){
			this.aliases.put(s.toLowerCase(), command);
		}
	}
	
	private class Invite{
		ClanMember inviter;
		Clan clan;
		String name; //Name of invited
		long created;
		
		public Invite(ClanMember cm, String invited){
			inviter = cm;
			clan = cm.getClan();
			name = invited;
			created = System.currentTimeMillis();
		}
		
		public ClanMember getInviter(){
			return inviter;
		}
		public Clan getClan(){
			return clan;
		}
		public String getName(){
			return name;
		}
		public boolean hasExpired(){
			return System.currentTimeMillis() > created + 60000;
		}
	}
	
	private class AllyRequest{
		Clan asker;
		Clan asked;
		long created;
		
		public AllyRequest(Clan asker, Clan asked){
			this.asker = asker;
			this.asked = asked;
			created = System.currentTimeMillis();
		}
		
		public Clan getAsker(){
			return asker;
		}
		public Clan getAsked(){
			return asked;
		}
		public boolean hasExpired(){
			return System.currentTimeMillis() > created + 60000;
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