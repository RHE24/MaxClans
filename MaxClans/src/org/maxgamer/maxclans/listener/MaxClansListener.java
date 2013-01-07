package org.maxgamer.maxclans.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.maxgamer.maxclans.MaxClans;

public class MaxClansListener implements Listener{
	public void register(){
		Bukkit.getPluginManager().registerEvents(this, MaxClans.instance);
	}
	public void unregister(){
		Bukkit.getPluginManager().registerEvents(this, MaxClans.instance);
	}
}