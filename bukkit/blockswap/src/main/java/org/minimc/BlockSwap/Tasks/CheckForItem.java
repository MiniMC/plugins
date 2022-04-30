package org.minimc.BlockSwap.Tasks;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Material;


public class CheckForItem extends BukkitRunnable {

	private Main plugin;

	public CheckForItem(Main p) {
		plugin = p;
	}

	@Override
	public void run() {
			
	
		List<Player> players = arena.getPlayers();

		for (final Player player : players) {
			Location location = player.getLocation();
			location.getBlock().setType(Material.BEDROCK);
			player.teleport(location.add(0,1,0));
		}

		cancel();
	}

}
