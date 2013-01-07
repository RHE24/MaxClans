package org.maxgamer.maxclans;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import java.sql.Connection;

import org.maxgamer.maxclans.clan.ClanManager;
import org.maxgamer.maxclans.database.BufferStatement;
import org.maxgamer.maxclans.database.Database;

public class Converter{
	/**
	 * Converts from v0.2 to v0.3.
	 * 
	 * Adjusts how items are stored in the database.
	 */
	public static boolean convert_02_03() throws Exception{
		Database db = ClanManager.getDatabase();
		
		PreparedStatement ps = db.getConnection().prepareStatement("SELECT * FROM clans");
		ResultSet rs = ps.executeQuery();
		
		String type = ps.getMetaData().getColumnTypeName(13);
		if(type.equalsIgnoreCase("BLOB")){
			return false;
		}
		else if(type.equalsIgnoreCase("null")){
			//No records in the table yet.
			return false;
		}
		
		db.getConnection().close();
		rs.close();
		ps.close();
		
		
		//Copies clans.db to clans.db.0.2.bak, just incase I screw it!
		File existing = new File(MaxClans.instance.getDataFolder(), "clans.db");
		File backup = new File(existing.getAbsolutePath() + ".0.2.bak");
		System.out.println("Backing up to " + backup.getName());
		
		InputStream in = new FileInputStream(existing);
		OutputStream out = new FileOutputStream(backup);
		
		byte[] buf = new byte[1024];
		int len;
		while((len = in.read(buf)) > 0){
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
		
		ps = db.getConnection().prepareStatement("SELECT * FROM clans");
		rs = ps.executeQuery();
		
		System.out.println("Converting 0.2 -> 0.3");
		List<BufferStatement> queries = new LinkedList<BufferStatement>();
		
		//These queries get queued - They do not get executed yet! Stage#1
		queries.add(new BufferStatement("ALTER TABLE clans RENAME TO clans_temp"));
		queries.add(new BufferStatement("CREATE TABLE 'clans' ('name' TEXT(20) NOT NULL, 'ff' INTEGER NOT NULL DEFAULT 0, 'spawnWorld'  TEXT(20) NOT NULL, 'spawnX' INTEGER NOT NULL DEFAULT 0, 'spawnY' INTEGER NOT NULL DEFAULT 0, 'spawnZ' INTEGER NOT NULL DEFAULT 0, 'spawnYaw' INTEGER NOT NULL DEFAULT 0, 'spawnPitch' INTEGER NOT NULL DEFAULT 0, 'kills' INTEGER NOT NULL DEFAULT 0, 'deaths' INTEGER NOT NULL DEFAULT 0, 'isOpen' INTEGER NOT NULL DEFAULT 0, 'motd'  TEXT(100) NOT NULL, 'inv'  BLOB NOT NULL, 'levels'  INTEGER NOT NULL DEFAULT 0, 'respawn'  INTEGER NOT NULL DEFAULT 0, PRIMARY KEY ('name'))"));
		queries.add(new BufferStatement("INSERT INTO clans SELECT * FROM clans_temp"));
		queries.add(new BufferStatement("DROP TABLE clans_temp"));
		
		//Next these queries get queued - They don't get executed yet! Stage#2
		while(rs.next()){
			String data = rs.getString("inv");
			byte[] bytes = data.getBytes("ISO-8859-1");
			
			BufferStatement bs = new BufferStatement("UPDATE clans SET inv = ? WHERE name = ?", bytes, rs.getString("name"));
			queries.add(bs);
		}
		
		ps.close();
		rs.close();
		
		db.getConnection().close();
		//Stage #1 & #2 get executed now!
		for(BufferStatement bs : queries){
			Connection con = db.getConnection();
			
			PreparedStatement st = bs.prepareStatement(con);
			st.execute();
		}
		
		return true;
	}
}