package org.maxgamer.maxclans.listener;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.maxgamer.maxclans.MaxClans;
import org.maxgamer.maxclans.clan.Clan;
import org.maxgamer.maxclans.clan.ClanManager;
import org.maxgamer.maxclans.clan.ClanMember;

public class PlayerListener extends MaxClansListener{
	@EventHandler(priority = EventPriority.LOW)
	public void onDamage(EntityDamageByEntityEvent e){
		if(e.getDamage() <= 0 || e.getDamager() == null) return;
		if(MaxClans.instance.isBlacklisted(e.getEntity().getWorld())) return; //Friendly fire forced off.
		if(!(e.getEntity() instanceof Player)) return;
		Player victim = (Player) e.getEntity();
		Player attacker;
		if(e.getDamager() instanceof Player){
			attacker = (Player) e.getDamager();
		}
		else if(e.getDamager() instanceof Projectile){
			Projectile proj = (Projectile) e.getDamager();
			if(proj.getShooter() instanceof Player){
				attacker = (Player) proj.getShooter();
			}
			else{
				return;
			}
		}
		else{
			return;
		}
		if(victim == attacker) return; //Retard shot himself or something
		
		ClanMember attackUser = ClanManager.getClanMember(attacker.getName());
		ClanMember victimUser = ClanManager.getClanMember(victim.getName());
		
		if(attackUser == null || victimUser == null) return; //Neither are clan members yet
		if(attackUser.getClan() == null || victimUser.getClan() == null) return; //Neither have clans currently.
		Clan attackClan = attackUser.getClan();
		Clan victimClan = victimUser.getClan();
		
		if(attackClan == victimClan){
			if(!attackClan.hasFriendlyFire()){
				e.setCancelled(true);
				attacker.sendMessage(ChatColor.RED + "Your clan has friendly fire disabled. You may not attack clan members.");
				return;
			}
			else{
				//Same clan, but they have FF on, so they can damage each other.
				return;				
			}
		}
		else if(attackClan.getAllies().contains(victimClan)){
			//They're allied.
			if(!attackClan.hasFriendlyFire() && !victimClan.hasFriendlyFire()){
				//Both have FF disabled.
				e.setCancelled(true);
				attacker.sendMessage(ChatColor.RED + "Both your clans have friendly fire disabled. You may not attack ally clans.");
				return;
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onDeath(PlayerRespawnEvent e){
		ClanMember cm = ClanManager.getClanMember(e.getPlayer());
		if(cm == null || cm.getClan() == null) return;
		Clan c = cm.getClan();
		//Player.getLocation() gets death location.
		if(c.getSpawn() == null || (c.getSpawn().getWorld() == e.getPlayer().getLocation() && e.getPlayer().getLocation().distanceSquared(c.getSpawn()) < Clan.SPAWN_PROTECTION_RADUS_SQUARED)) return;
		e.setRespawnLocation(c.getSpawn());
	}
}