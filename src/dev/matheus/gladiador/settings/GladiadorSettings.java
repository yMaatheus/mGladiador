package dev.matheus.gladiador.settings;

import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;

import dev.matheus.gladiador.Main;

public class GladiadorSettings {
	
	private Requirements requirements;
	private Announce warning;
	private Announce starting;
	private Deathmatch deathmatch;
	private Winners winners;
	
	public GladiadorSettings(Main instance) {
		FileConfiguration mainConfig = instance.getMainConfig();
		FileConfiguration announcementsConfig = instance.getAnnouncementsConfig();
		this.requirements = new Requirements(mainConfig);
		this.warning = new Announce("chamando", "chamando", mainConfig, announcementsConfig);
		this.starting = new Announce("iniciando", "fechou", mainConfig, announcementsConfig);
		this.deathmatch = new Deathmatch("deathmatch", mainConfig, announcementsConfig);
		this.winners = new Winners(mainConfig);
	}
	
	public Requirements getRequirements() {
		return requirements;
	}
	
	public Announce getWarning() {
		return warning;
	}
	
	public Announce getStarting() {
		return starting;
	}
	
	public Deathmatch getDeathmatch() {
		return deathmatch;
	}
	
	public Winners getWinners() {
		return winners;
	}
	
	public class Requirements {
		private int membersPerClan;
		private int clans;
		private int players;
		
		public Requirements(FileConfiguration mainConfig) {
			this.membersPerClan = mainConfig.getInt("Gladiador.requisitos.membrosMaximosPorClan");
			this.clans = mainConfig.getInt("Gladiador.requisitos.clans");
			this.players = mainConfig.getInt("Gladiador.requisitos.jogadores");
		}
		
		public int getMembersPerClan() {
			return membersPerClan;
		}
		
		public int getClans() {
			return clans;
		}
		
		public int getPlayers() {
			return players;
		}
	}
	
	public class Announce {
		private int alerts;
		private int time;
		private String button = null;
		private List<String> announceMessage;
		
		public Announce(String arg0, String arg1, FileConfiguration mainConfig, FileConfiguration announcementsConfig) {
			String args0String = "Gladiador." + arg0;
			this.alerts = mainConfig.getInt(args0String + ".avisos");
			this.time = mainConfig.getInt(args0String + ".tempo");
			String botao = announcementsConfig.getString(arg1 + ".botao");
			if (botao == null) {
				this.announceMessage = announcementsConfig.getStringList(arg1);
			} else {
				this.button = botao;
				this.announceMessage = announcementsConfig.getStringList(arg1 + ".anuncio");
			}
		}
		
		public int getAlerts() {
			return alerts;
		}
		
		public int getTime() {
			return time;
		}
		
		public String getButton() {
			return button;
		}
		
		public List<String> getAnnounceMessage() {
			return announceMessage;
		}
	}
	
	public class Deathmatch {
		private int alerts;
		private int time;
		private int forceTime;
		private int maxClans; //Acima disso não inicia auto apenas com force (Comando ou time)
		private List<String> announceMessage;
		
		public Deathmatch(String arg, FileConfiguration mainConfig, FileConfiguration announcementsConfig) {
			this.alerts = mainConfig.getInt("Gladiador." + arg + ".avisos");
			this.time = mainConfig.getInt("Gladiador." + arg + ".tempo");
			this.forceTime = mainConfig.getInt("Gladiador." + arg + ".forceDeathmatchtime");
			this.maxClans = mainConfig.getInt("Gladiador." + arg + ".clans");
			this.announceMessage = announcementsConfig.getStringList(arg);
		}
		
		public int getAlerts() {
			return alerts;
		}
		
		public int getTime() {
			return time;
		}
		
		public int getForceTime() {
			return forceTime;
		}
		
		public int getMaxClans() {
			return maxClans;
		}
		
		public List<String> getAnnounceMessage() {
			return announceMessage;
		}
	}
	
	public class Winners {
		private boolean isAlive;
		private String mitoPrefix;
		private String mitoAtual;
		private double mitoPremio;
		private String gladiadorPrefix;
		private List<String> gladiadores;
		private double gladiadorPremio;
		
		public Winners(FileConfiguration mainConfig) {
			String arg = "Gladiador.ganhadores";
			this.isAlive = mainConfig.getBoolean(arg + ".estarVivo");
			this.mitoPrefix = mainConfig.getString(arg + ".mito.prefixo");
			this.mitoAtual = mainConfig.getString(arg + ".mito.atual");
			this.mitoPremio = mainConfig.getDouble(arg + ".mito.premio");
			this.gladiadorPrefix = mainConfig.getString(arg + ".gladiadores.prefixo");
			this.gladiadores = mainConfig.getStringList(arg + ".gladiadores.atuais");
			this.gladiadorPremio = mainConfig.getDouble(arg + ".gladiadores.premio");
		}
		
		public boolean isAlive() {
			return isAlive;
		}
		
		public String getMitoPrefix() {
			return mitoPrefix;
		}
		
		public String getMitoAtual() {
			return mitoAtual;
		}
		
		public void setMitoAtual(String mitoAtual) {
			this.mitoAtual = mitoAtual;
		}
		
		public double getMitoPremio() {
			return mitoPremio;
		}
		
		public String getGladiadorPrefix() {
			return gladiadorPrefix;
		}
		
		public List<String> getGladiadores() {
			return gladiadores;
		}
		
		public void setGladiadores(List<String> gladiadores) {
			this.gladiadores = gladiadores;
		}
		
		public double getGladiadorPremio() {
			return gladiadorPremio;
		}
	}
}