package org.maxgamer.maxclans.listener;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.maxgamer.MaxPvP.ValidKillEvent;
import org.maxgamer.maxclans.clan.Clan;
import org.maxgamer.maxclans.clan.ClanManager;
import org.maxgamer.maxclans.clan.ClanMember;

public class MaxPvPListener extends MaxClansListener{
	@EventHandler(priority = EventPriority.MONITOR)
	public void onKill(ValidKillEvent e){
		if(e.isCancelled()) return;
		
		ClanMember cmKiller = ClanManager.getClanMember(e.getKiller());
		ClanMember cmVictim = ClanManager.getClanMember(e.getVictim());
		
		if(cmKiller != null && cmKiller.getClan() != null){
			Clan clan = cmKiller.getClan();
			
			clan.addKill();
			clan.update();
		}
		
		if(cmVictim != null && cmVictim.getClan() != null){
			Clan clan = cmVictim.getClan();
			clan.addDeath();
			clan.update();
		}
		
		if(cmKiller != null && cmVictim != null){
			Clan cKiller = cmKiller.getClan();
			Clan cVictim = cmVictim.getClan();
			
			if(cKiller == null || cVictim == null) return; //One of them has no clan. They cannot be at war.
			
			if(cKiller.isEnemy(cVictim)){
				//TODO: The victim was killed by an enemy clan member
				e.getKiller().sendMessage(ChatColor.RED + "Killed by an enemy clan member!");
			}
			if(cVictim.isEnemy(cKiller)){
				//TODO: The killer killed an enemy clan member
				e.getKiller().sendMessage(ChatColor.GREEN + "Killed an enemy clan member!");
			}
		}
	}
}