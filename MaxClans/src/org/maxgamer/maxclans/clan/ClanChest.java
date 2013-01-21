package org.maxgamer.maxclans.clan;

import java.util.HashSet;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.maxgamer.maxclans.MaxClans;
import org.maxgamer.maxclans.util.Util;

public class ClanChest{
	private HashSet<Player> viewers = new HashSet<Player>();
	private Inventory inv;
	private Clan clan;
	private boolean hasChanged = false;
	
	private byte[] lastUpdate;
	
	/** Loads a previously existing chest. 
	 * @param load is the serialized version of the NBT string containing all items. */
	public ClanChest(Clan clan, byte[] load, int size){
		this.clan = clan;
		
		try{
			inv = Util.getInventory(load, clan.getName() + "'s Clan Chest", size);
		}
		catch(ClassNotFoundException e){
			e.printStackTrace();
			System.out.println("MaxClans is incompatible with this build of bukkit.");
		}
		lastUpdate = load;
	}
	/** Returns a String representation of the items in this chest in NBT form */
	public byte[] getNBTBytes(){
		try {
			byte[] bytes = Util.getBytes(inv);
			lastUpdate = bytes;
			return bytes;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.out.println("MaxClans is incompatible with this build of bukkit.");
			return lastUpdate;
		}
	}
	/** When using this method, you have to manually edit the record in the database.  The string part of the query can be fetched using ClanChest.getNBTString() and escaping it. */
	public ClanChest(Clan clan){
		this.clan = clan;
		inv = Bukkit.createInventory(null, MaxClans.instance.chestRows * 9, clan.getName() + " clan chest");
		lastUpdate = getNBTBytes(); //We can do this because the ClanManager creates us, and does its OWN query, instead of using this.update();
	}
	public HashSet<Player> getViewers(){
		return viewers;
	}
	/** Forces everyone viewing this inv to close it */
	public void close(){
		if(viewers.isEmpty()){
			return; //Nobody to close it for.
		}
		for(Player p : viewers){
			p.getOpenInventory().close();
		}
		viewers.clear();
	}
	public void addViewer(Player p){
		viewers.add(p);
	}
	public void removeViewer(Player p){
		if(viewers.remove(p)){
			this.setChanged();
			this.update();
		}
	}
	public Inventory getInventory(){
		return inv;
	}
	public Clan getClan(){
		return clan;
	}
	public boolean hasChanged(){
		return hasChanged;
	}
	public void setChanged(){
		hasChanged = true;
	}
	/** Equal to update(false); */
	public void update(){
		update(false);
	}
	/** IF force = true, it will update the inventory in the database even if it hasn't changed. */
	public void update(boolean force){
		if(!force && !hasChanged) return; //Hasnt changed, not forced.
		hasChanged = false;
		
		byte[] now = this.getNBTBytes();
		if(!force && lastUpdate.length == now.length && lastUpdate.equals(now)){
			//Nothing has changed
			System.out.println("Nothing has changed.");
			return;
		}
		lastUpdate = now;
		ClanManager.getDatabase().execute("UPDATE clans SET inv = ? WHERE name = ?", now, clan.getName());
	}
	
	/**
	 * Gives the player the entire contents of this chest.
	 * It returns a LinkedList of all the items it could not give the player.
	 * This method clears the chest, regardless of whether or not the player could fit
	 * all the items.  This method updates the chest contents in the database.
	 * @param p The player to give the items to.
	 */
	public LinkedList<ItemStack> giveContents(Player p){
		//Drop the clan items
		close();
		LinkedList<ItemStack> drop = new LinkedList<ItemStack>();
		for(ItemStack iStack : getInventory().getContents()){
			if(iStack == null) continue; //Empty
			drop.addAll(p.getInventory().addItem(iStack).values());
		}
		getInventory().clear();
		update(true);
		
		return drop;
	}
}