package org.maxgamer.maxclans.clan;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.maxgamer.maxclans.MaxClans;
import org.maxgamer.maxclans.database.Database;

public class Clan{
	private HashSet<Clan> enemies = new HashSet<Clan>(5);
	private HashSet<Clan> allies = new HashSet<Clan>(5);
	
	private HashSet<ClanMember> masters = new HashSet<ClanMember>();
	private HashSet<ClanMember> leaders = new HashSet<ClanMember>();
	private HashSet<ClanMember> members = new HashSet<ClanMember>();
	private HashSet<ClanMember> guests = new HashSet<ClanMember>();
	
	private HashMap<String, ClanMember> online = new HashMap<String, ClanMember>();
	
	private String name;
	private String motd = "None";
	
	private boolean friendlyFire;
	private boolean isOpen = false;
	
	private String spawnWorld = "";
	private double spawnX = 0;
	private double spawnY = 0;
	private double spawnZ = 0;
	private float spawnYaw = 0;
	private float spawnPitch = 0;
	
	private int kills = 0;
	private int deaths = 0;
	private int levels = 0;
	
	public static final int SPAWN_PROTECTION_RADUS_SQUARED = 900;
	
	private ClanChest clanChest;
	
	private Location spawn;
	private boolean respawn;
	
	public Clan(String name){
		this.name = name;
	}
	
	public ClanChest getClanChest(){
		return clanChest;
	}
	public void setClanChest(ClanChest c){
		clanChest = c;
	}
	
	public int getRating(){
		double rating = 1250;
		//KDR.
		double kdr = getKDR()*3 - 1;
		rating += kdr * 7.5;
		rating += this.levels;
		rating += this.getGuests().size() * 5;
		rating += this.getBalance() * 0.2;
		
		return (int) rating;
	}
	
	public double getKDR(){
		double kills = (this.kills == 0 ? 1 : this.kills);
		double deaths = (this.deaths == 0 ? 1 : this.deaths);
		return kills/deaths;
	}
	
	/** Marginally faster than size >= n for big clans. */
	public boolean size(int n){
		int i = 0;
		for(@SuppressWarnings("unused") ClanMember cm : guests){
			if(++i >= n) return true;
		}
		return false;
	}
	/** A hashmap of all players in this clan who are online.  Key = name, Value = player.  Modifying this hashmap is bad. */
	public HashMap<String, ClanMember> getOnline(){
		return online;
	}
	/** A hashmap of all players in this clan who are online and have the given rank. Key = name, Value = player. Modifying this hashmap is OK.. DO NOT STORE THE Player values in memory permanently! Causes memory leaks. */
	public HashMap<String, ClanMember> getOnline(int rank){
		HashMap<String, ClanMember> online = new HashMap<String, ClanMember>();
		
		for(Entry<String, ClanMember> entry : this.online.entrySet()){
			if(entry.getValue().hasRank(rank)) online.put(entry.getKey(), entry.getValue());
		}
		return online;
	}
	
	/** Sets the message of the day for this clan. Use clan.update() to save to database */
	public void setMotd(String motd){
		if(motd == null) motd = "";
		this.motd = motd;
	}
	/** This clans message of the day */
	public String getMotd(){
		return motd;
	}
	
	public void setKills(int n){
		kills = n;
	}
	public void setDeaths(int n){
		deaths = n;
	}
	public void setLevels(int n){
		levels = n;
	}
	public int getKills(){
		return kills;
	}
	public int getDeaths(){
		return deaths;
	}
	public int getLevels(){
		return levels;
	}
	public void addKill(){
		kills++;
	}
	public void addDeath(){
		deaths++;
	}
	public void addLevel(){
		levels++;
	}
	/** Whether or not players should respawn at the clans spawn, given they are not too close to it. */
	public boolean respawn(){
		return respawn;
	}
	/** Setting this to true will let player respawn at the clans spawn if they don't die too close to it. */
	public void setRespawn(boolean respawn){
		this.respawn = respawn;
	}
	public boolean isOpen(){
		return isOpen;
	}
	public void setOpen(boolean open){
		isOpen = open;
	}
	public void setSpawn(String w, double x, double y, double z, float yaw, float pitch){
		World world = Bukkit.getWorld(w);
		if(world == null) spawn = null;
		else spawn = new Location(world, x, y, z, yaw, pitch);
		
		spawnWorld = w;
		spawnX = x;
		spawnY = y;
		spawnZ = z;
		spawnYaw = yaw;
		spawnPitch = pitch;
	}
	
