package dev.matheus.gladiador.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import dev.matheus.gladiador.Main;
import dev.matheus.gladiador.managers.SpectatorsManager;

public class SpectatorsListener implements Listener {
	
	private Main instance;
	
	public SpectatorsListener(Main instance) {
		this.instance = instance;
		Bukkit.getPluginManager().registerEvents(this, instance);
	}
	
	@EventHandler
	public void onEntityDamageEvent(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			SpectatorsManager spectadorManager = instance.getGladiadorManager().getSpectatorsManager();
			if (spectadorManager.getSpectators().contains(player)) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player) {
			Player damager = (Player) event.getDamager();
			SpectatorsManager spectadorManager = instance.getGladiadorManager().getSpectatorsManager();
			if (spectadorManager.getSpectators().contains(damager)) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		SpectatorsManager spectadorManager = instance.getGladiadorManager().getSpectatorsManager();
		if (spectadorManager.getSpectators().contains(player)) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerPickupItemEvent(PlayerPickupItemEvent event) {
		Player player = event.getPlayer();
		SpectatorsManager spectadorManager = instance.getGladiadorManager().getSpectatorsManager();
		if (spectadorManager.getSpectators().contains(player)) {
			event.setCancelled(true);
			return;
		}
	}
}