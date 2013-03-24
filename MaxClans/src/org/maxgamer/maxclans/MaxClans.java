package org.maxgamer.maxclans;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.maxgamer.maxclans.clan.Clan;
import org.maxgamer.maxclans.clan.ClanChest;
import org.maxgamer.maxclans.clan.ClanManager;
import org.maxgamer.maxclans.command.ClanAdminCommand;
import org.maxgamer.maxclans.command.ClanCommand;
import org.maxgamer.maxclans.listener.HerochatListener;
import org.maxgamer.maxclans.listener.MaxPvPListener;
import org.maxgamer.maxclans.listener.McMMOListener;
import org.maxgamer.maxclans.listener.PlayerListener;
import org.maxgamer.maxclans.listener.PlayerTracker;
import org.maxgamer.maxclans.listener.WorldListener;

public class MaxClans extends JavaPlugin{
	public static MaxClans instance;
	public static boolean debug = true;
	
	private HashSet<World> blacklisted = new HashSet<World>();
	
	private Economy economy;

	//Primary
	private PlayerTracker playerTracker;
	private PlayerListener playerListener;
	private WorldListener worldListener;
	//Plugins
	private McMMOListener mcMMOListener;
	private MaxPvPListener maxPVPListener;
	private HerochatListener herochatListener;
	//Commands
	private ClanCommand clanCommand;
	private ClanAdminCommand caCommand;
	
	public double clanCost = 0;
	public int chestRows = 2;
	
	private static PrintStream ps;
	
	public void onEnable(){
		instance = this;
		if(debug){
			File out = new File(this.getDataFolder(), "errors.txt");
			try {
				out.createNewFile();
				ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(out, true)));
			} catch (IOException e) {
				System.out.println("Couldnt create debug file... Turning off");
				debug = false;
			}
		}
		try{
			this.saveDefaultConfig();
			this.reloadConfig();
			this.getConfig().options().copyDefaults();
			
			if(Converter.convert_02_03()){
				System.out.println("Conversion Complete.");
			}
			
			System.out.println("Loading clans...");
			ClanManager.load();
			System.out.println("Done...");
			
			setupEconomy();
			
			clanCommand = new ClanCommand();
			caCommand = new ClanAdminCommand();
			this.getCommand("clan").setExecutor(clanCommand);
			this.getCommand("clanadmin").setExecutor(caCommand);
			Bukkit.getPluginManager().registerEvents(clanCommand, this);
			
			playerTracker = new PlayerTracker();
			playerListener = new PlayerListener();	
			worldListener = new WorldListener();
			
			
			playerTracker.register();
			playerListener.register();
			worldListener.register();
			
			if(Bukkit.getPluginManager().getPlugin("mcMMO") != null){
				mcMMOListener = new McMMOListener();
				mcMMOListener.register();
				System.out.println("Hooked mcMMO!");
			}
			else{
				System.out.println("Failed to hook mcMMO!");
			}
			
			if(Bukkit.getPluginManager().getPlugin("MaxPvP") != null){
				maxPVPListener = new MaxPvPListener();
				maxPVPListener.register();
				System.out.println("Hooked MaxPvP");
			}
			else{
				System.out.println("Failed to hook MaxPvP");
			}
			
			Plugin herochat = Bukkit.getPluginManager().getPlugin("Herochat");
			if(herochat != null){
				herochatListener = new HerochatListener();
				herochatListener.register();
				
				System.out.println("Hooked Herochat!");
			}
			else{
				System.out.println("Failed to hook Herochat");
			}
		}
		catch(Exception e){
			error(e);
		}
		catch(Error e){
			error(e);
		}
	}
	public void onDisable(){
		//Close all chests, then update them in the database.
		for(Clan clan : ClanManager.getClans().values()){
			ClanChest cc = clan.getClanChest();
			cc.close();
			cc.update();
		}
		ClanManager.unload();
		ClanManager.getDatabase().close();
		
		if(ps != null){
			ps.close();
		}
	}
	
	@Override
	/** Reads the config from disk, stores it in memory, then uses MaxClans.parseConfig() */
	public void reloadConfig(){
		super.reloadConfig();
		parseConfig();
	}
	/** Reads the config from memory, and not harddrive, and interprets it */
	public void parseConfig(){
		blacklisted.clear();
		List<String> blacklisted = getConfig().getStringList("blacklisted");
		if(blacklisted != null){
			for(String w : blacklisted){
				World world = Bukkit.getWorld(w);
				this.blacklisted.add(world);
			}
		}
		clanCost = getConfig().getDouble("clan.cost");
		chestRows = getConfig().getInt("clan.chest-rows");
	}
	
	public boolean isBlacklisted(World w){
		return blacklisted.contains(w);
	}
	
	public static void error(String s){
		ps.println(s);
		System.out.println(s);
	}
	public static void error(Exception e){
		e.printStackTrace(ps);
		e.printStackTrace(System.out);
	}
	public static void error(Error e){
		e.printStackTrace(ps);
		e.printStackTrace(System.out);
	}
	
	public boolean setupEconomy(){
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
	}
	public Economy getEcon(){
		return economy;
	}
}