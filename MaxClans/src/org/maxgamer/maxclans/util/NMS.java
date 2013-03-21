package org.maxgamer.maxclans.util;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.maxgamer.maxclans.MaxClans;
import org.maxgamer.maxclans.util.nms.NMSDependent;

public class NMS{
	/** The known working NMSDependent. This will be null if we haven't found one yet. */
	private static NMSDependent nms;
	
	static{
		String packageName = Bukkit.getServer().getClass().getPackage().getName();
		packageName = packageName.substring(packageName.lastIndexOf(".") + 1);
		
		try {
			Class<? extends NMSDependent> nmsClass = Class.forName("org.maxgamer.maxclans.util.nms." + packageName + ".NMSCore").asSubclass(NMSDependent.class);
			
			nms = nmsClass.newInstance();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			MaxClans.error("Error: Cannot load. I don't know how to run on this version of bukkit!");
			Bukkit.getPluginManager().disablePlugin(MaxClans.instance);
		} catch (InstantiationException e) {
			e.printStackTrace();
			MaxClans.error("Error: Cannot load. I don't know how to run on this version of bukkit!");
			Bukkit.getPluginManager().disablePlugin(MaxClans.instance);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			MaxClans.error("Error: Cannot load. I don't know how to run on this version of bukkit!");
			Bukkit.getPluginManager().disablePlugin(MaxClans.instance);
		}
	}
	
	/**
	 * Converts the given inventory into a compressed byte[].
	 * @param inv The inventory to convert
	 * @return The byte[] representation.
	 * @throws ClassNotFoundException If this version of maxclans isnt up to date enough with bukkit
	 */
	public static byte[] getBytes(Inventory inv) throws ClassNotFoundException{
		return nms.getBytes(inv);
	}
	
	public static Inventory getInventory(byte[] bytes, String name, int size) throws ClassNotFoundException{
		return nms.getInventory(bytes, name, size);
	}
}