package org.maxgamer.maxclans.clan;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.maxgamer.maxclans.MaxClans;
import org.maxgamer.maxclans.database.Database;
import org.maxgamer.maxclans.database.Database.ConnectionException;
import org.maxgamer.maxclans.database.DatabaseCore;
import org.maxgamer.maxclans.database.DatabaseHelper;
import org.maxgamer.maxclans.database.SQLiteCore;
import org.maxgamer.maxclans.util.TrieSet;
import org.maxgamer.maxclans.util.Util;

public class ClanManager{
	private static HashMap<String, ClanMember> players = new HashMap<String, ClanMember>();
	private static HashMap<String, Clan> clans = new HashMap<String, Clan>();
	private static TrieSet clan_names = new TrieSet();
	private static TrieSet player_names = new TrieSet();
	
	private static Database database;
	
	static{
		DatabaseCore dbCore = new SQLiteCore(new File(MaxClans.instance.getDataFolder(), "clans.db"));
		
		try{
			database = new Database(dbCore);
		}
		catch(ConnectionException e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Database Layout:
	 * 
	 * Members Table:
	 * Name 		|	ClanName	 | Rank	|
	 * Netherfoam	|	HighFive	 |	2	|
	 * Saintsrock	|	BGNoobs		 |	2	|
	 * 
	 * Clans Table:
	 * Name		|	FF	| SpawnWorld	|	SpawnX	|	SpawnY	|	SpawnZ	|
	 * HighFive	|	1	| challenge_8	|	200		|	64		|	-2000	|
	 * BGNoobs	|	0	| arena			|	999		|	64		|	510		|
	 * 
	 * Enemies Table:
	 * Name 	| Enemy
	 * HighFive | BGNoobs
	 * 
	 * Allies Table:
	 * Name 	| Ally
	 * BGNoobs | HighFive
	 * 
	 */
	public static void load(){
		try {
			DatabaseHelper.setup(database);
			
			//Load clans from the database
			PreparedStatement ps = database.getConnection().prepareStatement("SELECT * FROM clans");
			ResultSet rs = ps.executeQuery();
			
			while(rs.next()){
				try{
				String name = rs.getString("name");
				boolean ff = rs.getBoolean("ff");
				String spawnWorld = rs.getString("spawnWorld");
				double spawnX = rs.getDouble("spawnX");
				double spawnY = rs.getDouble("spawnY");
				double spawnZ = rs.getDouble("spawnZ");
				float spawnYaw = rs.getFloat("spawnYaw");
				float spawnPitch = rs.getFloat("spawnPitch");
				
				int kills = rs.getInt("kills");
				int deaths = rs.getInt("deaths");
				int levels = rs.getInt("levels");
				
				boolean open = rs.getBoolean("isOpen");
				String motd = rs.getString("motd");
				byte[] inv = rs.getBytes("inv");
				
				Clan clan = new Clan(name);
				clan.setFriendlyFire(ff);
				clan.setSpawn(spawnWorld, spawnX, spawnY, spawnZ, spawnYaw, spawnPitch);
				clan.setDeaths(deaths);
				clan.setKills(kills);
				clan.setOpen(open);
				clan.setMotd(motd);
				clan.setLevels(levels);
				
				ClanChest cc = new ClanChest(clan, inv, MaxClans.instance.getConfig().getInt("clan.chest-rows") * 9);
				clan.setClanChest(cc);
				
				name = name.toLowerCase();
				clans.put(name, clan);
				clan_names.add(name);
				}
				catch(Exception e){
					if(MaxClans.debug){
						MaxClans.error(e);
					}
				}
				catch(Error e){
					if(MaxClans.debug){
						MaxClans.error(e);
					}
				}
			}
			
			//Load enemies
			ps = database.getConnection().prepareStatement("SELECT * FROM enemies");
			rs = ps.executeQuery();
			
			while(rs.next()){
				String c1 = rs.getString("name");
				String c2 = rs.getString("enemy");
				
				Clan clan = getClan(c1);
				Clan enemy = getClan(c2);
				
				if(clan == null){
					//DEBUG
					MaxClans.instance.getLogger().info("Invalid clan in enemies table: " + c1);
					continue;
				}
				if(enemy == null){
					//DEBUG
					MaxClans.instance.getLogger().info("Invalid clan in enemies table: " + c2);
					continue;
				}
				
				clan.getEnemies().add(enemy);
			}
			
			//Load allies
			ps = database.getConnection().prepareStatement("SELECT * FROM allies");
			rs = ps.executeQuery();
			
			while(rs.next()){
				String c1 = rs.getString("name");
				String c2 = rs.getString("ally");
				
				Clan clan = getClan(c1);
				Clan ally = getClan(c2);
				
				if(clan == null){
					//DEBUG
					MaxClans.instance.getLogger().info("Invalid clan in allies table: " + c1);
					continue;
				}
				if(ally == null){
					//DEBUG
					MaxClans.instance.getLogger().info("Invalid clan in allies table: " + c2);
					continue;
				}
				
				clan.getAllies().add(ally);
			}
			
			//Load members
			ps = database.getConnection().prepareStatement("SELECT * FROM members");
			rs = ps.executeQuery();
			
			while(rs.next()){
				String name = rs.getString("name");
				int rank = rs.getInt("rank");
				String clanName = rs.getString("clan");
				
				ClanMember member = new ClanMember(name);
				
				if(!clanName.isEmpty()){ //If they have no clan, this will be blank.
					Clan clan = getClan(clanName);
					
					if(clan == null){
						//DEBUG
						MaxClans.instance.getLogger().info("Invalid clan in members table: " + clanName);
					}
					else{
						member.setClan(clan);
						member.setRank(rank);
						
						if(rank >= 3){
							clan.getMasters().add(member);
						}
						if(rank >= 2){
							clan.getLeaders().add(member);
						}
						if(rank >= 1){
							clan.getMembers().add(member);
						}
						clan.getGuests().add(member);
					}
				}
				
				players.put(name.toLowerCase(),member);
				player_names.add(name.toLowerCase());
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			MaxClans.instance.getLogger().severe("Could not load clans from database!");
		}
	}
	public static Database getDatabase(){
		return database;
	}
	public static HashMap<String, Clan> getClans(){
		return clans;
	}
	
	/** The same as getClanMember(s, false); */
	public static ClanMember getClanMember(String s){
		return getClanMember(s, false);
	}
	/** Fetches a clan member. Use true for autocomplete, false otherwise. If an online player is used and is not a clan member yet, they will be auto added. */
	public static ClanMember getClanMember(String s, boolean auto){
		if(auto){
			ClanMember cm = players.get(s.toLowerCase());
			if(cm != null) return cm;
			
			Player p = Bukkit.getPlayer(s);
			if(p != null){
				cm = players.get(p.getName().toLowerCase());
				if(cm != null) return cm;
				else return createClanMember(p);
			}
			
			s = player_names.nearestKey(s.toLowerCase());
			return players.get(s);
		}
		return players.get(s.toLowerCase());
	}
	/** Fetches the given clan member by player object.  Does not autocomplete */
	public static ClanMember getClanMember(Player p){
		return getClanMember(p.getName(), false);
	}
	/** Creates a new clan member for the given player. Does not check for duplicates. Duplicates will cause errors. */
	public static ClanMember createClanMember(Player p){
		ClanMember cm = new ClanMember(p.getName());
		cm.setPlayer(p);
		players.put(p.getName().toLowerCase(), cm);
		player_names.add(p.getName().toLowerCase());
		
		String name = p.getName();
		database.execute("INSERT INTO members (name, clan, rank) VALUES (?, ?, ?)", name, "", cm.getRank());
		
		return cm;
	}
	/** Convenience method to fetch the clan a player is in. */
	public static Clan getClan(Player p){
		ClanMember cm = getClanMember(p);
		if(cm == null) return null;
		return cm.getClan();
	}
	/** Fetches a clan by name. Case insensitive. Does not autocomplete. */
	public static Clan getClan(String name){
		return getClan(name, false);
	}
	/** Fetches a clan by name. Case insensitive. If auto = true, it will autocomplete */
	public static Clan getClan(String name, boolean auto){
		name = name.toLowerCase();
		if(auto){
			name = clan_names.nearestKey(name);
			return clans.get(name);
		}
		return clans.get(name);
	}
	/** Creates a new clan with the given name */
	public static Clan createClan(String name){
		Clan clan = new Clan(name);
		ClanChest cc = new ClanChest(clan);
		clan.setClanChest(cc);
		
		clans.put(name.toLowerCase(), clan);
		clan_names.add(name.toLowerCase());
		
		//Insert clan into database.
		database.execute("INSERT INTO clans (name, ff, spawnWorld, spawnX, spawnY, spawnZ, motd, inv) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", name, (clan.hasFriendlyFire() ? 1 : 0), "", 0, 64, 0, "", cc.getNBTBytes());

		return clan;
	}
	/** Releases all data this ClanManager has stored. Does not stop the database. */
	public static void unload(){
		players.clear();
		clans.clear();
		clan_names.clear();
		player_names.clear();
	}
	
	public static TrieSet getTrieSet(){
		return clan_names;
	}
	/**
	 * Sorts and finds the top clans, then returns them sorted, from highest rating to lowest.
	 * @param amount The amount of results wanted.  If the array is not filled, the rest of the values will be null.
	 * @return The array of sorted clans
	 */
	public static Clan[] getHighestRatings(int amount){
		Clan[] top = new Clan[amount];
		for(Clan clan : ClanManager.getClans().values()){
			for(int i = top.length - 1; i >= -1; i--){
				if(i == -1 || (top[i] != null && top[i].getRating() > clan.getRating())){
					if(i == top.length - 1){
						//Poorer than the poorest rich guy
						break;
					}
					
					Util.shuffle(top, i + 1);
					top[i + 1] = clan;
					break;
				}
			}
		}
		return top;
	}
	/**
	 * Sorts and finds the top clans, then returns them sorted, from highest rating to lowest.
	 * @param amount The amount of results wanted.  If the array is not filled, the rest of the values will be null.
	 * @return The array of sorted clans
	 */
	public static Clan[] getHighestKills(int amount){
		Clan[] top = new Clan[amount];
		for(Clan clan : ClanManager.getClans().values()){
			for(int i = top.length - 1; i >= -1; i--){
				if(i == -1 || (top[i] != null && top[i].getKills() > clan.getKills())){
					if(i == top.length - 1){
						//Poorer than the poorest rich guy
						break;
					}
					
					Util.shuffle(top, i + 1);
					top[i + 1] = clan;
					break;
				}
			}
		}
		return top;
	}
	/**
	 * Sorts and finds the top clans, then returns them sorted, from highest rating to lowest.
	 * @param amount The amount of results wanted.  If the array is not filled, the rest of the values will be null.
	 * @return The array of sorted clans
	 */
	public static Clan[] getHighestDeaths(int amount){
		Clan[] top = new Clan[amount];
		for(Clan clan : ClanManager.getClans().values()){
			for(int i = top.length - 1; i >= -1; i--){
				if(i == -1 || (top[i] != null && top[i].getDeaths() > clan.getDeaths())){
					if(i == top.length - 1){
						//Poorer than the poorest rich guy
						break;
					}
					
					Util.shuffle(top, i + 1);
					top[i + 1] = clan;
					break;
				}
			}
		}
		return top;
	}
	/**
	 * Sorts and finds the top clans, then returns them sorted, from highest rating to lowest.
	 * @param amount The amount of results wanted.  If the array is not filled, the rest of the values will be null.
	 * @return The array of sorted clans
	 */
	public static Clan[] getHighestCash(int amount){
		Clan[] top = new Clan[amount];
		for(Clan clan : ClanManager.getClans().values()){
			for(int i = top.length - 1; i >= -1; i--){
				if(i == -1 || (top[i] != null && top[i].getBalance() > clan.getBalance())){
					if(i == top.length - 1){
						//Poorer than the poorest rich guy
						break;
					}
					
					Util.shuffle(top, i + 1);
					top[i + 1] = clan;
					break;
				}
			}
		}
		return top;
	}
	/**
	 * Sorts and finds the top clans, then returns them sorted, from highest rating to lowest.
	 * @param amount The amount of results wanted.  If the array is not filled, the rest of the values will be null.
	 * @return The array of sorted clans
	 */
	public static Clan[] getHighestLevels(int amount){
		Clan[] top = new Clan[amount];
		for(Clan clan : ClanManager.getClans().values()){
			for(int i = top.length - 1; i >= -1; i--){
				if(i == -1 || (top[i] != null && top[i].getLevels() > clan.getLevels())){
					if(i == top.length - 1){
						//Poorer than the poorest rich guy
						break;
					}
					
					Util.shuffle(top, i + 1);
					top[i + 1] = clan;
					break;
				}
			}
		}
		return top;
	}
	/**
	 * Sorts and finds the top clans, then returns them sorted, from highest rating to lowest.
	 * @param amount The amount of results wanted.  If the array is not filled, the rest of the values will be null.
	 * @return The array of sorted clans
	 */
	public static Clan[] getHighestPlayers(int amount){
		Clan[] top = new Clan[amount];
		for(Clan clan : ClanManager.getClans().values()){
			for(int i = top.length - 1; i >= -1; i--){
				if(i == -1 || (top[i] != null && top[i].getGuests().size() > clan.getGuests().size())){
					if(i == top.length - 1){
						//Poorer than the poorest rich guy
						break;
					}
					
					Util.shuffle(top, i + 1);
					top[i + 1] = clan;
					break;
				}
			}
		}
		return top;
	}
	/**
	 * Removes the given clan from memory, but not the database.
	 * @param clan The clan to remove.
	 */
	public static void forget(Clan clan){
		clans.remove(clan.getName().toLowerCase());
		clan_names.remove(clan.getName().toLowerCase());
	}
}