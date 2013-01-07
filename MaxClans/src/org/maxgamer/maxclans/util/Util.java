package org.maxgamer.maxclans.util;
import java.util.regex.Pattern;

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
}