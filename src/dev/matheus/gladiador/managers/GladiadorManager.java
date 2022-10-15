package dev.matheus.gladiador.managers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import dev.matheus.gladiador.Main;
import dev.matheus.gladiador.commands.GladiadorCommand;
import dev.matheus.gladiador.commands.MitoCommand;
import dev.matheus.gladiador.listeners.GladiadorListener;
import dev.matheus.gladiador.managers.TopManager.GladiadorTop;
import dev.matheus.gladiador.objects.Gladiador;
import dev.matheus.gladiador.objects.Gladiador.statusType;
import dev.matheus.gladiador.settings.GladiadorSettings;
import dev.matheus.gladiador.utils.ClansAPI;
import net.sacredlabyrinth.phaed.simpleclans.Clan;

public class GladiadorManager {
	
	private Main instance;
	private GladiadorSettings settings;
	private SpectatorsManager spectatorsManager;
	private Gladiador gladiador;
	
	public GladiadorManager(Main instance) {
		this.instance = instance;
		this.settings = new GladiadorSettings(instance);
		this.spectatorsManager = new SpectatorsManager(this, instance);
		this.gladiador = null;
		registerCommands();
		new GladiadorListener(instance);
	}
	
	private void registerCommands() {
		instance.registerCommand(new GladiadorCommand(this, instance), "gladiador");
		instance.registerCommand(new Command("gladiadores") {
			
			@Override
			public boolean execute(CommandSender sender, String arg, String[] args) {
				List<String> gladiadores = settings.getWinners().getGladiadores();
				sender.sendMessage("§7Os gladiadores atuais sao§8: §f" + gladiadores.toString());
				return false;
			}
		}, "gladiadores");
		instance.registerCommand(new MitoCommand(instance), "mito");
	}
	
	public void startGladiadorEvent(CommandSender sender) {
		if (!instance.getLocationsManager().hasAll()) {
			sender.sendMessage(instance.getMessage("Gladiador.semLocations"));
			return;
		}
		if (gladiador != null) {
			sender.sendMessage(instance.getMessage("Gladiador.acontecendo"));
			return;
		}
		this.gladiador = new Gladiador(instance);
		startGladiador();
		sender.sendMessage("§7Iniciando evento gladiador.");
	}
	
	public void startGladiador() {
		int time = settings.getWarning().getTime();
		int alerts = settings.getWarning().getAlerts();
		gladiador.setTask(new BukkitRunnable() {
			
			@Override
			public void run() {
				if (gladiador.getAlerts() >= alerts) {
					int clansSize = gladiador.getClans().size();
					int playersSize = gladiador.getParticipantes().size();
					GladiadorSettings.Requirements requirements = settings.getRequirements();
					if ((clansSize < requirements.getClans()) || (playersSize < requirements.getPlayers())) {
						gladiador.announceCancelNotRequirements(instance);
						gladiador.teleportAll("saida", instance.getMessage("Gladiador.enviadoSaida"), instance);
						gladiador.reset();
						gladiador = null;
						return;
					}
					closeGladiador();
				} else {
					gladiador.announceWarning(settings, alerts, time);
					gladiador.setAlerts(gladiador.getAlerts() + 1);
				}
			}
		}.runTaskTimer(instance, 20L, time * 20L));
	}
	
	public void closeGladiador() {
		gladiador.setStatus(statusType.FECHADO);
		gladiador.reset();
		int time = settings.getStarting().getTime();
		int alerts = settings.getStarting().getAlerts();
		gladiador.setTask(new BukkitRunnable() {
			
			@Override
			public void run() {
				if (gladiador.getAlerts() >= alerts) {
					gladiador.setStatus(statusType.PVP);
					gladiador.reset();
					gladiador.setTime(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(settings.getDeathmatch().getForceTime()));
					gladiador.announceStartPvP(instance);
				} else {
					gladiador.announceClose(settings, alerts, time);
					gladiador.setAlerts(gladiador.getAlerts() + 1);
				}
			}
		}.runTaskTimer(instance, 20L, time * 20L));
	}
	
	public void deathmatch() {
		gladiador.setStatus(statusType.DEATHMATCH_PVPOFF);
		gladiador.setTime(0);
		gladiador.teleportAll("deathmatch", instance.getMessage("Gladiador.deathmacthTeleport"), instance);
		int time = settings.getDeathmatch().getTime();
		int alerts = settings.getDeathmatch().getAlerts();
		gladiador.setTask(new BukkitRunnable() {
			
			@Override
			public void run() {
				if (gladiador.getAlerts() >= alerts) {
					gladiador.setStatus(statusType.DEATHMATCH_PVPON);
					gladiador.announceStartPvP(instance);
					gladiador.reset();
					spectatorsManager.teleportAll("deathmatch", null, instance);
				} else {
					gladiador.announceDeathmatch(settings, alerts, time);
					gladiador.setAlerts(gladiador.getAlerts() + 1);
				}
			}
		}.runTaskTimer(instance, 20L, time * 20L));
	}
	
