package com.github.pocketkid2.thewalls.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.pocketkid2.thewalls.Arena;
import com.github.pocketkid2.thewalls.Status;
import com.github.pocketkid2.thewalls.TheWallsPlugin;

public class StartCommand implements CommandExecutor {

	private TheWallsPlugin plugin;

	public StartCommand(TheWallsPlugin p) {
		plugin = p;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player player = (Player) sender;
		if (plugin.getGM().isInGame(player)) {
			Arena arena = plugin.getGM().getArenaForPlayer(player);
			if (arena.getStatus() != Status.READY) {
				player.sendMessage(plugin.addPrefix(ChatColor.RED + "You can only vote to start the game when the game is idle and waiting!"));
				return true;
			}
			if (arena.hasVoted(player)) {
				player.sendMessage(plugin.addPrefix(ChatColor.RED + "You have already voted to start the game!"));
			} else {
				arena.castVote(player);
			}
		}
        return true;
	}
}
