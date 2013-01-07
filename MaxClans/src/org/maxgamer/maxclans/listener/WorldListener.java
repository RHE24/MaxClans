package org.maxgamer.maxclans.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.maxgamer.maxclans.MaxClans;

public class WorldListener extends MaxClansListener{
	@EventHandler(priority = EventPriority.MONITOR)
	public void onWorldLoad(WorldLoadEvent e){
		MaxClans.instance.parseConfig();
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onWorldUnload(WorldUnloadEvent e){
		MaxClans.instance.parseConfig();
	}
}