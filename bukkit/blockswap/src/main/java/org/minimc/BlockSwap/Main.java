package org.minimc.BlockSwap;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;

import org.minimc.BlockSwap.Commands.startBS;
import org.minimc.BlockSwap.Commands.stopBS;
import org.minimc.BlockSwap.Listeners.ListenBS;

public class Main extends JavaPlugin{

	public HashMap<String, Material> plmap = new HashMap<String, Material>();
	
	@Override
	public void onEnable() {
		new startBS(this);
		new stopBS(this);
		new ListenBS(this);
	}

	public void castVote(Player player) {
		votes.add(player);
		broadcast(ChatColor.GRAY + player.getDisplayName() + " has voted to start the game! (" + ChatColor.GOLD + votes.size() + ChatColor.GRAY + "/" + ChatColor.GOLD + players.size() + ChatColor.GRAY
				+ ")");
		if ((votes.size() == players.size()) && (players.size() > 1)) {
			beginGame();
		}
	}
}
