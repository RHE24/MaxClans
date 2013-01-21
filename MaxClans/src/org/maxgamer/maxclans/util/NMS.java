package org.maxgamer.maxclans.util;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class NMS{
	private static HashMap<String, NMSDependent> dependents = new HashMap<String, NMSDependent>();
	
	static{
		NMSDependent dep;
		
		/* ***********************
		 * **       1.4       ** *
		 * ***********************/
		try{
			dep = new NMSDependent(){
				@Override
				public Inventory getInventory(byte[] bytes, String name, int size){
					//Fetch the String and convert it back to a byte[], then decompress it into a NBTTagCompound.
					net.minecraft.server.NBTTagCompound c = net.minecraft.server.NBTCompressedStreamTools.a(bytes);
					//The tag compound contained a list called "inventory"
					net.minecraft.server.NBTTagList list = c.getList("inventory");
					
					Inventory inv = Bukkit.createInventory(null, size, name);
					
					//Reconstruct the items from their NBT tags
					for(int i = 0; i < list.size(); i++){
						//Fetch the next nbt item in the list
						net.minecraft.server.NBTTagCompound item = (net.minecraft.server.NBTTagCompound) list.get(i);
						//The item has a tag "index", where it is stored in the chest
						int index = item.getInt("index");
						//Create an itemstack from the nbt tag
						net.minecraft.server.ItemStack is = net.minecraft.server.ItemStack.a(item);
						//Convert that itemstack into a craftbukkit itemstack
						ItemStack cis = org.bukkit.craftbukkit.inventory.CraftItemStack.asBukkitStack(is);
						//Put the itemstack in the inventory.
						inv.setItem(index, cis);
					}
					return inv;
				}
	
				@Override
				public byte[] getBytes(Inventory inv) {
					//Blank NBT tag
					net.minecraft.server.NBTTagCompound c = new net.minecraft.server.NBTTagCompound();
					//Blank NBT list
					net.minecraft.server.NBTTagList list = new net.minecraft.server.NBTTagList();
					
					//Iterate over the inventory contents
					for(int index = 0; index < inv.getContents().length; index++){
						//Convert it to a craftitem stack
						org.bukkit.craftbukkit.inventory.CraftItemStack cis = (org.bukkit.craftbukkit.inventory.CraftItemStack) inv.getItem(index);
						if(cis != null){ //If cis == null, no item is there, ignore it.
							//Convert it to a NMS itemstack
							net.minecraft.server.ItemStack is = org.bukkit.craftbukkit.inventory.CraftItemStack.createNMSItemStack(cis);
							//Save the NMS itemstack to a new NBT tag
							net.minecraft.server.NBTTagCompound itemCompound = new net.minecraft.server.NBTTagCompound();
							itemCompound = is.save(itemCompound);
							
							//Set the position of the item in the chest under "index"
							itemCompound.set("index", new net.minecraft.server.NBTTagInt("index", index));
							//Add the item to the NBT list of items
							list.add(itemCompound);
						}
					}
					//Put the list in the blank tag
					c.set("inventory", list);
					
					//Convert the NBT tag to a byte[]
					byte[] bytes = net.minecraft.server.NBTCompressedStreamTools.a(c);
					//Convert & escape the byte[] to a string
					return bytes;
				}
			};
			dependents.put("", dep);
		}
		catch(Exception e){}
		catch(Error e){}
		
		try{
			/* ***********************
			 * **      1.4.5      ** *
			 * ***********************/
			dep = new NMSDependent(){
				@Override
				public Inventory getInventory(byte[] bytes, String name, int size){
					//Fetch the String and convert it back to a byte[], then decompress it into a NBTTagCompound.
					net.minecraft.server.v1_4_5.NBTTagCompound c = net.minecraft.server.v1_4_5.NBTCompressedStreamTools.a(bytes);
					//The tag compound contained a list called "inventory"
					net.minecraft.server.v1_4_5.NBTTagList list = c.getList("inventory");
					
					Inventory inv = Bukkit.createInventory(null, size, name);
					
					//Reconstruct the items from their NBT tags
					for(int i = 0; i < list.size(); i++){
						//Fetch the next nbt item in the list
						net.minecraft.server.v1_4_5.NBTTagCompound item = (net.minecraft.server.v1_4_5.NBTTagCompound) list.get(i);
						//The item has a tag "index", where it is stored in the chest
						int index = item.getInt("index");
						//Create an itemstack from the nbt tag
						net.minecraft.server.v1_4_5.ItemStack is = net.minecraft.server.v1_4_5.ItemStack.a(item);
						//Convert that itemstack into a craftbukkit itemstack
						ItemStack cis = org.bukkit.craftbukkit.v1_4_5.inventory.CraftItemStack.asBukkitStack(is);
						//Put the itemstack in the inventory.
						inv.setItem(index, cis);
					}
					return inv;
				}
	
				@Override
				public byte[] getBytes(Inventory inv) {
					//Blank NBT tag
					net.minecraft.server.v1_4_5.NBTTagCompound c = new net.minecraft.server.v1_4_5.NBTTagCompound();
					//Blank NBT list
					net.minecraft.server.v1_4_5.NBTTagList list = new net.minecraft.server.v1_4_5.NBTTagList();
					
					//Iterate over the inventory contents
					for(int index = 0; index < inv.getContents().length; index++){
						//Convert it to a craftitem stack
						org.bukkit.craftbukkit.v1_4_5.inventory.CraftItemStack cis = (org.bukkit.craftbukkit.v1_4_5.inventory.CraftItemStack) inv.getItem(index);
						if(cis != null){ //If cis == null, no item is there, ignore it.
							//Convert it to a NMS itemstack
							net.minecraft.server.v1_4_5.ItemStack is = org.bukkit.craftbukkit.v1_4_5.inventory.CraftItemStack.createNMSItemStack(cis);
							//Save the NMS itemstack to a new NBT tag
							net.minecraft.server.v1_4_5.NBTTagCompound itemCompound = new net.minecraft.server.v1_4_5.NBTTagCompound();
							itemCompound = is.save(itemCompound);
							
							//Set the position of the item in the chest under "index"
							itemCompound.set("index", new net.minecraft.server.v1_4_5.NBTTagInt("index", index));
							//Add the item to the NBT list of items
							list.add(itemCompound);
						}
					}
					//Put the list in the blank tag
					c.set("inventory", list);
					
					//Convert the NBT tag to a byte[]
					byte[] bytes = net.minecraft.server.v1_4_5.NBTCompressedStreamTools.a(c);
					//Convert & escape the byte[] to a string
					return bytes;
				}
			};
			dependents.put("v1_4_5", dep);
		}
		catch(Exception e){}
		catch(Error e){}
		
		try{
			/* ***********************
			 * **      1.4.6      ** *
			 * ***********************/
			dep = new NMSDependent(){
				@Override
				public Inventory getInventory(byte[] bytes, String name, int size){
					//Fetch the String and convert it back to a byte[], then decompress it into a NBTTagCompound.
					net.minecraft.server.v1_4_6.NBTTagCompound c = net.minecraft.server.v1_4_6.NBTCompressedStreamTools.a(bytes);
					//The tag compound contained a list called "inventory"
					net.minecraft.server.v1_4_6.NBTTagList list = c.getList("inventory");
					
					Inventory inv = Bukkit.createInventory(null, size, name);
					
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
						//Put the itemstack in the inventory.
						inv.setItem(index, cis);
					}
					return inv;
				}
	
				@Override
				public byte[] getBytes(Inventory inv) {
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
					return bytes;
				}
			};
			dependents.put("v1_4_6", dep);
		}
		catch(Exception e){}
		catch(Error e){}
		
		try{
			/* ***********************
			 * **      1.4.7      ** *
			 * ***********************/
			dep = new NMSDependent(){
				@Override
				public Inventory getInventory(byte[] bytes, String name, int size){
					//Fetch the String and convert it back to a byte[], then decompress it into a NBTTagCompound.
					net.minecraft.server.v1_4_R1.NBTTagCompound c = net.minecraft.server.v1_4_R1.NBTCompressedStreamTools.a(bytes);
					//The tag compound contained a list called "inventory"
					net.minecraft.server.v1_4_R1.NBTTagList list = c.getList("inventory");
					
					Inventory inv = Bukkit.createInventory(null, size, name);
					
					//Reconstruct the items from their NBT tags
					for(int i = 0; i < list.size(); i++){
						//Fetch the next nbt item in the list
						net.minecraft.server.v1_4_R1.NBTTagCompound item = (net.minecraft.server.v1_4_R1.NBTTagCompound) list.get(i);
						//The item has a tag "index", where it is stored in the chest
						int index = item.getInt("index");
						//Create an itemstack from the nbt tag
						net.minecraft.server.v1_4_R1.ItemStack is = net.minecraft.server.v1_4_R1.ItemStack.createStack(item);
						//Convert that itemstack into a craftbukkit itemstack
						//org.bukkit.craftbukkit.v1_4_6.inventory.CraftItemStack cis = new org.bukkit.craftbukkit.v1_4_6.inventory.CraftItemStack(is);
						ItemStack cis = org.bukkit.craftbukkit.v1_4_R1.inventory.CraftItemStack.asBukkitCopy(is);
						//Put the itemstack in the inventory.
						inv.setItem(index, cis);
					}
					return inv;
				}
	
				@Override
				public byte[] getBytes(Inventory inv) {
					//Blank NBT tag
					net.minecraft.server.v1_4_R1.NBTTagCompound c = new net.minecraft.server.v1_4_R1.NBTTagCompound();
					//Blank NBT list
					net.minecraft.server.v1_4_R1.NBTTagList list = new net.minecraft.server.v1_4_R1.NBTTagList();
					
					//Iterate over the inventory contents
					for(int index = 0; index < inv.getContents().length; index++){
						//Convert it to a craftitem stack
						org.bukkit.craftbukkit.v1_4_R1.inventory.CraftItemStack cis = (org.bukkit.craftbukkit.v1_4_R1.inventory.CraftItemStack) inv.getItem(index);
						if(cis != null){ //If cis == null, no item is there, ignore it.
							//Convert it to a NMS itemstack
							net.minecraft.server.v1_4_R1.ItemStack is = org.bukkit.craftbukkit.v1_4_R1.inventory.CraftItemStack.asNMSCopy(cis);
							//Save the NMS itemstack to a new NBT tag
							net.minecraft.server.v1_4_R1.NBTTagCompound itemCompound = new net.minecraft.server.v1_4_R1.NBTTagCompound();
							itemCompound = is.save(itemCompound);
							
							//Set the position of the item in the chest under "index"
							itemCompound.set("index", new net.minecraft.server.v1_4_R1.NBTTagInt("index", index));
							//Add the item to the NBT list of items
							list.add(itemCompound);
						}
					}
					//Put the list in the blank tag
					c.set("inventory", list);
					
					//Convert the NBT tag to a byte[]
					byte[] bytes = net.minecraft.server.v1_4_R1.NBTCompressedStreamTools.a(c);
					//Convert & escape the byte[] to a string
					return bytes;
				}
			};
			dependents.put("v1_4_R1", dep);
		}
		catch(Exception e){}
		catch(Error e){}
	}
	
	/** The known working NMSDependent. This will be null if we haven't found one yet. */
	private static NMSDependent nms;
	
	/**
	 * Converts the given inventory into a compressed byte[].
	 * @param inv The inventory to convert
	 * @return The byte[] representation.
	 * @throws ClassNotFoundException If this version of maxclans isnt up to date enough with bukkit
	 */
	public static byte[] getBytes(Inventory inv) throws ClassNotFoundException{
		if(nms == null){ //We haven't found it yet.
			for(NMSDependent dep : dependents.values()){
				try{
					byte[] bytes = dep.getBytes(inv);
					nms = dep; //If we made it this far, we've found a working version.
					return bytes; //End of loop.
				}
				catch(Exception e){}
				catch(Error e){}
			}
			throw new ClassNotFoundException("This version of MaxClans is incompatible."); //We haven't got code to support your version!
		}
		else{ //We have a known getter
			return nms.getBytes(inv);
		}
	}
	
	public static Inventory getInventory(byte[] bytes, String name, int size) throws ClassNotFoundException{
		if(nms == null){ //We haven't found it yet.
			for(NMSDependent dep : dependents.values()){
				System.out.println("Searching...");
				try{
					Inventory inv = dep.getInventory(bytes, name, size);
					nms = dep; //If we made it this far, we've found a working version.
					return inv; //End of loop.
				}
				catch(Exception e){
					e.printStackTrace();
				}
				catch(Error e){
					e.printStackTrace();
				}
			}
			throw new ClassNotFoundException("This version of MaxClans is incompatible."); //We haven't got code to support your version!
		}
		else{ //We have a known getter
			return nms.getInventory(bytes, name, size);
		}
	}
	
	private interface NMSDependent{
		public byte[] getBytes(Inventory inv);
		public Inventory getInventory(byte[] bytes, String name, int size);
	}
}