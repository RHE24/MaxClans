package org.maxgamer.maxclans.util;
import java.util.regex.Pattern;

import org.bukkit.inventory.Inventory;

public class Util{	
	private static Pattern VALID_CHARS_PATTERN = Pattern.compile("[A-Za-z0-9_ ]");
	/**
	 * Returns a string containing all characters that aren't A-Z, a-z, 0-9 or _. 
	 * Never returns a null string.
	 * @param s The string to check
	 * @return The string of invalid characters or an empty string if it is valid.
	 */
	public static String getInvalidChars(String s){
		return VALID_CHARS_PATTERN.matcher(s).replaceAll("");
	}
	
	public static <T> void shuffle(T[] values, int n){
		for(int i = values.length - 1; i > n; i--){
			values[i] = values[i - 1];
		}
		values[n] = null;
	}
	
	/**
	 * Converts the given byte[] into a new inventory.
	 * @param bytes The byte[] representation of the inventory, see getBytes(Inventory inv);
	 * @param name The name of the inventory to be created
	 * @param size The size of the inventory to be created
	 * @return the inventory
	 * @throws ClassNotFoundException If this version of MaxClans is not updated enough
	 */
	public static Inventory getInventory(byte[] bytes, String name, int size) throws ClassNotFoundException{
		return NMS.getInventory(bytes, name, size);
	}
	/**
	 * Converts the given Inventory into a byte[] for storage
	 * @param inv The inventory to convert
	 * @return The byte[]
	 * @throws ClassNotFoundException If this version of MaxClans is not updated enough
	 */
	public static byte[] getBytes(Inventory inv) throws ClassNotFoundException{
		return NMS.getBytes(inv);
	}
}