	public Location getSpawn(){
		if(spawn == null){ //Spawn not set.
			World world = Bukkit.getWorld(spawnWorld); 
			if(world != null){ //The world is now loaded, we can use it!
				spawn = new Location (world, spawnX, spawnY, spawnZ, spawnPitch, spawnYaw);
			}
		}
		return spawn;
	}
	
	/** Returns a list of all the masters of this clan. */
	public HashSet<ClanMember> getMasters(){
		return masters;
	}
	
	/** Returns a list of all the leaders and masters of this clan. */
	public HashSet<ClanMember> getLeaders(){
		return leaders;
	}
	
	/** Returns a list of all members, leaders and masters of this clan */
	public HashSet<ClanMember> getMembers(){
		return members;
	}
	
	/** Returns a list of all guests, members, leaders and masters of this clan */
	public HashSet<ClanMember> getGuests(){
		return guests;
	}
	
	public int getRank(ClanMember cm){
		if(this.masters.contains(cm)) return 3;
		if(this.leaders.contains(cm)) return 2;
		if(this.guests.contains(cm)) return 1;
		return 0;
	}
	
	/** Returns true if clan members can hurt each other */
	public boolean hasFriendlyFire(){
		return friendlyFire;
	}
	
	/** Changes this clans friendly fire setting */
	public void setFriendlyFire(boolean ff){
		this.friendlyFire = ff;
	}
	
	/** Returns true if the given clan is allied */
	public boolean isAlly(Clan c){
		return allies.contains(c);
	}
	/** Returns true if the given clan is enemied */
	public boolean isEnemy(Clan c){
		return enemies.contains(c);
	}
	/** Returns true if the given clan is neither allied nor enemy */
	public boolean isNeutral(Clan c){
		return (!isEnemy(c) && !isAlly(c));
	}
	/** Adds the given ally to this clans list of allies.  Also updates the database */
	public void addAlly(Clan c){
		allies.add(c);
		Database db = ClanManager.getDatabase();
		String ally = c.name;
		
		//db.execute("INSERT INTO allies (name, ally) VALUES ('" + name + "', '" + ally + "')");
		db.execute("INSERT INTO allies (name, ally) VALUES (?, ?)", name, ally);
	}
	/** Deletes the given ally from this clans list of allies.  Also updates the database */
	public void removeAlly(Clan c){
		allies.remove(c);
		Database db = ClanManager.getDatabase();
		
		String ally = c.name;
		
		//db.execute("DELETE FROM allies WHERE name = '" + name + "' AND ally = '" + ally + "'");
		db.execute("DELETE FROM allies WHERE name = ? AND ally = ?", name, ally);
	}
	/** Adds the given enemy to this clans list of enemies.  Also updates the database */
	public void addEnemy(Clan c){
		enemies.add(c);
		Database db = ClanManager.getDatabase();
		
		String enemy = c.name;
		
		//db.execute("INSERT INTO enemies (name, enemy) VALUES ('" + name + "', '" + enemy + "')");
		db.execute("INSERT INTO enemies (name, enemy) VALUES (?, ?)", name, enemy);
	}
	/** Deletes the given enemy from this clans list of enemies.  Also updates the database */
	public void removeEnemy(Clan c){
		enemies.remove(c);
		Database db = ClanManager.getDatabase();
		
		String enemy = c.name;
		
		//db.execute("DELETE FROM enemies WHERE name = '" + name + "' AND enemy = '" + enemy + "'");
		db.execute("DELETE FROM enemies WHERE name = ? AND enemy = ?", name, enemy);
	}
	
	/** Returns the name of this clan */
	public String getName(){
		return name;
	}
	/** Changes the name of this clan... Also updates it in the database. */
	public void setName(String n){
		Database db = ClanManager.getDatabase();
		db.execute("UPDATE allies SET name = ? WHERE name = ?", n, name);
		db.execute("UPDATE allies SET ally = ? WHERE ally = ?", n, name);
		
		db.execute("UPDATE enemies SET name = ? WHERE name = ?", n, name);
		db.execute("UPDATE enemies SET enemy = ? WHERE enemy = ?", n, name);
		
		db.execute("UPDATE members SET clan = ? WHERE clan = ?", n, name);
		db.execute("UPDATE clans SET name = ? WHERE name = ?", n, name);
		
		ClanManager.getTrieSet().remove(name.toLowerCase());
		ClanManager.getTrieSet().add(n.toLowerCase());
		
		ClanManager.getClans().remove(name.toLowerCase());
		ClanManager.getClans().put(n.toLowerCase(), this);
		
		name = n;
	}
	
