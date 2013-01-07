package org.maxgamer.maxclans.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.maxgamer.maxclans.MaxClans;
import org.maxgamer.maxclans.clan.Clan;
import org.maxgamer.maxclans.clan.ClanManager;
import org.maxgamer.maxclans.clan.ClanMember;
/** Tracks player connects & disconnects, and notifies Clan and ClanMember objects */
public class PlayerTracker extends MaxClansListener{
	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(PlayerJoinEvent e){
		final ClanMember cm = ClanManager.getClanMember(e.getPlayer().getName());
		if(cm == null) return;
		
		cm.setPlayer(e.getPlayer());
		if(cm.getClan() != null){
			cm.getClan().getOnline().put(e.getPlayer().getName(), cm);
			
			Bukkit.getScheduler().runTaskLater(MaxClans.instance, new Runnable(){
				@Override
				public void run(){
					cm.send(ChatColor.AQUA + "Clan Motd: " + cm.getClan().getMotd());
				}
			}, 40);
			
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onQuit(PlayerQuitEvent e){
		ClanMember cm = ClanManager.getClanMember(e.getPlayer().getName());
		if(cm == null) return;
		
		cm.setPlayer(null);
		
		if(cm.getClan() != null){
			Clan clan = cm.getClan();
			
			clan.getOnline().remove(e.getPlayer().getName());
			clan.getClanChest().removeViewer(e.getPlayer());
		}
	}
}