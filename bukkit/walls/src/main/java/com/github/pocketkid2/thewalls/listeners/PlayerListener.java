package com.github.pocketkid2.thewalls.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.github.pocketkid2.thewalls.Arena;
import com.github.pocketkid2.thewalls.MySQL;
import com.github.pocketkid2.thewalls.Status;
import com.github.pocketkid2.thewalls.TheWallsPlugin;

public class PlayerListener implements Listener {

	private TheWallsPlugin plugin;

	public PlayerListener(TheWallsPlugin p) {
		plugin = p;
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (plugin.getGM().isInGame(event.getPlayer()) && (plugin.getGM().getArenaForPlayer(event.getPlayer()).getStatus() != Status.INGAME)) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "The game has not started yet!");
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (plugin.getGM().isProtected(event.getBlock())) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "Please do not break the walls!");
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (plugin.getGM().isInGame(event.getEntity())) {
			Player player = event.getEntity();
			Player killer = event.getEntity().getKiller();
			Arena arena = plugin.getGM().getArenaForPlayer(player);

			player.setHealth(20);
			player.setGameMode(GameMode.SPECTATOR);

			event.setDeathMessage(ChatColor.RED + ChatColor.stripColor(event.getDeathMessage()));

			new BukkitRunnable() {
				@Override
				public void run() {
					player.getInventory().clear();
					player.setHealth(20);
					player.setFoodLevel(20);
					player.setExp(0);
					player.setExhaustion(20);
					player.teleport(plugin.getLobbySpawn());
					arena.removePlayer(player);
					plugin.debug("Resetting player " + player.getName());

					/* Update player stats */
					String playerUUID = player.getUniqueId().toString();
					try {
						PreparedStatement getStats = MySQL.getConnection().prepareStatement("SELECT * FROM walls_players WHERE uuid = ?");
						getStats.setString(1, playerUUID);
						ResultSet rs = getStats.executeQuery();
						if (rs.next() == true) {
							if (rs.getString("name") != "") {
								PreparedStatement updateStats = MySQL.getConnection().prepareStatement("UPDATE walls_players SET name=?, loses=?, xp=?, deaths=? WHERE uuid=?");
								updateStats.setString(1, player.getDisplayName()); /* name */
								updateStats.setInt(2, rs.getInt("loses") + 1); /* Loses */
								updateStats.setInt(3, rs.getInt("xp") + 15); /* XP */
								updateStats.setInt(4, rs.getInt("deaths") + 1); /* Deaths */
								updateStats.setString(5, playerUUID); /* UUID */
								updateStats.executeUpdate();
							}
						} else {
							PreparedStatement updateStats = MySQL.getConnection().prepareStatement("INSERT IGNORE INTO walls_players (uuid,loses,xp,deaths,name) VALUES (?,?,?,?,?)");
								updateStats.setString(1, playerUUID); /* UUID */
								updateStats.setInt(2, 1); /* Loses */
								updateStats.setInt(3, 15); /* XP */
								updateStats.setInt(4, 1); /* Deaths */
								updateStats.setString(5, player.getDisplayName()); /* name */
								updateStats.executeUpdate();
						}
						if (killer != null) {
							player.sendMessage(player.getUniqueId(), ChatColor.RED + "You Died to " + ChatColor.WHITE + killer.getDisplayName() + ChatColor.RED + ", but you still recieved " + ChatColor.WHITE + "15 XP" + ChatColor.RED + "!" + ChatColor.RESET);
						} else {
							player.sendMessage(player.getUniqueId(), ChatColor.RED + "You Died, but you still recieved " + ChatColor.WHITE + "15 XP" + ChatColor.RED + "!" + ChatColor.RESET);
						}
					} catch(Exception e) {
						plugin.debug("An error occured: " + e);
					}

					/* Update killers stats */
					if (killer != null) {
						String killerUUID = killer.getUniqueId().toString();
						try {
							PreparedStatement getStats = MySQL.getConnection().prepareStatement("SELECT * FROM walls_players WHERE uuid = ?");
							getStats.setString(1, killerUUID);
							ResultSet rs = getStats.executeQuery();
							if (rs.next() == true) {
								if (rs.getString("name") != "") {
									PreparedStatement updateStats = MySQL.getConnection().prepareStatement("UPDATE walls_players SET name=?, kills=?, xp=? WHERE uuid=?");
									updateStats.setString(1, player.getDisplayName()); /* name */
									updateStats.setInt(2, rs.getInt("kills") + 1); /* Loses */
									updateStats.setInt(3, rs.getInt("xp") + 10); /* XP */
									updateStats.setString(4, killerUUID); /* UUID */
									updateStats.executeUpdate();
								}
							} else {
								PreparedStatement updateStats = MySQL.getConnection().prepareStatement("INSERT IGNORE INTO walls_players (uuid,kills,xp,name) VALUES (?,?,?,?)");
									updateStats.setString(1, killerUUID); /* UUID */
									updateStats.setInt(2, 1); /* Loses */
									updateStats.setInt(3, 10); /* XP */
									updateStats.setString(4, player.getDisplayName()); /* name */
									updateStats.executeUpdate();
							}
						} catch(Exception e) {
							plugin.debug("An error occured: " + e);
						}
						killer.sendMessage(killer.getUniqueId(), ChatColor.GREEN + "You killed " + ChatColor.WHITE + player.getDisplayName() + ChatColor.GREEN + " and recieved " + ChatColor.WHITE + "10 XP" + ChatColor.GREEN + "!" + ChatColor.RESET);
					}

					if (arena.getPlayers().size() > 1) {
						arena.broadcast(ChatColor.RED + "" + arena.getPlayers().size() + ChatColor.DARK_RED + " players remaining!");
					} else {
						arena.endGame();
					}
				}
			}.runTask(plugin);
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (plugin.getGM().isInGame(event.getPlayer())) {
			Player player = event.getPlayer();
			Arena arena = plugin.getGM().getArenaForPlayer(player);

			event.setQuitMessage(ChatColor.WHITE + player.getDisplayName() + ChatColor.GRAY + " has left the game!");

			player.getInventory().clear();
			player.setHealth(20);
			player.setFoodLevel(20);
			player.setExp(0);
			player.setExhaustion(20);
			player.teleport(plugin.getLobbySpawn());
			arena.removePlayer(player);
			plugin.debug("Resetting player " + player.getName());

			new BukkitRunnable() {
				@Override
				public void run() {
					if (arena.getPlayers().size() > 1) {
						arena.broadcast(ChatColor.RED + "" + arena.getPlayers().size() + ChatColor.DARK_RED + " players remaining!");
					} else {
						arena.endGame();
					}
				}
			};
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		if (plugin.getGM().isInGame(player)) {
			player.sendMessage(ChatColor.RED + "You are already in a game!");
		} else {
			Arena arena = plugin.getGM().getArenaByName("original");
			if (arena == null) {
				player.sendMessage(ChatColor.RED + "The arena doesn't exist!");
			} else {
				switch (arena.getStatus()) {
				case INCOMPLETE:
					player.sendMessage(ChatColor.RED + "That arena has not been fully set up yet, please talk to an admin about it!");
					break;
				case INGAME:
					player.sendMessage(ChatColor.RED + "That game is currently in progress, you can't join now!");
					player.setGameMode(GameMode.SPECTATOR);
					break;
				case RESETTING:
					player.sendMessage(ChatColor.RED + "That arena is resetting, please wait and try again!");
					break;
				case READY:
				case STARTING:
					Location spawn;
					try {
						spawn = arena.getSpawnLocations().get(arena.getPlayers().size());
					} catch (IndexOutOfBoundsException e) {
						player.sendMessage(ChatColor.RED + "That game is full!");
						return;
					}

					// Schedule player join event
					new BukkitRunnable() {
						@Override
						public void run() {
							player.getInventory().clear();
							player.setHealth(20);
							player.setFoodLevel(20);
							player.setExp(0);
							player.setExhaustion(20);
							player.setGameMode(GameMode.SURVIVAL);
							player.teleport(spawn);
							arena.addPlayer(player);
							arena.broadcast(ChatColor.WHITE + player.getDisplayName() + ChatColor.GRAY + " has joined the game! (" + ChatColor.WHITE + arena.getPlayers().size() + ChatColor.GRAY + ")");
						}
					}.runTask(plugin);
					break;
				default:
					break;
				}
			}
		}
	}
}
