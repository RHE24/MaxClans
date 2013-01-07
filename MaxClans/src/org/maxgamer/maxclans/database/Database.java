package org.maxgamer.maxclans.database;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.maxgamer.maxclans.MaxClans;

public class Database {
	private DatabaseCore dbCore;
	private Buffer buffer;
	
	private DatabaseWatcher dbw;
	private BukkitTask task;
	
	public Database(File file){
		this.dbCore = new SQLite(file);
		this.buffer = new Buffer(this);
		this.dbw = new DatabaseWatcher(this);
	}
	
	/**
	 * Reschedules the db watcher
	 */
	public void scheduleWatcher(){
		this.task = Bukkit.getScheduler().runTaskLater(MaxClans.instance, this.dbw, 300);
	}
	
	public BukkitTask getTask(){
		return task;
	}
	public void setTask(BukkitTask task){
		this.task = task; 
	}
	
	public DatabaseWatcher getDatabaseWatcher(){
		return this.dbw;
	}
	
	public Buffer getBuffer(){
		return this.buffer;
	}
	
	/**
	 * Returns true if the table exists
	 * @param table The table to check for
	 * @return True if the table is found
	 */
	public boolean hasTable(String table){
		String query = "SELECT * FROM " + table + " LIMIT 0,1";
		try {
			PreparedStatement ps = this.getConnection().prepareStatement(query);
			ps.executeQuery();
			
			return true;
		} catch (SQLException e) {
			return false;
		}
	}
	
	public boolean hasColumn(String table, String column){
		String query = "SELECT * FROM " + table + " LIMIT 0,1";
		try{
			PreparedStatement ps = this.getConnection().prepareStatement(query);
			ResultSet rs = ps.executeQuery();
			rs.findColumn(column); //Throws an exception if the column isnt found...
			
			return true; //Column was found.. hasColumn.
		} 
		catch(SQLException e){
			return false;
		}
	}
	
	public void execute(String query, Object... values){
		BufferStatement bs = new BufferStatement(query, values);
		this.getBuffer().addString(bs);
	}

	/**
	 * Gets the database connection for
	 * executing queries on.
	 * @return The database connection
	 */
	public Connection getConnection(){
		return this.dbCore.getConnection();
	}
	
	/**
	 * Escapes the given string as well as possible.  It is instead recommended to use prepared statements, or, Database.execute(String, objects)
	 * @param s The string to escape - Usually a database value
	 * @return The escaped version of the string.
	 */
	public String escape(String s){
		return this.dbCore.escape(s);
	}
	
}
