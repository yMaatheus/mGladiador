package dev.matheus.gladiador.managers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import dev.matheus.gladiador.Main;

public class LocationsManager {
	
	private Main instance;
	private FileConfiguration locationsConfig;
	private HashMap<String, Location> locationsCache;
	
	public LocationsManager(FileConfiguration file, Main instance) {
		this.locationsConfig = file;
		this.instance = instance;
		this.locationsCache = new HashMap<String, Location>();
		if (locationsConfig.getConfigurationSection("Locations") != null) {
			locationsConfig.getConfigurationSection("Locations").getKeys(false).forEach(name -> {
				String world = locationsConfig.getString("Locations." + name + ".world");
				double x = locationsConfig.getDouble("Locations." + name + ".x");
				double y = locationsConfig.getDouble("Locations." + name + ".y");
				double z = locationsConfig.getDouble("Locations." + name + ".z");
				float yaw = (float) locationsConfig.getDouble("Locations." + name + ".yaw");
				float pitch = (float) locationsConfig.getDouble("Locations." + name + ".pitch");
				Location location = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
				locationsCache.put(name, location);
			});
		}
	}
	
	public void set(String name, Location location) {
		if (locationsConfig == null) {
			instance.logError("Falha ao tentar localizar YAML 'locations.yml'");
			return;
		}
		this.locationsConfig.set("Locations." + name + ".world", location.getWorld().getName());
		this.locationsConfig.set("Locations." + name + ".x", location.getX());
		this.locationsConfig.set("Locations." + name + ".y", location.getY());
		this.locationsConfig.set("Locations." + name + ".z", location.getZ());
		this.locationsConfig.set("Locations." + name + ".yaw", location.getYaw());
		this.locationsConfig.set("Locations." + name + ".pitch", location.getPitch());
		try {
			this.locationsConfig.save(new File(instance.getDataFolder(), "locations.yml"));
			this.locationsCache.put(name, location);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean hasAll() {
		if (locationsCache.isEmpty()) {
			return false;
		}
		if (!(locationsCache.containsKey("saida"))) {
			return false;
		}
		if (!(locationsCache.containsKey("spawn"))) {
			return false;
		}
		if (!(locationsCache.containsKey("deathmatch"))) {
			return false;
		}
		return true;
	}
	
	public HashMap<String, Location> getLocationsCache() {
		return locationsCache;
	}
}