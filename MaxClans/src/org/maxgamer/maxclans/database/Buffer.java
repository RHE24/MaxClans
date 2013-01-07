package org.maxgamer.maxclans.database;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.maxgamer.maxclans.MaxClans;

public class Buffer {
	private Database db;
	public boolean locked = false;
	
	public List<BufferStatement> queries = new ArrayList<BufferStatement>(5);
	
	public Buffer(Database db){
		this.db = db;
	}
	
	/**
	 * Adds a query to the buffer
	 * @param q The query to add.  This should be sanitized beforehand.
	 */
	public void addString(final BufferStatement bs){
		Runnable r = new Runnable(){
			public void run() {
				while(locked){
					try {
						//1 millisecond
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				locked = true;
				queries.add(bs);
				locked = false;
				
				if(db.getTask() == null){
					//Database watcher isnt running yet, start it again.
					db.scheduleWatcher();
				}
			}
		};
		Bukkit.getScheduler().runTaskAsynchronously(MaxClans.instance, r);
	}
}
