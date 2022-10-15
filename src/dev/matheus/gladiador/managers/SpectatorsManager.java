package dev.matheus.gladiador.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import dev.matheus.gladiador.Main;
import dev.matheus.gladiador.listeners.SpectatorsListener;
import dev.matheus.gladiador.objects.Gladiador;
import dev.matheus.gladiador.objects.Gladiador.statusType;

public class SpectatorsManager {
	
	private Main instance;
	private GladiadorManager gladiadorManager;
	
	private ArrayList<Player> spectators;
	private ArrayList<Player> showAll;
	private HashMap<Player, Long> cooldown;
	
	// /gladiador specs (habilitar and desabilitar spectators)
	// /gladiador camarote (join and leave spectator mode)
	
	public SpectatorsManager(GladiadorManager gladiadorManager, Main instance) {
		this.instance = instance;
		this.gladiadorManager = gladiadorManager;
		this.spectators = new ArrayList<>();
		this.showAll = new ArrayList<>();
		this.cooldown = new HashMap<Player, Long>();
		new SpectatorsListener(instance);
	}
	
	public void add(Player player) {
		PlayerInventory inventory = player.getInventory();
		if (hasInvFull(inventory)) {
			player.sendMessage(instance.getMessage("Espectador.inventarioCheio"));
			return;
		}
		Gladiador gladiador = gladiadorManager.getGladiador();
		statusType status = gladiador.getStatus();
		Location spectatorSpawn;
		for (Player online : Bukkit.getOnlinePlayers()) {
			if (showAll.contains(online)) {
				online.showPlayer(player);
			} else {
				online.hidePlayer(player);
			}
		}
 		//Bukkit.getOnlinePlayers().forEach(online -> online.hidePlayer(player));
		LocationsManager locationsManager = instance.getLocationsManager();
		if ((status == statusType.DEATHMATCH_PVPOFF) || (status == statusType.DEATHMATCH_PVPON)) {
			spectatorSpawn = locationsManager.getLocationsCache().get("deathmatch");
		} else {
			spectatorSpawn = locationsManager.getLocationsCache().get("spawn");
		}
		player.teleport(spectatorSpawn);
		player.setAllowFlight(true);
		spectators.add(player);
		//inventory.setContents(this.getSpectatorInventory());
		player.sendMessage(instance.getMessage("Espectador.entrou"));
	}
	
	public void remove(Player player) {
		// remove invisibility
		// teleport to spawn
		// clear inventory
		PlayerInventory inventory = player.getInventory();
		for (Player online : Bukkit.getOnlinePlayers()) {
			online.showPlayer(player);
		}
		if (showAll.contains(player)) {
			for (Player spec : getSpectators()) {
				player.hidePlayer(spec);
			}
			showAll.remove(player);
		}
		LocationsManager locationsManager = instance.getLocationsManager();
		player.teleport(locationsManager.getLocationsCache().get("saida"));
		player.setAllowFlight(false);
		spectators.remove(player);
		putCooldown(player);
		inventory.clear();
		inventory.setHelmet(null);
		inventory.setChestplate(null);
		inventory.setLeggings(null);
		inventory.setBoots(null);
		player.updateInventory();
		player.sendMessage(instance.getMessage("Espectador.saiu"));
	}
	
	public void showAll(Player player) {
		// show all specs
		// add to list
		if (isInCooldown(player)) {
			player.sendMessage(instance.getMessage("Espectador.cooldown"));
			return;
		}
		for (Player spec : getSpectators()) {
			player.showPlayer(spec);
		}
		showAll.add(player);
		putCooldown(player);
		player.sendMessage(instance.getMessage("Espectador.espectadoresHabilitados"));
	}
	
	public void removeShowAll(Player player, boolean delay) {
		if (delay) {
			if (isInCooldown(player)) {
				player.sendMessage(instance.getMessage("Espectador.cooldown"));
				return;
			}
		}
		for (Player spec : getSpectators()) {
			player.hidePlayer(spec);
		}
		this.showAll.remove(player);
		putCooldown(player);
		player.sendMessage(instance.getMessage("Espectador.espectadoresDesabilitados"));
	}
	
	public void teleportAll(String location, String message, Main instance) {
		if (!(spectators.isEmpty())) {
			LocationsManager locationsManager = instance.getLocationsManager();
			spectators.forEach(player -> {
				player.teleport(locationsManager.getLocationsCache().get(location));
				player.sendMessage(message);
			});
		}
	}
	
	public void removeAll() {
		if (!(spectators.isEmpty())) {
			for (Player spectator : spectators) {
				PlayerInventory inventory = spectator.getInventory();
				for (Player online : Bukkit.getOnlinePlayers()) {
					online.showPlayer(spectator);
				}
				if (showAll.contains(spectator)) {
					for (Player spec : getSpectators()) {
						spectator.hidePlayer(spec);
					}
					showAll.remove(spectator);
				}
				LocationsManager locationsManager = instance.getLocationsManager();
				spectator.teleport(locationsManager.getLocationsCache().get("saida"));
				spectator.setAllowFlight(false);
				inventory.clear();
				inventory.setHelmet(null);
				inventory.setChestplate(null);
				inventory.setLeggings(null);
				inventory.setBoots(null);
				spectator.updateInventory();
			}
			spectators.clear();
		}
	}
	
	public ArrayList<Player> getShowAll() {
		ArrayList<Player> showAllReturn = new ArrayList<>();
		showAllReturn.addAll(showAll);
		return showAllReturn;
	}
	
	private boolean hasInvFull(PlayerInventory playerInventory) {
		for (ItemStack itemstack : playerInventory.getContents()){if (itemstack != null) {return true;}}
		ItemStack helmet = playerInventory.getHelmet();
		ItemStack chestplate = playerInventory.getHelmet();
		ItemStack leggings = playerInventory.getHelmet();
		ItemStack boots = playerInventory.getHelmet();
		if ((helmet != null)||(chestplate != null)||(leggings != null)||(boots != null)) {return true;}
		return false;
	}
	
	public ArrayList<Player> getSpectators() {
		ArrayList<Player> spectatorsReturn = new ArrayList<>();
		spectatorsReturn.addAll(spectators);
		return spectatorsReturn;
	}
	
	public void removeSpectator(Player player) {
		ArrayList<Player> spectatorsReturn = new ArrayList<>();
		spectatorsReturn.addAll(spectators);
		spectatorsReturn.remove(player);
		this.spectators = spectatorsReturn;
	}
	
	public boolean isInCooldown(Player player) {
		if (cooldown.containsKey(player)) {
			if (System.currentTimeMillis() <= cooldown.get(player)) {
				return true;
			}
		}
		return false;
	}
	
	private void putCooldown(Player player) {
		cooldown.put(player, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30));
	}
}