package dev.matheus.gladiador.managers;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import dev.matheus.gladiador.Main;

public class TopManager {
	
	private Main instance;
	private GladiadorManager gladiadorManager;
	private BukkitTask bukkitTask;
	
	private List<GladiadorTop> gladiadorAllValues;
	private List<GladiadorTop> gladiadorTopList;
	
	private List<Mito> mitoAllValues;
	private List<Mito> mitoTopList;
	
	private HashMap<String, Mito> mitoCache;
	
	public TopManager(Main instance) {
		this.instance = instance;
		this.gladiadorManager = instance.getGladiadorManager();
		this.gladiadorAllValues = new ArrayList<>();
		this.gladiadorTopList = new ArrayList<>();
		this.mitoAllValues = new ArrayList<>();
		this.mitoTopList = new ArrayList<>();
		this.mitoCache = new HashMap<>();
		startTask(instance.getMainConfig());
	}
	
	public void startTask(FileConfiguration mainConfig) {
		if (bukkitTask == null) {
			return;
		}
		int update = mainConfig.getInt("Top.update");
		if (update == 0) {
			update = 10;
		}
		bukkitTask = new BukkitRunnable() {
			
			@Override
			public void run() {
				gladiadorUpdateTop();
				mitoUpdateTop();
			}
		}.runTaskTimerAsynchronously(instance, 20L, update * 60 * 20L);
	}
	
	public void stopTask() {
		if (bukkitTask != null) {
			bukkitTask.cancel();
		}
		bukkitTask = null;
	}
	
	public Mito getMito(String playerName) {
		try {
			PreparedStatement pstmt = instance.getSqlite().getConnection().prepareStatement("SELECT * FROM mitos WHERE Name=?");
			pstmt.setString(1, playerName.toLowerCase());
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				Mito mito = new Mito(rs.getString("PlayerRealName"));
				mito.setMitos(rs.getInt("Mitos"));
				return mito;
			}
			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return new Mito(playerName);
	}
	
