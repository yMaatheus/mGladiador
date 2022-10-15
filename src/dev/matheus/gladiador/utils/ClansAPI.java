package dev.matheus.gladiador.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;

import dev.matheus.gladiador.Main;
import dev.matheus.gladiador.objects.Gladiador;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;

public class ClansAPI {
	
	public static boolean hasClan(Player player) {
		Main instance = Main.getInstance();
		if (instance.getSimpleClans().getClanManager().getClanPlayer(player) != null) {
			return true;
		}
		return false;
	}
	
	public static void setClanFF(Player player, boolean friendlyFire) {
		Main instance = Main.getInstance();
		SimpleClans simpleclans = instance.getSimpleClans();
		if (simpleclans.getClanManager().getClanPlayer(player) != null) {
			simpleclans.getClanManager().getClanPlayer(player).setFriendlyFire(friendlyFire);
		}
	}
	
	public static String getClanTag(Clan clan) {
		if (clan != null) {
			if (clan.getTagLabel() != null) {
				return clan.getTagLabel().replaceAll("[\\[\\]\\s.]", "").replaceAll("(§([0-9|a-f|r]))+", "$1");
			}
			return null;
		}
		return null;
	}
	
	public static ClanPlayer getClanPlayer(Player player) {
		Main instance = Main.getInstance();
		SimpleClans simpleclans = instance.getSimpleClans();
		if (simpleclans.getClanManager().getClanPlayer(player) != null) {
			return simpleclans.getClanManager().getClanPlayer(player);
		}
		return null;
	}
	
	@SuppressWarnings("deprecation")
	public static ClanPlayer getClanPlayer(String arg) {
		Main instance = Main.getInstance();
		SimpleClans simpleclans = instance.getSimpleClans();
		if (simpleclans.getClanManager().getClanPlayer(arg) != null) {
			return simpleclans.getClanManager().getClanPlayer(arg);
		}
		return null;
	}
	
	public static String getPlayerClanTag(ClanPlayer clanPlayer) {
		if (clanPlayer != null) {
			return clanPlayer.getClan().getTagLabel().replaceAll("[\\[\\]\\s.]", "").replaceAll("(§([0-9|a-f|r]))+", "$1");
		}
		return "Nenhum";
	}
	
	public static Clan getClan(String tag) {
		Main instance = Main.getInstance();
		SimpleClans simpleclans = instance.getSimpleClans();
		if (simpleclans.getClanManager().getClan(tag) != null) {
			return simpleclans.getClanManager().getClan(tag);
		}
		return null;
	}
	
	public static List<String> getClanList(Gladiador gladiador) {
		Main instance = Main.getInstance();
		HashMap<String, Integer> clanCount = new HashMap<String, Integer>();
		for (Player player : gladiador.getParticipantes().keySet()) {
			ClanPlayer playerClan = instance.getSimpleClans().getClanManager().getClanPlayer(player);
			if (clanCount.containsKey(playerClan.getTag())) {
				clanCount.put(playerClan.getTag(), clanCount.get(playerClan.getTag()) + 1);
			} else {
				clanCount.put(playerClan.getTag(), 1);
			}
		}
		List<String> list = new ArrayList<String>();
		clanCount.keySet().forEach(key -> list.add(ClansAPI.getClanTag(ClansAPI.getClan(key)) + " §7(§f" + clanCount.get(key) + "§7)§f"));
		return list;
	}
	
	public static List<String> getPlayersList(Gladiador gladiador) {
		List<String> list = new ArrayList<String>();
		gladiador.getParticipantes().keySet().forEach(key -> list.add(key.getName() + " §7(§f" + gladiador.getpKills().get(key.getName().toLowerCase()) + "§7)§f"));
		return list;
	}
}