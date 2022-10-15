package dev.matheus.gladiador.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.matheus.gladiador.Main;
import dev.matheus.gladiador.managers.GladiadorManager;
import dev.matheus.gladiador.managers.TopManager;
import dev.matheus.gladiador.managers.TopManager.Mito;

public class MitoCommand extends Command {
	
	private Main instance;
	
	public MitoCommand(Main instance) {
		super("mito");
		this.instance = instance;
	}
	
	@Override
	public boolean execute(CommandSender sender, String arg, String[] args) {
		TopManager topManager = instance.getTopManager();
		GladiadorManager gladiadorManager = instance.getGladiadorManager();
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("top")) {
				sender.sendMessage("");
				sender.sendMessage("         §5§lTOP MITO         ");
				sender.sendMessage("  §7(Atulizado a cada 10 minutos) ");
				sender.sendMessage("");
				List<Mito> m = topManager.getMitoTopList();
				for (int a = 0; a < m.size(); a++) {
					Mito b = m.get(a);
					int position = a + 1;
					String playerName = b.getPlayerName();
					int valor = b.getMitos();
					sender.sendMessage(" §f" + position + "§7. §5" + playerName + " §7- §f" + valor + " vezes");
				}
				sender.sendMessage("");
				return true;
			}
			if (args.length >= 2) {
				if ((args[0].equalsIgnoreCase("set")) || (args[0].equalsIgnoreCase("definir"))) {
					if (!(sender.hasPermission("mgladiador.admin"))) {
						sender.sendMessage(instance.getMessage("semPermissao"));
						return false;
					}
					Player player = Bukkit.getPlayer(args[1]);
					if (player == null) {
						sender.sendMessage(instance.getMessage("offline"));
						return false;
					}
					topManager.setMito(player);
				}
				return true;
			}
			return true;
		}
		String mitoAtual = gladiadorManager.getSettings().getWinners().getMitoAtual();
		//String mitoAtual = gladiadorManager.getSettings().getWinners().getMitoAtual();
		sender.sendMessage("");
		sender.sendMessage("§7O mito atual é§8: §f" + mitoAtual);
		Mito mito = topManager.getMito(sender.getName().toLowerCase());
		if (mito != null) {
			sender.sendMessage("§7Você já foi mito§8: §f" + mito.getMitos() + " §7vezes.");
		} else {
			sender.sendMessage("§7Você já foi mito§8: §f0 §7vezes.");
		}
		sender.sendMessage("");
		return false;
	}
}