package com.github.pocketkid2.thewalls.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.pocketkid2.thewalls.Arena;
import com.github.pocketkid2.thewalls.Status;
import com.github.pocketkid2.thewalls.TheWallsPlugin;

public class SetArenaPlayerSignSubCommand extends TheWallsSubCommand {

	public SetArenaPlayerSignSubCommand(TheWallsPlugin p) {
		super(p);
	}

	@Override
	public boolean mustBePlayer() {
		return true;
	}

	@Override
	public List<String> names() {
		return Arrays.asList("setarenaplayersign", "ps");
	}

	@Override
	public String description() {
		return "Sets the arena player sign for the specified arena to the sign you are currently looking at";
	}

	@Override
	public int minArguments() {
		return 1;
	}

	@Override
	public int maxArguments() {
		return 1;
	}

	@Override
	public boolean isAdminCommand() {
		return true;
	}

	@Override
	public String usageMessage() {
		return "setarenaplayersign/ps <name>";
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) {
		Player player = (Player) sender;
		Arena arena = plugin.getGM().getArenaByName(args[0]);
		if (arena == null) {
			player.sendMessage(plugin.addPrefix(ChatColor.RED + "The arena " + ChatColor.GRAY + args[0] + ChatColor.RED + " does not exist!"));
			return;
		}
		if ((arena.getStatus() != Status.INCOMPLETE) && (arena.getStatus() != Status.READY)) {
			player.sendMessage(plugin.addPrefix(ChatColor.RED + "Please wait till the game is finished to modify it's values"));
			return;
		}
		Block b = player.getTargetBlockExact(5);
		if (b == null) {
			player.sendMessage(plugin.addPrefix(ChatColor.RED + "Please get closer to your target player sign!"));
			return;
		}
		arena.setPlayerSign(b.getLocation());
		player.sendMessage(plugin.addPrefix(ChatColor.GRAY + "The sign you have targeted is now the player sign for arena " + ChatColor.GREEN + arena.getName()));
	}

}
