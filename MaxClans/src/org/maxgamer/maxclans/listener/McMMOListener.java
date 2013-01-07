package org.maxgamer.maxclans.listener;

import org.bukkit.event.EventHandler;
import org.maxgamer.maxclans.clan.ClanManager;
import org.maxgamer.maxclans.clan.ClanMember;

import com.gmail.nossr50.events.experience.McMMOPlayerLevelUpEvent;

public class McMMOListener extends MaxClansListener{
	@EventHandler
	public void onLevelUp(McMMOPlayerLevelUpEvent e){
		ClanMember cm = ClanManager.getClanMember(e.getPlayer());
		if(cm == null || cm.getClan() == null) return; //No clan.
		cm.getClan().addLevel();
		cm.getClan().update(); //TODO: Find a better place to do this!
	}
}