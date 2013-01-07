package org.maxgamer.maxclans.clan;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.maxgamer.maxclans.MaxClans;
import org.maxgamer.maxclans.database.Database;

public class ClanMember{
	private String name;
	private Clan clan;
	private int rank = 0;
	
	private Player player;
	
	public ClanMember(String name){
		this.name = name;
	}
	
	public Player getPlayer(){
		return player;
	}
	public void setPlayer(Player p){
		player = p;
	}
	/** The name of this clan member.  Not lowercase. */
	public String getName(){
		return name;
	}
	/** This players clan */
	public Clan getClan(){
		return clan;
	}
	/** Same as getClan != null */
	public boolean hasClan(){
		return clan != null;
	}
	/** Returns the rank this clan member thinks it is. Fast */
	public int getRank(){
		return rank;
	}
	public boolean hasRank(int rank){
		return this.rank >= rank;
	}
	/** Do not use this.  Sets this players rank without notifying the clan */
	public void setRank(int rank){
		this.rank = rank;
	}
	/** Do not use this.  Sets this players clan without notifying the clan */
	public void setClan(Clan c){
		clan = c;
	}
	
	/** Updates this member in the database */
	public void update(){
		Database db = ClanManager.getDatabase();
		String clan = (this.clan == null ? "" : this.clan.getName());
		
		//db.execute("UPDATE members SET clan = '" + clan + "', rank = '" + rank + "' WHERE name = '" + name + "'");
		db.execute("UPDATE members SET clan = ?, rank = ? WHERE name = ?", clan, rank, name);
	}
	@Override
	public String toString(){
		return this.getName();
	}
	/** Tries to send the given message to the player. Fails silenty if offline. */
	public boolean send(String m){
		Player p = Bukkit.getPlayerExact(this.getName());
		if(p == null) return false;
		p.sendMessage(m);
		return true;
	}
	/** Tries to transfer the given amount from this players account to the players clan account.  Returns true if successful. Does not require an update to the database.*/
	public boolean clanDeposit(double amount){
		if(amount < 0) throw new IllegalArgumentException("Deposit amount must be > 0");
		
		Economy econ = MaxClans.instance.getEcon();
		if(econ == null || clan == null) return false;
		
		if(econ.getBalance(getName()) < amount) return false;
		
		EconomyResponse r1 = econ.withdrawPlayer(this.getName(), amount);
		if(r1.transactionSuccess() == false) return false;
		EconomyResponse r2 = econ.depositPlayer("clan-" + this.getClan().getName(), amount);
		if(r2.transactionSuccess() == false){
			//Couldn't deposit $ into clan.
			econ.depositPlayer(this.getName(), amount);
			return false; 
		}
		return true;
	}
	/** Tries to transfer the given amount from this players clan account to the players account. Returns true if successful. Does not require an update to the database. */
	public boolean clanWithdraw(double amount){
		if(amount < 0) throw new IllegalArgumentException("Withdrawel amount must be > 0");
		
		Economy econ = MaxClans.instance.getEcon();
		if(econ == null || clan == null) return false;
		
		if(clan.getBalance() < amount) return false;
		
		EconomyResponse r1 = econ.withdrawPlayer("clan-" + this.getClan().getName(), amount);
		if(r1.transactionSuccess() == false) return false;
		EconomyResponse r2 = econ.depositPlayer(this.getName(), amount);
		if(r2.transactionSuccess() == false){
			//Couldnt deposit $ into player
			econ.depositPlayer("clan-" + this.getClan().getName(), amount);
			return false;
		}
		return true;
	}
}