package org.maxgamer.maxclans.listener;

import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.server.PluginEnableEvent;
import org.maxgamer.maxclans.clan.Clan;
import org.maxgamer.maxclans.clan.ClanManager;
import org.maxgamer.maxclans.clan.ClanMember;

import com.dthielke.herochat.Channel;
import com.dthielke.herochat.ChannelChatEvent;
import com.dthielke.herochat.ChannelStorage;
import com.dthielke.herochat.Herochat;
import com.dthielke.herochat.StandardChannel;
import com.dthielke.herochat.Chatter.Result;

public class HerochatListener extends MaxClansListener{
	private static Channel clanChannel;
	private static Channel allyChannel;
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChat(ChannelChatEvent e){
		if(e.getResult() != Result.ALLOWED) return;
		
		if(e.getChannel() == clanChannel){
			e.setResult(Result.FAIL);
			ClanMember cm = ClanManager.getClanMember(e.getSender().getPlayer());
			if(cm == null || cm.getClan() == null){
				e.getSender().getPlayer().sendMessage(ChatColor.RED + "You are not in a clan!");
				return;
			}
			String format = clanChannel.applyFormat(e.getFormat(), e.getBukkitFormat(), e.getSender().getPlayer());
			String msg = String.format(format, e.getSender().getName(), e.getMessage());
			
			cm.getClan().sendMessage(msg);
		}
		else if(e.getChannel() == allyChannel){
			e.setResult(Result.FAIL);
			ClanMember cm = ClanManager.getClanMember(e.getSender().getPlayer());
			if(cm == null || cm.getClan() == null){
				e.getSender().getPlayer().sendMessage(ChatColor.RED + "You are not in a clan!");
				return;
			}
			String format = allyChannel.applyFormat(e.getFormat(), e.getBukkitFormat(), e.getSender().getPlayer());
			String msg = String.format(format, e.getSender().getName(), e.getMessage());
			
			cm.getClan().sendMessage(msg);
			for(Clan clan : cm.getClan().getAllies()){
				clan.sendMessage(msg);
			}
		}
	}
	
	@EventHandler
	public void onPluginLoad(PluginEnableEvent e){
		if(e.getPlugin().getName().equals("Herochat")){
			clanChannel = new StandardChannel(new DummyChannelStorage(), "Clans", "C", Herochat.getChannelManager());
		    clanChannel.setFormat("{default}");
		    clanChannel.setPassword("");
		    clanChannel.setColor(ChatColor.GREEN);
		    clanChannel.setDistance(0);
		    clanChannel.setShortcutAllowed(true);
		    clanChannel.setVerbose(true);
		    clanChannel.setMuted(false);
		    clanChannel.setCrossWorld(true);
		    clanChannel.setColor(ChatColor.GREEN);
		    
		    Herochat.getChannelManager().addChannel(clanChannel);
		    
		    
			allyChannel = new StandardChannel(new DummyChannelStorage(), "Ally", "A", Herochat.getChannelManager());
			allyChannel.setFormat("{default}");
			allyChannel.setPassword("");
			allyChannel.setColor(ChatColor.GOLD);
			allyChannel.setDistance(0);
			allyChannel.setShortcutAllowed(true);
			allyChannel.setVerbose(true);
			allyChannel.setMuted(false);
			allyChannel.setCrossWorld(true);
			allyChannel.setColor(ChatColor.GOLD);
		    
		    Herochat.getChannelManager().addChannel(allyChannel);
		}
	}
	
	/** Dummy channel storage that doesn't actually store anything and all methods return null */
	private class DummyChannelStorage implements ChannelStorage{

		@Override
		public void addChannel(Channel arg0) {
		}

		@Override
		public void flagUpdate(Channel arg0) {
		}

		@Override
		public Channel load(String arg0) {
			return null;
		}

		@Override
		public Set<Channel> loadChannels() {
			return null;
		}

		@Override
		public void removeChannel(Channel arg0) {
		}

		@Override
		public void update() {
		}

		@Override
		public void update(Channel arg0) {
		}
	}
}