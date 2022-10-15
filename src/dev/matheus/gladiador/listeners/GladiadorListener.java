package dev.matheus.gladiador.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import dev.matheus.gladiador.Main;
import dev.matheus.gladiador.managers.GladiadorManager;
import dev.matheus.gladiador.objects.Gladiador;
import dev.matheus.gladiador.objects.Gladiador.statusType;
import dev.matheus.gladiador.utils.ClansAPI;
import net.sacredlabyrinth.phaed.simpleclans.Clan;

public class GladiadorListener implements Listener {
	
	private Main instance;
	
	public GladiadorListener(Main instance) {
		this.instance = instance;
		Bukkit.getPluginManager().registerEvents(this, instance);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		GladiadorManager gladiadorManager = instance.getGladiadorManager();
		if (gladiadorManager.getGladiador() != null) {
			if ((gladiadorManager.getGladiador().getStatus() == statusType.CHAMANDO) || (gladiadorManager.getGladiador().getStatus() == statusType.FECHADO)) {
				if (gladiadorManager.getGladiador().getParticipantes().containsKey(player)) {
					if (instance.getLocationsManager().getLocationsCache().get("camarote") == null) {
						return;
					}
					if (!(event.getFrom().equals(instance.getLocationsManager().getLocationsCache().get("saida")))) {
						event.setCancelled(true);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerQuitEvent(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		GladiadorManager gladiadorManager = instance.getGladiadorManager();
		if (gladiadorManager.getGladiador() != null) {
			if (gladiadorManager.getGladiador().getParticipantes().containsKey(player)) {
				gladiadorManager.getGladiador().getParticipantes().remove(player);
				Clan clan = instance.getSimpleClans().getClanManager().getClanPlayer(player).getClan();
				gladiadorManager.getGladiador().remove(clan);
			}
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = event.getEntity();
			GladiadorManager gladiadorManager = instance.getGladiadorManager();
			if (gladiadorManager.getGladiador() != null) {
				Gladiador gladiador = gladiadorManager.getGladiador();
				if (gladiador.getParticipantes().containsKey(player)) {
					gladiador.getParticipantes().remove(player);
					Player killer = null;
					if (player.getKiller() != null) {
						killer = player.getKiller();
						String clans = ClansAPI.getClanList(gladiador).toString().replace("[", "").replace("]", "");
						Bukkit.broadcastMessage(instance.getAnnouncementsConfig().getString("clans").replace("@clans", clans).replace("&", "�"));
						if (gladiador.getParticipantes().containsKey(killer)) {
							int kills = gladiador.getpKills().get(killer.getName().toLowerCase()) + 1;
							Bukkit.broadcastMessage(instance.getAnnouncementsConfig().getString("kill").replace("@morto", player.getName()).replace("@killer", killer.getName()).replace("@kills", String.valueOf(kills)).replace("&", "§"));
							gladiador.getpKills().put(killer.getName().toLowerCase(), kills);
						}
					}
					Clan clan = instance.getSimpleClans().getClanManager().getClanPlayer(player).getClan();
					gladiador.remove(clan);
				}
			}
			if (player.getKiller() != null) {
				if (player.getKiller() instanceof Player) { 
					Player killer = player.getKiller();
					if (gladiadorManager.getSettings().getWinners().getMitoAtual().equalsIgnoreCase(player.getName())) {
						instance.getTopManager().setMito(killer);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityDamageEvent(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			GladiadorManager gladiadorManager = instance.getGladiadorManager();
			if (gladiadorManager.getGladiador() != null) {
				if ((gladiadorManager.getGladiador().getStatus() != statusType.PVP) && (gladiadorManager.getGladiador().getStatus() != statusType.DEATHMATCH_PVPON)) {
					if (gladiadorManager.getGladiador().getParticipantes().containsKey(player)) {
						event.setCancelled(true);
					}
				}
			}
		}
	}
}