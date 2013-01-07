package org.maxgamer.maxclans.clan;

import java.util.HashSet;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.maxgamer.maxclans.MaxClans;

public class ClanChest{
	private HashSet<Player> viewers = new HashSet<Player>();
	private Inventory inv;
	private Clan clan;
	private boolean hasChanged = false;
	
	private byte[] lastUpdate;
	
	/** Loads a previously existing chest. 
	 * @param load is the serialized version of the NBT string containing all items. */
	public ClanChest(Clan clan, byte[] load){
		this.clan = clan;
		//TODO: Convert to new NMS core when updating bukkit
		//The chest inventory
		inv = Bukkit.createInventory(null, MaxClans.instance.chestRows * 9, clan.getName() + " clan chest");
		//Fetch the String and convert it back to a byte[], then decompress it into a NBTTagCompound.
		net.minecraft.server.v1_4_6.NBTTagCompound c = net.minecraft.server.v1_4_6.NBTCompressedStreamTools.a(load);
		//The tag compound contained a list called "inventory"
		net.minecraft.server.v1_4_6.NBTTagList list = c.getList("inventory");
		
		//Reconstruct the items from their NBT tags
		for(int i = 0; i < list.size(); i++){
			//Fetch the next nbt item in the list
			net.minecraft.server.v1_4_6.NBTTagCompound item = (net.minecraft.server.v1_4_6.NBTTagCompound) list.get(i);
			//The item has a tag "index", where it is stored in the chest
			int index = item.getInt("index");
			//Create an itemstack from the nbt tag
			net.minecraft.server.v1_4_6.ItemStack is = net.minecraft.server.v1_4_6.ItemStack.a(item);
			//Convert that itemstack into a craftbukkit itemstack
			//org.bukkit.craftbukkit.v1_4_6.inventory.CraftItemStack cis = new org.bukkit.craftbukkit.v1_4_6.inventory.CraftItemStack(is);
			ItemStack cis = org.bukkit.craftbukkit.v1_4_6.inventory.CraftItemStack.asBukkitCopy(is);
			//Put the itemstack in the inventory.x
			inv.setItem(index, cis);
		}
		lastUpdate = load;
	}
	/** Returns a String representation of the items in this chest in NBT form */
	public byte[] getNBTBytes(){
		//Saving
		//Blank NBT tag
		net.minecraft.server.v1_4_6.NBTTagCompound c = new net.minecraft.server.v1_4_6.NBTTagCompound();
		//Blank NBT list
		net.minecraft.server.v1_4_6.NBTTagList list = new net.minecraft.server.v1_4_6.NBTTagList();
		
		//Iterate over the inventory contents
		for(int index = 0; index < inv.getContents().length; index++){
			//Convert it to a craftitem stack
			org.bukkit.craftbukkit.v1_4_6.inventory.CraftItemStack cis = (org.bukkit.craftbukkit.v1_4_6.inventory.CraftItemStack) inv.getItem(index);
			if(cis != null){ //If cis == null, no item is there, ignore it.
				//Convert it to a NMS itemstack
				net.minecraft.server.v1_4_6.ItemStack is = org.bukkit.craftbukkit.v1_4_6.inventory.CraftItemStack.asNMSCopy(cis);
				//Save the NMS itemstack to a new NBT tag
				net.minecraft.server.v1_4_6.NBTTagCompound itemCompound = new net.minecraft.server.v1_4_6.NBTTagCompound();
				itemCompound = is.save(itemCompound);
				
				//Set the position of the item in the chest under "index"
				itemCompound.set("index", new net.minecraft.server.v1_4_6.NBTTagInt("index", index));
				//Add the item to the NBT list of items
				list.add(itemCompound);
			}
		}
		//Put the list in the blank tag
		c.set("inventory", list);
		
		//Convert the NBT tag to a byte[]
		byte[] bytes = net.minecraft.server.v1_4_6.NBTCompressedStreamTools.a(c);
		//Convert & escape the byte[] to a string
		//return new String(bytes, "ISO-8859-1");
		return bytes;
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