package org.maxgamer.maxclans.util.nms;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

public abstract class NMSDependent{
	/** Returns true if this can be used as a NMS version */
	public boolean isValid(){
		String bukkit = Bukkit.getServer().getClass().getPackage().getName();
		bukkit = bukkit.substring(bukkit.lastIndexOf(".") + 1);
		
		String me = this.getClass().getPackage().getName();
		me = me.substring(bukkit.lastIndexOf(".") + 1);
		
		if(me.equals(bukkit)) return true;
		else return false;
	}
	
	public abstract byte[] getBytes(Inventory inv);
	public abstract Inventory getInventory(byte[] bytes, String name, int size);
	
}