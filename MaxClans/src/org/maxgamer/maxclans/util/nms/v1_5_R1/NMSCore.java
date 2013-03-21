package org.maxgamer.maxclans.util.nms.v1_5_R1;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.maxgamer.maxclans.util.nms.NMSDependent;

public class NMSCore extends NMSDependent{
	
	@Override
	public Inventory getInventory(byte[] bytes, String name, int size){
		//Fetch the String and convert it back to a byte[], then decompress it into a NBTTagCompound.
		net.minecraft.server.v1_5_R1.NBTTagCompound c = net.minecraft.server.v1_5_R1.NBTCompressedStreamTools.a(bytes);
		//The tag compound contained a list called "inventory"
		net.minecraft.server.v1_5_R1.NBTTagList list = c.getList("inventory");
		
		Inventory inv = Bukkit.createInventory(null, size, name);
		
		//Reconstruct the items from their NBT tags
		for(int i = 0; i < list.size(); i++){
			//Fetch the next nbt item in the list
			net.minecraft.server.v1_5_R1.NBTTagCompound item = (net.minecraft.server.v1_5_R1.NBTTagCompound) list.get(i);
			//The item has a tag "index", where it is stored in the chest
			int index = item.getInt("index");
			//Create an itemstack from the nbt tag
			net.minecraft.server.v1_5_R1.ItemStack is = net.minecraft.server.v1_5_R1.ItemStack.createStack(item);
			//Convert that itemstack into a craftbukkit itemstack
			//org.bukkit.craftbukkit.v1_4_6.inventory.CraftItemStack cis = new org.bukkit.craftbukkit.v1_4_6.inventory.CraftItemStack(is);
			ItemStack cis = org.bukkit.craftbukkit.v1_5_R1.inventory.CraftItemStack.asBukkitCopy(is);
			//Put the itemstack in the inventory.
			inv.setItem(index, cis);
		}
		return inv;
	}

	@Override
	public byte[] getBytes(Inventory inv) {
		//Blank NBT tag
		net.minecraft.server.v1_5_R1.NBTTagCompound c = new net.minecraft.server.v1_5_R1.NBTTagCompound();
		//Blank NBT list
		net.minecraft.server.v1_5_R1.NBTTagList list = new net.minecraft.server.v1_5_R1.NBTTagList();
		
		//Iterate over the inventory contents
		for(int index = 0; index < inv.getContents().length; index++){
			//Convert it to a craftitem stack
			org.bukkit.craftbukkit.v1_5_R1.inventory.CraftItemStack cis = (org.bukkit.craftbukkit.v1_5_R1.inventory.CraftItemStack) inv.getItem(index);
			if(cis != null){ //If cis == null, no item is there, ignore it.
				//Convert it to a NMS itemstack
				net.minecraft.server.v1_5_R1.ItemStack is = org.bukkit.craftbukkit.v1_5_R1.inventory.CraftItemStack.asNMSCopy(cis);
				//Save the NMS itemstack to a new NBT tag
				net.minecraft.server.v1_5_R1.NBTTagCompound itemCompound = new net.minecraft.server.v1_5_R1.NBTTagCompound();
				itemCompound = is.save(itemCompound);
				
				//Set the position of the item in the chest under "index"
				itemCompound.set("index", new net.minecraft.server.v1_5_R1.NBTTagInt("index", index));
				//Add the item to the NBT list of items
				list.add(itemCompound);
			}
		}
		//Put the list in the blank tag
		c.set("inventory", list);
		
		//Convert the NBT tag to a byte[]
		byte[] bytes = net.minecraft.server.v1_5_R1.NBTCompressedStreamTools.a(c);
		//Convert & escape the byte[] to a string
		return bytes;
	}
}