package dev.matheus.gladiador.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import dev.matheus.gladiador.Main;
import dev.matheus.gladiador.managers.LocationsManager;
import dev.matheus.gladiador.settings.GladiadorSettings;
import dev.matheus.gladiador.utils.TextComponent_B;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;

public class Gladiador {
	
	private List<Clan> clans;
	private HashMap<String, Integer> pKills;
	private HashMap<Player, ClanPlayer> participantes; // Player, ClanPlayer
	private statusType status;
	private long time;
	private BukkitTask task;
	private int alerts;
	
	public Gladiador(Main instance) {
		this.clans = new ArrayList<>();
		this.pKills = new HashMap<String, Integer>();
		this.participantes = new HashMap<Player, ClanPlayer>();
		this.status = statusType.CHAMANDO;
		this.task = null;
		this.alerts = 0;
	}
	
	public void announceWarning(GladiadorSettings settings, int alerts, int time) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			String tempo = String.valueOf((alerts - getAlerts()) * time);
			String clans = String.valueOf(getClans().size());
			String participantes = String.valueOf(getParticipantes().size());
			for (String message : settings.getWarning().getAnnounceMessage()) {
				String messageReplaced = message.replace("@tempo", tempo).replace("@clans", clans).replace("@players", participantes).replace("&", "§");
				if (message.contains("@botao")) {
					messageReplaced = messageReplaced.replace("@botao", settings.getWarning().getButton().replace("&", "§"));
					ClickEvent clickEvent = new ClickEvent(Action.RUN_COMMAND, "/gladiador");
					player.spigot().sendMessage(new TextComponent_B(messageReplaced).setClickEvent(clickEvent).toTextComponent());
				} else {
					player.spigot().sendMessage(new TextComponent_B(messageReplaced).toTextComponent());
				}
			}
		}
	}
	
	public void announceClose(GladiadorSettings settings, int alerts, int time) {
		for (String message : settings.getStarting().getAnnounceMessage()) {
			String tempo = String.valueOf((alerts - getAlerts()) * time);
			String messageReplaced = message.replace("@tempo", tempo).replace("@clans", getClansSize()).replace("@players", getPlayersSize()).replace("&", "§");
			Bukkit.broadcastMessage(messageReplaced);
		}
	}
	
	public void announceStartPvP(Main instance) {
		for (String message : instance.getAnnouncementsConfig().getStringList("iniciou")) {
			String messageReplaced = message.replace("@clans", getClansSize()).replace("@players", getPlayersSize()).replace("&", "§");
			Bukkit.broadcastMessage(messageReplaced);
		}
	}
	
	public void announceDeathmatch(GladiadorSettings settings, int alerts, int time) {
		for (String message : settings.getDeathmatch().getAnnounceMessage()) {
			String tempo = String.valueOf((alerts - getAlerts()) * time);
			String messageReplaced = message.replace("@tempo", tempo).replace("@clans", getClansSize()).replace("@players", getPlayersSize()).replace("&", "§");
			Bukkit.broadcastMessage(messageReplaced);
		}
	}
	
	public void announceEnd(String clanTag, String mitoName, List<String> formatGladiadoresKills, Main instance) {
		for (String message : instance.getAnnouncementsConfig().getStringList("finalizado")) {
			String messageReplaced = message.replace("@clan", clanTag).replace("@ganhadores", getGladiadores(formatGladiadoresKills)).replace("@mito", mitoName).replace("&", "§");
			Bukkit.broadcastMessage(messageReplaced);
		}
	}
	
	public void announceCancelNotRequirements(Main instance) {
		for (String message : instance.getAnnouncementsConfig().getStringList("cancelado.quantidadeInvalida")) {
			Bukkit.broadcastMessage(message.replace("&", "§"));
		}
	}
	
	public void announceCancel(Main instance) {
		for (String message : instance.getAnnouncementsConfig().getStringList("cancelado.geral")) {
			Bukkit.broadcastMessage(message.replace("&", "§"));
		}
	}
	
	public void teleportAll(String location, String message, Main instance) {
		LocationsManager locationsManager = instance.getLocationsManager();
		participantes.keySet().forEach(player -> {
			player.teleport(locationsManager.getLocationsCache().get(location));
			player.sendMessage(message);
		});
	}
	
	public void reset() {
		if (task != null) {
			task.cancel();
		}
		task = null;
		alerts = 0;
	}
	
	public void remove(Clan clan) {
		if (clans.contains(clan)) {
			boolean find = false;
			for (ClanPlayer member : clan.getMembers()) {
				if (getParticipantes().containsValue(member)) {
					find = true;
				}
			}
			if (!(find)) {
				clans.remove(clan);
				if (!(getStatus() == statusType.CHAMANDO)) {
					Main.getInstance().getGladiadorManager().check();
				}
			}
		}
	}
	
	public String getClansSize() {
		if (!(clans.isEmpty()))  {
			return String.valueOf(clans.size());
		}
		return "0";
	}
	
	public String getPlayersSize() {
		if (!(participantes.isEmpty()))  {
			return String.valueOf(participantes.size());
		}
		return "0";
	}
	
	public String getGladiadores(List<String> formatGladiadores) {
		if (!formatGladiadores.isEmpty()) {
			return formatGladiadores.toString().replace("[", "").replace("]", "");
		}
		return "Nenhum";
	}
	
	public Kills kills(String name, Player player, Integer kills) {
		return new Kills(name, player, kills);
	}
	
	public List<Kills> top(int size, List<Kills> topAll) {
		List<Kills> convert = new ArrayList<>();
		convert.addAll(topAll);
		Collections.sort(convert, new Comparator<Kills>() {

			@Override
			public int compare(Kills pt1, Kills pt2) {
				Integer f1 = pt1.getKills();
				Integer f2 = pt2.getKills();
				return f2.compareTo(f1);
			}
		});
		if (convert.size() > size) {
			convert = convert.subList(0, size);
		}
		return convert;
	}
	
	public class Kills {
		private String name;
		private Player player;
		private Integer kills;
		
		public Kills(String name, Player player, Integer kills) {
			this.name = name;
			this.player = player;
			this.kills = kills;
		}
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public Player getPlayer() {
			return player;
		}
		
		public void setPlayer(Player player) {
			this.player = player;
		}
		
		public Integer getKills() {
			return kills;
		}
		
		public void setKills(Integer kills) {
			this.kills = kills;
		}
	}
	
	public List<Clan> getClans() {
		return clans;
	}
	
	public HashMap<String, Integer> getpKills() {
		return pKills;
	}
	
	public HashMap<Player, ClanPlayer> getParticipantes() {
		return participantes;
	}
	
	public statusType getStatus() {
		return status;
	}
	
	public void setStatus(statusType status) {
		this.status = status;
	}
	
	public long getTime() {
		return time;
	}
	
	public void setTime(long time) {
		this.time = time;
	}
	
	public BukkitTask getTask() {
		return task;
	}
	
	public void setTask(BukkitTask task) {
		this.task = task;
	}
	
	public int getAlerts() {
		return alerts;
	}
	
	public void setAlerts(int alerts) {
		this.alerts = alerts;
	}
	
	public enum statusType {
		CHAMANDO, FECHADO, PVP, DEATHMATCH_PVPOFF, DEATHMATCH_PVPON;
	}
}