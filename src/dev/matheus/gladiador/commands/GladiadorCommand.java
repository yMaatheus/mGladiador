package dev.matheus.gladiador.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.matheus.gladiador.Main;
import dev.matheus.gladiador.managers.GladiadorManager;
import dev.matheus.gladiador.managers.LocationsManager;
import dev.matheus.gladiador.managers.SpectatorsManager;
import dev.matheus.gladiador.managers.TopManager;
import dev.matheus.gladiador.managers.TopManager.GladiadorTop;
import dev.matheus.gladiador.objects.Gladiador;
import dev.matheus.gladiador.objects.Gladiador.statusType;
import dev.matheus.gladiador.settings.GladiadorSettings;
import dev.matheus.gladiador.utils.ClansAPI;
import dev.matheus.gladiador.utils.TimeFormater;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;

public class GladiadorCommand extends Command {
	
	private Main instance;
	private GladiadorManager gladiadorManager;
	
	public GladiadorCommand(GladiadorManager gladiadorManager, Main instance) {
		super("gladiador");
		this.instance = instance;
		this.gladiadorManager = gladiadorManager;
	}
	
	@Override
	public boolean execute(CommandSender sender, String arg, String[] args) {
		if (args.length > 0) {
			if (args.length == 1) {
				if ((args[0].equalsIgnoreCase("sair"))) {
					if (sender instanceof Player) {
						Player player = (Player) sender;
						if (gladiadorManager.getGladiador() == null) {
							sender.sendMessage(instance.getMessage("Gladiador.naoAcontecendo"));
							return true;
						}
						Gladiador gladiador = gladiadorManager.getGladiador();
						if (!(gladiador.getParticipantes().containsKey(player))) {
							sender.sendMessage(instance.getMessage("Gladiador.naoEstaEvento"));
							return true;
						}
						gladiador.getParticipantes().remove(player);
						Clan clan = instance.getSimpleClans().getClanManager().getClanPlayer(player).getClan();
						LocationsManager locationsManager = instance.getLocationsManager();
						gladiador.remove(clan);
				        player.teleport(locationsManager.getLocationsCache().get("saida"));
						player.sendMessage(instance.getMessage("Gladiador.saiu"));
					}
				} else if (args[0].equalsIgnoreCase("camarote")) {
					if (sender instanceof Player) {
						if (gladiadorManager.getGladiador() == null) {
							sender.sendMessage(instance.getMessage("Gladiador.naoAcontecendo"));
							return true;
						}
						Gladiador gladiador = gladiadorManager.getGladiador();
						if (gladiador.getStatus() == statusType.CHAMANDO) {
							sender.sendMessage("§cAguarde o evento fechar para espectar.");
							return true;
						}
						Player player = (Player) sender;
						SpectatorsManager spectatorsManager = gladiadorManager.getSpectatorsManager();
						if (spectatorsManager.getSpectators().contains(player)) {
							if (spectatorsManager.isInCooldown(player)) {
								player.sendMessage(instance.getMessage("Espectador.cooldown"));
								return true;
							}
							spectatorsManager.remove(player);
						} else {
							if (spectatorsManager.isInCooldown(player)) {
								player.sendMessage(instance.getMessage("Espectador.cooldown"));
								return true;
							}
							spectatorsManager.add(player);
						}
					}
				} else if ((args[0].equalsIgnoreCase("specs")) || (args[0].equalsIgnoreCase("spectators"))) {
					if (sender instanceof Player) {
						Player player = (Player) sender;
						SpectatorsManager spectatorsManager = gladiadorManager.getSpectatorsManager();
						if ((sender.hasPermission("mgladiador.command.specs"))||(spectatorsManager.getSpectators().contains(player))){
							if (spectatorsManager.getShowAll().contains(player)) {
								spectatorsManager.removeShowAll(player, true);
							} else {
								spectatorsManager.showAll(player);
							}
						}
					}
				} else if (args[0].equalsIgnoreCase("top")) {
					TopManager topManager = instance.getTopManager();
					sender.sendMessage("");
					sender.sendMessage("         §b§lTOP GLAD         ");
					sender.sendMessage("  §7(Atulizado a cada 10 minutos) ");
					sender.sendMessage("");
					List<GladiadorTop> m = topManager.getGladiadorTopList();
					for (int a = 0; a < m.size(); a++) {
						GladiadorTop b = m.get(a);
						int position = a + 1;
						String clan;
						if (ClansAPI.getClanTag(ClansAPI.getClan(b.getClan())) != null) {
							clan = ClansAPI.getClanTag(ClansAPI.getClan(b.getClan()));
						} else {
							clan = b.getClan();
						}
						sender.sendMessage(" §f" + position + "§7. §b" + clan + " §7- §f" + b.getWins() +" vitórias");
					}
					sender.sendMessage("");
				} else if ((args[0].equalsIgnoreCase("ajuda")) || (args[0].equalsIgnoreCase("help"))) {
					sendHelpCommands(sender, arg);
				} else if (args[0].equalsIgnoreCase("iniciar")) {
					if (sender.hasPermission("mgladiador.admin")) {
						gladiadorManager.startGladiadorEvent(sender);
						return true;
					}
					sender.sendMessage(instance.getMessage("semPermissao"));
				} else if (args[0].equalsIgnoreCase("forcedeathmatch")) {
					if (sender.hasPermission("mgladiador.admin")) {
						if (gladiadorManager.getGladiador() == null) {
							sender.sendMessage(instance.getMessage("Gladiador.naoAcontecendo"));
							return false;
						}
						if (gladiadorManager.getGladiador().getStatus() != statusType.PVP) {
							sender.sendMessage("§cSó é possivel forçar o inicio do deathmatch durante o status de PvP.");
							return false;
						}
						gladiadorManager.deathmatch();
						sender.sendMessage("§7Inicio de deathmacth forçado com sucesso.");
						return true;
					}
					sender.sendMessage(instance.getMessage("semPermissao"));
				} else if ((args[0].equalsIgnoreCase("parar")) || (args[0].equalsIgnoreCase("cancelar"))) {
					if (sender.hasPermission("mgladiador.admin")) {
						if (gladiadorManager.getGladiador() == null) {
							sender.sendMessage(instance.getMessage("Gladiador.naoAcontecendo"));
							return false;
						}
						gladiadorManager.stop();
						return true;
					}
					sender.sendMessage(instance.getMessage("semPermissao"));
				} else if (args[0].equalsIgnoreCase("info")) {
					if (sender.hasPermission("mgladiador.command.info")) {
						sendInfoGladiador(sender);
						return true;
					}
					sender.sendMessage(instance.getMessage("semPermissao"));
				}
			} else if (args.length == 2) {
				if (args[0].equalsIgnoreCase("top")) {
					if (args[1].equalsIgnoreCase("reset")) {
						if (sender.hasPermission("mgladiador.admin")) {
							TopManager topManager = instance.getTopManager();
							topManager.resetGladiadorTop(sender);
							return true;
						}
						sender.sendMessage(instance.getMessage("semPermissao"));
					}
				} else if (args[0].equalsIgnoreCase("set")) {
					if (sender.hasPermission("mgladiador.admin")) {
						if (sender instanceof Player) {
							String name = args[1];
							Player player = (Player) sender;
							boolean find = false;
							if (args[1].equalsIgnoreCase("spawn")) {
								find = true;
							} else if (args[1].equalsIgnoreCase("saida")) {
								find = true;
							} else if (args[1].equalsIgnoreCase("deathmatch")) {
								find = true;
							}
							if (find) {
								LocationsManager locationsManager = instance.getLocationsManager();
								locationsManager.set(name.toLowerCase(), player.getLocation());
								sender.sendMessage("§77Localização §f" + name.toLowerCase() + " §7definida com sucesso!");
							}
						}
						return true;
					}
					sender.sendMessage(instance.getMessage("semPermissao"));
				}
			} else if (args.length == 4) {
				if (args[0].equalsIgnoreCase("top")) {
					if (args[1].equalsIgnoreCase("set")) {
						if (sender.hasPermission("mgladiador.admin")) {
							String clan = args[2].toLowerCase();
							int quantidade = Integer.parseInt(args[3]);
							TopManager topManager = instance.getTopManager();
							topManager.setWinsInGladiadorTop(clan, quantidade, sender);
							topManager.gladiadorUpdateTop();
							return true;
						}
						sender.sendMessage(instance.getMessage("semPermissao"));
					}
				}
			}
			return true;
		}
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (gladiadorManager.getGladiador() == null) {
				sender.sendMessage(instance.getMessage("Gladiador.naoAcontecendo"));
				return true;
			}
			Gladiador gladiador = gladiadorManager.getGladiador();
			if (gladiador.getStatus() != statusType.CHAMANDO) {
				sender.sendMessage(instance.getMessage("Gladiador.fechado"));
				return true;
			}
			if (ClansAPI.hasClan(player) == false) {
				sender.sendMessage(instance.getMessage("Gladiador.semClan"));
				return true;
			}
			if (gladiador.getParticipantes().containsKey(player)) {
				sender.sendMessage("§cVocê já está no evento gladiador.");
				return true;
			}
			SimpleClans simpleClans = instance.getSimpleClans();
			Clan clan = simpleClans.getClanManager().getClanPlayer(player).getClan();
			GladiadorSettings settings = gladiadorManager.getSettings();
			LocationsManager locationsManager = instance.getLocationsManager();
			SpectatorsManager spectatorsManager = gladiadorManager.getSpectatorsManager();
			int i = 0;
			for (ClanPlayer participantes : gladiador.getParticipantes().values()) {
				if (participantes.getClan() == clan) {
					i++;
				}
			}
			if (i >= settings.getRequirements().getMembersPerClan()) {
				sender.sendMessage(instance.getMessage("Gladiador.lotado"));
				return true;
			}
			player.teleport(locationsManager.getLocationsCache().get("spawn"));
			ClansAPI.setClanFF(player, false);
			gladiador.getpKills().put(player.getName().toLowerCase(), 0);
			gladiador.getParticipantes().put(player, simpleClans.getClanManager().getClanPlayer(player));
			if (gladiador.getClans().contains(clan) == false) {
				gladiador.getClans().add(clan);
			}
			sender.sendMessage(instance.getMessage("Gladiador.entrou"));
			if (spectatorsManager.getShowAll().contains(player)) {
				spectatorsManager.removeShowAll(player, false);
			}
		}
		return false;
	}
	
	private void sendHelpCommands(CommandSender sender, String arg) {
		String cmd = " §b /" + arg;
		sender.sendMessage("");
		sender.sendMessage(cmd+" §f- §7Entre no evento quando estiver aberto.");
		sender.sendMessage(cmd+" sair §f- §7Saia do evento gladiador.");
		sender.sendMessage(cmd+" camarote §f- §7Entre/Saia do modo espectador.");
		if (sender.hasPermission("mgladiador.command.specs")){
			sender.sendMessage(cmd+" specs §f- §7Habilite/Desabilite os espectadores.");
		}
		sender.sendMessage(cmd+" top §f- §7Veja os clans que mais ganharam o evento gladiador.");
		sender.sendMessage(cmd+" ajuda §f- §7Ver a lista de comandos.");
		sender.sendMessage("");
		if (sender.hasPermission("mgladiador.admin")) {
			sender.sendMessage(cmd+" iniciar §f- §7Inicie o evento gladiador.");
			sender.sendMessage(cmd+" forcedeathmatch §f- §7Force o inicio do deathmatch.");
			sender.sendMessage(cmd+" parar §f- §7Cancele o evento gladiador.");
			sender.sendMessage(cmd+" top reset §f- §7Limpe o top gladiador.");
			sender.sendMessage(cmd+" top set (tag) (quantidade) §f- §7Defina a quantidade de vitórias de um clan.");
			sender.sendMessage(cmd+" set (spawn | saida | deathmacth) §f- §7Salve as localizaçoes.");
			sender.sendMessage(cmd+" info §f- §7Obtenha informações do evento gladiador.");
			sender.sendMessage("");
		}
	}
	
	private void sendInfoGladiador(CommandSender sender) {
		if (gladiadorManager.getGladiador() == null) {
			sender.sendMessage(instance.getMessage("Gladiador.naoAcontecendo"));
			return;
		}
		Gladiador gladiador = gladiadorManager.getGladiador();
		sender.sendMessage("");
		sender.sendMessage("§b* §7Jogo§8:");
		sender.sendMessage("");
		String status = gladiador.getStatus().toString();
		sender.sendMessage("§7Status§8: §f" + status);
		String tempo = TimeFormater.formatOfEnd(gladiador.getTime());
		statusType s = gladiador.getStatus();
		if ((s == statusType.CHAMANDO) || (s == statusType.FECHADO)) {
			sender.sendMessage("§7Tempo§8: §fAinda irá começar a contagem!");
		} else if ((s == statusType.DEATHMATCH_PVPOFF) || (s == statusType.DEATHMATCH_PVPON)) {
			sender.sendMessage("§7Tempo§8: §fDeathmatch já encontra-se em andamento.");
		} else {
			sender.sendMessage("§7Tempo§8: §f" + tempo);
		}
		sender.sendMessage("");
		sender.sendMessage("§b* §7Clans§8:");
		sender.sendMessage("");
		String clansVivos = ClansAPI.getClanList(gladiador).toString().replace("[", "").replace("]", "");
		sender.sendMessage("§7Clans vivos§8: §f" + clansVivos);
		String nClans = gladiador.getClansSize();
		sender.sendMessage("§7Quantidade§8: §f" + nClans);
		sender.sendMessage("");
		sender.sendMessage("§b* §7Jogadores§8:");
		sender.sendMessage("");
		String playersVivos = ClansAPI.getPlayersList(gladiador).toString().replace("[", "").replace("]", "");
		sender.sendMessage("§7Vivos§8: §f" + playersVivos);
		String nPlayers = gladiador.getPlayersSize();
		sender.sendMessage("§7Quantidade§8: §f" + nPlayers);
		sender.sendMessage("");
	}
}