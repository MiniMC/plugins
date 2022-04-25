package com.github.pocketkid2.thewalls.tasks;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Material;

import com.github.pocketkid2.thewalls.Arena;
import com.github.pocketkid2.thewalls.Status;
import com.github.pocketkid2.thewalls.TheWallsPlugin;



public class StartGameTask extends BukkitRunnable {

	private TheWallsPlugin plugin;
	private Arena arena;

	public StartGameTask(TheWallsPlugin p, Arena a) {
		plugin = p;
		arena = a;
	}

	@Override
	public void run() {
		plugin.debug("Running start game task!");
		arena.saveState();
		arena.setStatus(Status.INGAME);
		arena.broadcast(ChatColor.GRAY + "The game has started! You have 10 minutes before the walls disappear!");
		new DropWallsTask(plugin, 10, arena).runTaskTimer(plugin, 0, 20 * 60);
		List<Player> players = arena.getPlayers();

		for (final Player player : players) {
			Location location = player.getLocation();
			location.getBlock().setType(Material.BEDROCK);
			player.teleport(location.add(0,1,0));
		}

		cancel();
	}

}
