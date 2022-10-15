package dev.matheus.gladiador.listeners;

import java.sql.SQLException;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import br.com.devpaulo.legendchat.api.events.ChatMessageEvent;
import dev.matheus.gladiador.Main;
import dev.matheus.gladiador.managers.GladiadorManager;
import dev.matheus.gladiador.managers.SpectatorsManager;
import dev.matheus.gladiador.objects.Gladiador;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.events.DisbandClanEvent;

public class GlobalListener implements Listener {
	
	private Main instance;
	
	public GlobalListener(Main instance) {
		this.instance = instance;
		Bukkit.getPluginManager().registerEvents(this, instance);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		String message = event.getMessage().toLowerCase();
		List<String> allowedCommandsGladiador = instance.getMainConfig().getStringList("Comandos.gladiadorHabilitados");
		List<String> blockedCommandsGlobal = instance.getMainConfig().getStringList("Comandos.globalCommandsBloqueados");
		List<String> allowedCommandsEspectadores = instance.getMainConfig().getStringList("Comandos.espectadoresHabilitados");
		GladiadorManager gladiadorManager = instance.getGladiadorManager();
		if (gladiadorManager.getGladiador() == null) {
			return;
		}
		Gladiador gladiador = gladiadorManager.getGladiador();
		if (!(player.hasPermission("gladiador.admin"))) {
			if (gladiador.getParticipantes().containsKey(player)) {
				boolean find = false;
				for (String command : allowedCommandsGladiador) {
					if ((message.startsWith(command + " ") || (message.equalsIgnoreCase(command)))) {
						find = true;
					}
				}
				if (!(find)) {
					event.setCancelled(true);
					instance.getMessage("Gladiador.comandoBloqueado");
					return;
				}
			}
		}
		if (!(player.hasPermission("mgladiador.bypass.globalcommands"))) {
			for (String command : blockedCommandsGlobal) {
				if ((message.startsWith(command + " ") || (message.equalsIgnoreCase(command)))) {
					event.setCancelled(true);
					player.sendMessage("§cComando bloqueado durantea a realizacao o evento gladiador.");
					break;
				}
			}
		}
		if (gladiadorManager.getSpectatorsManager() == null) {
			return;
		}
		SpectatorsManager spectatorsManager = gladiadorManager.getSpectatorsManager();
		List<Player> spectators = spectatorsManager.getSpectators();
		if (spectators.isEmpty()) {
			return;
		}
		if (spectators.contains(player)) {
			boolean find = false;
			for (String command : allowedCommandsEspectadores) {
				if ((message.startsWith(command + " ") || (message.equalsIgnoreCase(command)))) {
					find = true;
				}
			}
			if (!(find)) {
				event.setCancelled(true);
				player.sendMessage("§cComando bloqueado no modo espectador.");
				return;
			}
		}
	}
	
	@EventHandler
	public void onDisbandClanEvent(DisbandClanEvent event) {
		Clan clan = event.getClan();
		try {
			instance.getSqlite().executeUpdate("DELETE FROM gladtop WHERE Clan='" + clan.getTag() + "'", true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@EventHandler
	public void onChatMessageEvent(ChatMessageEvent event) {
		Player player = event.getSender();
		GladiadorManager gladiadorManager = instance.getGladiadorManager();
		if (gladiadorManager.getSettings().getWinners().getMitoAtual().equalsIgnoreCase(player.getName())) {
			if (event.getTags().contains("mito")) {
				event.setTagValue("mito", gladiadorManager.getSettings().getWinners().getMitoPrefix());
			}
		} else if (gladiadorManager.getSettings().getWinners().getGladiadores().contains(player.getName())) {
			if (event.getTags().contains("gladiador")) {
				event.setTagValue("gladiador", gladiadorManager.getSettings().getWinners().getGladiadorPrefix());
			}
		}
	}
}