	/** Returns a hashset of allied clans */
	public HashSet<Clan> getAllies(){
		return this.allies;
	}
	/** Returns a hashset of enemy clans */
	public HashSet<Clan> getEnemies(){
		return this.enemies;
	}
	/** Adds the given clan member to this clan. */
	public void add(ClanMember cm){
		if(cm.getClan() == this){
			//DEBUG
			System.out.println(cm.getName() + " is already a member of " + this.getName());
			return;
		}
		
		if(cm.hasClan()){
			//Kick them from their old clan.
			cm.getClan().kick(cm);
			System.out.println(cm.getName() + " kicked out of their old clan to join " + this.getName());
		}
		
		cm.setClan(this);
		cm.setRank(0);
		
		this.guests.add(cm);
		
		Player p = cm.getPlayer();
		if(p != null) online.put(p.getName(), cm);
	}
	/** Kicks the given clan member from this clan. */
	public void kick(ClanMember cm){
		if(cm.getClan() != this){
			//DEBUG
			System.out.println(cm.getName() + " is not a member of " + this.getName() + " and cannot be kicked!");
			return;
		}
		
		while(cm.getRank() > 0){
			this.demote(cm);
		}
		
		this.guests.remove(cm);
		cm.setClan(null);
		
		Player p = cm.getPlayer();
		if(p != null) online.remove(p.getName());
	}
	/** Demotes the given clan member one rank.  Returns true if successful. */
	public boolean demote(ClanMember cm){
		int rank = cm.getRank();
		if(rank == 0) return false;
		if(rank == 1){ //Member
			this.members.remove(cm);
			cm.setRank(rank - 1);
			return true;
		}
		if(rank == 2){ //Leader
			this.leaders.remove(cm);
			cm.setRank(rank - 1);
			return true;
		}
		if(rank == 3){ //Master
			this.masters.remove(cm);
			cm.setRank(rank - 1);
			return true;
		}
		//DEBUG
		System.out.println("Demotion error: Invalid rank for " + cm.getName() + " in clan " + this.getName() + ". rank: " + rank);
		return false;
	}
	/** Promotes the given clan member one rank.  Returns true if successful. */
	public boolean promote(ClanMember cm){
		int rank = cm.getRank();
		if(rank == 0){
			cm.setRank(rank + 1);
			this.members.add(cm);
			return true;
		}
		
		if(rank == 1){
			cm.setRank(rank + 1);
			this.leaders.add(cm);
			return true;
		}
		
		if(rank == 2){
			cm.setRank(rank + 1);
			this.masters.add(cm);
			return true;
		}
		
		if(rank == 3){
			return false; //Already top rank
		}
		//DEBUG
		System.out.println("Promotion error: Invalid clan for " + cm.getName() + " in clan " + this.getName() + ". rank: " + rank);
		return false;
	}
	
	/** Updates this clan in the database.  Does not modify allies or enemies in the database. Does not modify clan members. */
	public void update(){
		Database db = ClanManager.getDatabase();
		String world = this.spawnWorld;
		
		//db.execute("UPDATE clans SET ff = '" + (this.friendlyFire ? 1 : 0) + "', spawnWorld = '" + world + "', spawnX = '" + spawnX + "', spawnY = '" + spawnY + "', spawnZ = '" + spawnZ + "', spawnYaw = '" + spawnYaw + "', spawnPitch = '" + spawnPitch + "', isOpen = '" + (isOpen ? 1 : 0) + "', motd = '" + db.escape(motd) + "', levels = '" + levels + "', kills = '" + kills + "', deaths = '" + deaths + "', respawn = '" + (respawn ? 1 : 0) + "' WHERE name = '" + name + "'");
		db.execute("UPDATE clans SET ff = ?, spawnWorld = ?, spawnX = ?, spawnY = ?, spawnZ = ?, spawnYaw = ?, spawnPitch = ?, isOpen = ?, motd = ?, levels = ?, kills = ?, deaths = ?, respawn = ? WHERE name = ?", (this.friendlyFire ? 1 : 0), world, spawnX, spawnY, spawnZ, spawnYaw, spawnPitch, (isOpen ? 1 : 0), motd, levels, kills, deaths, (respawn ? 1 : 0), name);
	}
	
