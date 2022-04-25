package com.github.pocketkid2.thewalls.tasks;

import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.pocketkid2.thewalls.Arena;
import com.github.pocketkid2.thewalls.TheWallsPlugin;

public class DropWallsTask extends BukkitRunnable {

	private TheWallsPlugin plugin;
	private int minutesLeft;
	private Arena arena;

	public DropWallsTask(TheWallsPlugin p, int minutes, Arena a) {
		plugin = p;
		minutesLeft = minutes;
		arena = a;
	}

	@Override
	public void run() {
		

		plugin.debug("Running drop walls task, minutesLeft = " + minutesLeft);
		minutesLeft--;
		if (minutesLeft > 0) {
			arena.broadcast(ChatColor.GRAY + "The walls will fall in " + ChatColor.WHITE + minutesLeft + ChatColor.GRAY + " minutes");
		} else {
			arena.dropWalls();
			arena.broadcast(ChatColor.GREEN + "The walls have fallen!");
			cancel();
		}
	}

}
