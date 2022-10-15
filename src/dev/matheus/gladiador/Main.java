package dev.matheus.gladiador;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import dev.matheus.gladiador.listeners.GlobalListener;
import dev.matheus.gladiador.managers.GladiadorManager;
import dev.matheus.gladiador.managers.LocationsManager;
import dev.matheus.gladiador.managers.TopManager;
import dev.matheus.gladiador.sqlite.SQLite;
import dev.matheus.gladiador.sqlite.TablesManager;
import net.milkbowl.vault.economy.Economy;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;

public class Main extends JavaPlugin {
	
	private static Main instance;
	private FileConfiguration mainConfig;
	private FileConfiguration announcementsConfig;
	private FileConfiguration messagesConfig;
	private Economy econ = null;
	private SimpleClans simpleClans;
	private GladiadorManager gladiadorManager;
	private LocationsManager LocationsManager;
	private TopManager topManager;
	private SQLite sqlite;
	private TablesManager tablesManager;
	
	@Override
	public void onEnable() {
		instance = this;
		if (!getDescription().getAuthors().contains("yMatheus_")) {
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		if (!setupEconomy()) {
			logError("N�o foi possivel carregar a api do plugin 'Vault'.");
			logError("Desabilitando " + getDescription().getName() + "...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
		if (!setupSimpleClans()) {
			logError("N�o foi possivel carregar a api do plugin 'SimpleClans'.");
			logError("Desabilitando " + getDescription().getName() + "...");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		this.sqlite = new SQLite("storage.db", instance);
		this.tablesManager = new TablesManager(sqlite);
		this.mainConfig = loadConfig("config.yml");
		this.announcementsConfig = loadConfig("anuncios.yml");
		this.messagesConfig = loadConfig("mensagens.yml");
		this.gladiadorManager = new GladiadorManager(instance);
		this.LocationsManager = new LocationsManager(loadConfig("locations.yml"), instance);
		this.topManager = new TopManager(instance);
		new GlobalListener(instance);
	}
	
	@Override
	public void onLoad() {
		instance = this;
	}
	
	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
	
	private boolean setupSimpleClans() {
		if (getServer().getPluginManager().getPlugin("SimpleClans") == null) {
			return false;
		}
		simpleClans = (SimpleClans) getServer().getPluginManager().getPlugin("SimpleClans");
		return getSimpleClans() != null;
	}
	
	private FileConfiguration loadConfig(String arg0) {
		File file = new File(getDataFolder(), arg0);
		if (!file.exists()) {
			saveResource(arg0, false);
			file = new File(getDataFolder(), arg0);
		}
		return YamlConfiguration.loadConfiguration(file);
	}
	
	public void registerCommand(Command command, String... allys) {
		try {
			List<String> Aliases = new ArrayList<String>();
			for (String s : allys) {
				Aliases.add(s);
			}
			command.setAliases(Aliases);
			Field cmap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			cmap.setAccessible(true);
			CommandMap map = (CommandMap) cmap.get(Bukkit.getServer());
			map.register(command.getName(), getDescription().getName(), command);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void logInfo(String message) {
		System.out.println("[mGladiador] " + message);
	}
	
	public void logError(String message) {
		System.err.println("[mGladiador] " + message);
	}
	
	public String getMessage(String args) {
		return instance.messagesConfig.getString(args).replace("&", "§");
	}

	public List<String> getListMessage(String args) {
		return instance.messagesConfig.getStringList(args);
	}
	
	public static Main getInstance() {
		return instance;
	}
	
	public FileConfiguration getMainConfig() {
		return mainConfig;
	}
	
	public FileConfiguration getAnnouncementsConfig() {
		return announcementsConfig;
	}
	
	public FileConfiguration getMessagesConfig() {
		return messagesConfig;
	}
	
	public GladiadorManager getGladiadorManager() {
		return gladiadorManager;
	}
	
	public LocationsManager getLocationsManager() {
		return LocationsManager;
	}
	
	public TopManager getTopManager() {
		return topManager;
	}
	
	public SimpleClans getSimpleClans() {
		return simpleClans;
	}

	public SQLite getSqlite() {
		return sqlite;
	}

	public TablesManager getTablesManager() {
		return tablesManager;
	}
}