	public void end() {
		gladiador.reset();
		Clan clanVencedor = gladiador.getClans().get(0);
		Gladiador.Kills mito = null;
		List<Gladiador.Kills> topAll = new ArrayList<>();
		List<Gladiador.Kills> topList = new ArrayList<>();
		for (String name : gladiador.getpKills().keySet()) {
			if (Bukkit.getPlayer(name) != null) {
				Player player = Bukkit.getPlayer(name);
				if ((ClansAPI.hasClan(player)) && (ClansAPI.getClanPlayer(player).getClan() == clanVencedor)) {
					int kills = gladiador.getpKills().get(name).intValue();
					if (instance.getMainConfig().isBoolean("Gladiador.ganhadores.estarVivo")) {
						if (gladiador.getParticipantes().containsKey(player)) {
							topAll.add(gladiador.kills(name, player, Integer.valueOf(kills)));
						}
					} else {
						topAll.add(gladiador.kills(name, player, Integer.valueOf(kills)));
					}
				}
			}
		}
		mito = gladiador.top(1, topAll).get(0);
		topList.addAll(gladiador.top(3, topAll));
		topList.remove(mito);
		final List<String> formatGladiadores = new ArrayList<String>();
		final List<String> formatGladiadoresKills = new ArrayList<String>();
		TopManager topManager = instance.getTopManager();
		topManager.setMito(mito.getPlayer());
		Bukkit.dispatchCommand((CommandSender) Bukkit.getConsoleSender(), "money give " + mito.getPlayer().getName() + " " + settings.getWinners().getMitoPremio());
		topList.forEach(gladiadores -> {
			Bukkit.dispatchCommand((CommandSender) Bukkit.getConsoleSender(), "money give " + gladiadores.getPlayer().getName() + " " + settings.getWinners().getGladiadorPremio());
			formatGladiadores.add(gladiadores.getPlayer().getName());
			formatGladiadoresKills.add(instance.getMessage("formats.gladiadoresKills").replace("@player", gladiadores.getPlayer().getName()).replace("@kills", String.valueOf(gladiadores.getKills())));
		});
		setGladiadores(formatGladiadoresKills);
		String clanTag = ClansAPI.getClanTag(clanVencedor);
		String mitoName = instance.getMessage("formats.mitoKills").replace("@player", mito.getPlayer().getName()).replace("@kills", String.valueOf(mito.getKills()));
		gladiador.announceEnd(clanTag, mitoName, formatGladiadoresKills, instance);
		new BukkitRunnable() {
			public void run() {
				gladiador.teleportAll("saida", instance.getMessage("Gladiador.enviadoSaida"), instance);
				spectatorsManager.removeAll();
				gladiador.reset();
				gladiador = null;
			}
		}.runTaskLater(instance, 300L);
		GladiadorTop gladiadorTop = topManager.getClan(clanVencedor.getTag());
		topManager.setWinsInGladiadorTop(clanVencedor.getTag(), gladiadorTop.getWins() + 1, Bukkit.getConsoleSender());
		topManager.gladiadorUpdateTop();
		topManager.mitoUpdateTop();
	}
	
	public void stop() {
		gladiador.announceCancel(instance);
		gladiador.reset();
		gladiador.teleportAll("saida", instance.getMessage("Gladiador.enviadoSaida"), instance);
		spectatorsManager.removeAll();
		gladiador = null;
	}
	
	public void check() {
		if (gladiador == null) {
			return;
		}
		int clansSize = gladiador.getClans().size();
		if (clansSize == 1) {
			end();
		} else {
			statusType status = gladiador.getStatus();
			if ((status != statusType.PVP) || (status == statusType.DEATHMATCH_PVPOFF) || (status == statusType.DEATHMATCH_PVPON)) {
				return;
			}
			if ((System.currentTimeMillis() > gladiador.getTime()) || (clansSize <= settings.getDeathmatch().getMaxClans())) {
				deathmatch();
			}
		}
	}
	
	public void setGladiadores(List<String> formatGladiadores) {
		try {
			instance.getMainConfig().set("Gladiador.ganhadores.gladiadores.atuais", formatGladiadores);
			instance.getMainConfig().save(new File(instance.getDataFolder(), "config.yml"));
			settings.getWinners().setGladiadores(formatGladiadores);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public GladiadorSettings getSettings() {
		return settings;
	}
	
	public Gladiador getGladiador() {
		return gladiador;
	}
	
	public SpectatorsManager getSpectatorsManager() {
		return spectatorsManager;
	}
}