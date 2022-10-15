package dev.matheus.gladiador.sqlite;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import dev.matheus.gladiador.managers.TopManager.GladiadorTop;
import dev.matheus.gladiador.managers.TopManager.Mito;

public class TablesManager {
	
	private SQLite sqlite;
	
	public TablesManager(SQLite sqlite) {
		this.sqlite = sqlite;
		try {
			sqlite.execute("CREATE TABLE IF NOT EXISTS mitos (Name TEXT, PlayerRealName TEXT, Mitos INTEGER)", false);
			sqlite.execute("CREATE TABLE IF NOT EXISTS gladtop (Clan TEXT, Wins INTEGER)", false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void saveMito(Mito mito) {
		try {
			String query = "SELECT * FROM mitos WHERE Name='" + mito.getName()+ "';";
			Statement stmt = sqlite.getConnection().createStatement();
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next()) {
				stmt.execute("UPDATE mitos SET Mitos='" + mito.getMitos() + "' WHERE Name='" + mito.getName() + "'");
			} else {
				String columns = "(Name, PlayerRealName, Mitos)";
				String values = "('" + mito.getName() + "','" + mito.getPlayerName() + "','" + mito.getMitos() + "')";
				stmt.execute("INSERT INTO mitos " + columns + " " + " VALUES " + values + ";");
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void saveClan(GladiadorTop gladiadorTop) {
		try {
			String query = "SELECT * FROM gladtop WHERE Clan='" + gladiadorTop.getClan()+ "';";
			Statement stmt = sqlite.getConnection().createStatement();
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next()) {
				stmt.execute("UPDATE gladtop SET Wins='" + gladiadorTop.getWins() + "' WHERE Clan='" + gladiadorTop.getClan() + "'");
			} else {
				String columns = "(Clan, Wins)";
				String values = "('" + gladiadorTop.getClan() + "','" + gladiadorTop.getWins() + "')";
				stmt.execute("INSERT INTO gladtop " + columns + " " + " VALUES " + values + ";");
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}