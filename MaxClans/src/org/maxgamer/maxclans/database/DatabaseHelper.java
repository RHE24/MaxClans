package org.maxgamer.maxclans.database;

import java.sql.SQLException;

public class DatabaseHelper{
	public static void setup(Database database) throws SQLException{
		if(!database.hasTable("allies")){
			database.getConnection().prepareStatement("CREATE TABLE 'allies' ('name' TEXT(20) NOT NULL, 'ally' TEXT(20) NOT NULL, PRIMARY KEY ('name', 'ally'))").execute();
			System.out.println("Created allies table...");
		}
		if(!database.hasTable("clans")){
			database.getConnection().prepareStatement("CREATE TABLE 'clans' ('name' TEXT(20) NOT NULL, 'ff' INTEGER NOT NULL DEFAULT 0, 'spawnWorld'  TEXT(20) NOT NULL, 'spawnX' INTEGER NOT NULL DEFAULT 0, 'spawnY' INTEGER NOT NULL DEFAULT 0, 'spawnZ' INTEGER NOT NULL DEFAULT 0, 'spawnYaw' INTEGER NOT NULL DEFAULT 0, 'spawnPitch' INTEGER NOT NULL DEFAULT 0, 'kills' INTEGER NOT NULL DEFAULT 0, 'deaths' INTEGER NOT NULL DEFAULT 0, 'isOpen' INTEGER NOT NULL DEFAULT 0, 'motd'  TEXT(100) NOT NULL, 'inv'  BLOB NOT NULL, 'levels'  INTEGER NOT NULL DEFAULT 0, 'respawn'  INTEGER NOT NULL DEFAULT 0, PRIMARY KEY ('name'))").execute();
			System.out.println("Created clans table...");
		}
		if(!database.hasTable("enemies")){
			database.getConnection().prepareStatement("CREATE TABLE 'enemies' ('name' TEXT(20) NOT NULL, 'enemy' TEXT(20) NOT NULL, PRIMARY KEY ('name', 'enemy'))").execute();
			System.out.println("Created enemies table...");
		}
		if(!database.hasTable("members")){
			database.getConnection().prepareStatement("CREATE TABLE 'members' ('name' TEXT(20) NOT NULL, 'clan' TEXT(20) NOT NULL, 'rank' INTEGER NOT NULL DEFAULT 0, PRIMARY KEY ('name'))").execute();
			System.out.println("Created members table...");
		}
	}
}