	public void setMito(String playerName) {
		String tag = gladiadorManager.getSettings().getWinners().getMitoPrefix();
		String message = instance.getAnnouncementsConfig().getString("mito").replace("@newmito", playerName).replace("@tagmito", tag).replace("&", "§");
		Bukkit.broadcastMessage(message);
		instance.getMainConfig().set("Gladiador.ganhadores.mito.atual", playerName);
		try {
			instance.getMainConfig().save(new File(instance.getDataFolder(), "config.yml"));
			gladiadorManager.getSettings().getWinners().setMitoAtual(playerName);
			Mito mito = getMito(playerName);
			mito.setMitos(mito.getMitos() + 1);
			instance.getTablesManager().saveMito(mito);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setMito(Player player) {
		String tag = gladiadorManager.getSettings().getWinners().getMitoPrefix();
		String message = instance.getAnnouncementsConfig().getString("mito").replace("@newmito", player.getName()).replace("@tagmito", tag).replace("&", "§");
		Bukkit.broadcastMessage(message);
		player.getWorld().strikeLightningEffect(player.getLocation());
		player.getWorld().playEffect(player.getLocation(), Effect.PARTICLE_SMOKE, 10);
		instance.getMainConfig().set("Gladiador.ganhadores.mito.atual", player.getName());
		try {
			instance.getMainConfig().save(new File(instance.getDataFolder(), "config.yml"));
			gladiadorManager.getSettings().getWinners().setMitoAtual(player.getName());
			Mito mito = getMito(player.getName().toLowerCase());
			mito.setMitos(mito.getMitos() + 1);
			instance.getTablesManager().saveMito(mito);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public GladiadorTop getClan(String clan) {
		try {
			PreparedStatement pstmt = instance.getSqlite().getConnection().prepareStatement("SELECT * FROM gladtop WHERE Clan=?");
			pstmt.setString(1, clan);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				GladiadorTop gladiadorTop = new GladiadorTop(rs.getString("Clan"));
				gladiadorTop.setWins(rs.getInt("Wins"));
				return gladiadorTop;
			}
			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return new GladiadorTop(clan);
	}
	
	public void resetGladiadorTop(CommandSender sender) {
		try {
			Statement stmt = instance.getSqlite().getConnection().createStatement();
			stmt.execute("DELETE FROM gladtop;");
			stmt.execute("VACUUM");
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		gladiadorUpdateTop();
		sender.sendMessage("§7Gladiador §lTOP §7foi resetado com sucesso.");
	}
	
	public void setWinsInGladiadorTop(String clan, int quantidade, CommandSender sender) {
		if (instance.getSimpleClans().getClanManager().getClan(clan) == null) {
			sender.sendMessage("§cEsse clan não existe.");
			return;
		}
		GladiadorTop gladiadorTop = getClan(clan);
		gladiadorTop.setWins(quantidade);
		instance.getTablesManager().saveClan(gladiadorTop);
		sender.sendMessage("§7Você definiu as vitórias do clan §f" + clan + " §7para §f" + quantidade + "§7.");
	}
	
	public class GladiadorTop {
		private String clan;
		private int wins;
		
		public GladiadorTop(String clan) {
			this.clan = clan;
			this.wins = 0;
		}
		
		public String getClan() {
			return clan;
		}
		
		public int getWins() {
			return wins;
		}
		
		public void setWins(int wins) {
			this.wins = wins;
		}
	}
	
	public class Mito {
		private String name;
		private String playerName;
		private int mitos;
		
		public Mito(String PlayerName) {
			this.name = PlayerName.toLowerCase();
			this.playerName = PlayerName;
			this.mitos = 0;
		}
		
		public String getName() {
			return name;
		}
		
		public String getPlayerName() {
			return playerName;
		}
		
		public int getMitos() {
			return mitos;
		}
		
		public void setMitos(int mitos) {
			if (mitos < 0) {
				mitos = 0;
			}
			if (this.mitos != mitos) {
				this.mitos = mitos;
			}
		}
	}
	
	public void gladiadorUpdateTop() {
		try {
			gladiadorAllValues.clear();
			String query = "SELECT * FROM gladtop;";
			PreparedStatement pstmt = instance.getSqlite().getConnection().prepareStatement(query);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				if (rs.getInt("Wins") > 0) {
					GladiadorTop gladiador = new GladiadorTop(rs.getString("Clan"));
					gladiador.setWins(rs.getInt("Wins"));
					gladiadorAllValues.add(gladiador);
				}
			}
			rs.close();
			pstmt.close();
			getGladiadorTopList().clear();
			getGladiadorTopList().addAll(getGladiadorTop(10));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void mitoUpdateTop() {
		try {
			mitoAllValues.clear();
			String query = "SELECT * FROM mitos;";
			PreparedStatement pstmt = instance.getSqlite().getConnection().prepareStatement(query);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				Mito mito = new Mito(rs.getString("PlayerRealName"));
				mito.setMitos(rs.getInt("Mitos"));
				mitoAllValues.add(mito);
			}
			rs.close();
			pstmt.close();
			getMitoTopList().clear();
			getMitoTopList().addAll(getMitoTop(10));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private List<GladiadorTop> getGladiadorTop(int size) {
		List<GladiadorTop> convert = new ArrayList<>();
		convert.addAll(gladiadorAllValues);
		Collections.sort(convert, new Comparator<GladiadorTop>() {
			
			@Override
			public int compare(GladiadorTop pt1, GladiadorTop pt2) {
				Integer f1 = pt1.getWins();
				Integer f2 = pt2.getWins();
				return f2.compareTo(f1);
			}
		});
		if (convert.size() > size) {
			convert = convert.subList(0, size);
		}
		return convert;
	}
	
	private List<Mito> getMitoTop(int size) {
		List<Mito> convert = new ArrayList<>();
		convert.addAll(mitoAllValues);
		Collections.sort(convert, new Comparator<Mito>() {
			
			@Override
			public int compare(Mito pt1, Mito pt2) {
				Integer f1 = pt1.getMitos();
				Integer f2 = pt2.getMitos();
				return f2.compareTo(f1);
			}
		});
		if (convert.size() > size) {
			convert = convert.subList(0, size);
		}
		return convert;
	}
	
	public List<GladiadorTop> getGladiadorTopList() {
		return gladiadorTopList;
	}
	
	public List<Mito> getMitoTopList() {
		return mitoTopList;
	}
	
	public HashMap<String, Mito> getMitoCache() {
		return mitoCache;
	}
}