	/** Deletes this clan from the database.  Also safely removes any guests+ who belong to this clan.  Clan.update() won't work and is not necessary. ClanMember.update() is not necessary either.*/
	@SuppressWarnings("unchecked")
	public void delete(){
		Database db = ClanManager.getDatabase();
		
		db.execute("DELETE FROM clans WHERE name = ?", name);
		HashSet<ClanMember> guests = (HashSet<ClanMember>) this.getGuests().clone();
		for(ClanMember cm : guests){
			this.kick(cm);
			cm.update();
		}
		
		HashSet<Clan> allies = (HashSet<Clan>) this.allies.clone();
		for(Clan ally : allies){
			this.removeAlly(ally);
		}
		
		HashSet<Clan> enemies = (HashSet<Clan>) this.enemies.clone();
		for(Clan enemy : enemies){
			this.removeEnemy(enemy);
		}
		
		for(Clan clan : ClanManager.getClans().values()){
			if(clan.isAlly(this)){
				clan.removeAlly(this);
			}
			if(clan.isEnemy(this)){
				clan.removeEnemy(this);
			}
		}
		//Final goodbye.
		ClanManager.forget(this);
	}
	
	/** Fetches the nice formatted name of the given rank */
	public String getRankName(int rank){
		if(rank == 3) return "Clan Master";
		if(rank == 2) return "Clan Leader";
		if(rank == 1) return "Clan Member";
		if(rank == 0) return "Clan Guest";
		return ""+rank;
	}
	
	@Override
	public String toString(){
		return this.getName();
	}
	
	public String getFormattedEnemies(){
		return getFormattedEnemies(ChatColor.RED, ChatColor.GREEN);
	}
	/** Returns the enemies of this clan as a list. clanColor is the ChatColor clans appear in. Punctuation is the color ,'s appear in. */
	public String getFormattedEnemies(ChatColor clanColor, ChatColor punctuationColor){
		StringBuilder sb = new StringBuilder();
		if(getEnemies().isEmpty()){
			sb.append(clanColor + "None");
		}
		else{
			int i = 0;
			for(Clan c : this.getEnemies()){
				if(++i > 10){
					sb.append(clanColor + " and " + (getEnemies().size() - i) + " more  ");
					break;
				}
				sb.append(clanColor + c.getName() + punctuationColor + ", ");
			}
			sb.replace(sb.length() - 2, sb.length(), "");
		}
		return sb.toString();
	}
	public String getFormattedAllies(){
		return getFormattedAllies(ChatColor.GREEN, ChatColor.RED);
	}
	/** Returns the allies of this clan as a list. clanColor is the ChatColor clans appear in. Punctuation is the color ,'s appear in. */
	public String getFormattedAllies(ChatColor clanColor, ChatColor punctuationColor){
		StringBuilder sb = new StringBuilder();
		if(getAllies().isEmpty()){
			sb.append(clanColor + "None");
		}
		else{
			int i = 0;
			for(Clan c : this.getAllies()){
				if(++i > 10){
					sb.append(clanColor + " and " + (getAllies().size() - i) + " more  ");
					break;
				}
				sb.append(clanColor + c.getName() + punctuationColor + ", ");
			}
			sb.replace(sb.length() - 2, sb.length(), "");
		}
		return sb.toString();
	}
	
	/** Returns the guests of this clan as a list. clanColor is the ChatColor clans appear in. Punctuation is the color ,'s appear in. */
	public String getFormattedGuests(ChatColor clanColor, ChatColor punctuationColor, ChatColor onlineColor){
		StringBuilder sb = new StringBuilder();
		if(getGuests().isEmpty()){
			sb.append(clanColor + "None");
		}
		else{
			int i = 0;
			for(ClanMember cm : this.getGuests()){
				if(++i > 30){
					sb.append(clanColor + " and " + (guests.size() - i) + " more  ");
					break;
				}
				sb.append((cm.getPlayer() == null ? clanColor : onlineColor) + cm.getName() + punctuationColor + ", ");
			}
			sb.replace(sb.length() - 2, sb.length(), "");
		}
		return sb.toString();
	}
	/** Returns the guests of this clan as a list. clanColor is the ChatColor clans appear in. Punctuation is the color ,'s appear in. */
	public String getFormattedGuests(){
		return getFormattedGuests(ChatColor.AQUA, ChatColor.RED, ChatColor.GREEN);
	}
	/** Sends everyone in this clan who is online a message */
	public void sendMessage(String m){
		sendMessage(m, 0);
	}
	/** Sends everyone in this clan who is online and has the given rank (or higher) a message */
	public void sendMessage(String m, int rank){
		sendMessage(m, rank, null);
	}
	/** Sends everyone in this clan excluding the given member who is online and has the given rank (or higher) a message */
	public void sendMessage(String m, int rank, ClanMember exclude){
		for(ClanMember cm : online.values()){
			if(cm.getRank() < rank || cm == exclude) continue;
			cm.send(m);
		}
	}
	
	public double getBalance(){
		if(MaxClans.instance.getEcon() == null) return 0;
		else return MaxClans.instance.getEcon().getBalance("clan-"+this.getName());
	}
}