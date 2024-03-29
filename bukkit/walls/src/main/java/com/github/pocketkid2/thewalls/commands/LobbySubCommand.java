package com.github.pocketkid2.thewalls.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.github.pocketkid2.thewalls.TheWallsPlugin;

public class LobbySubCommand extends TheWallsSubCommand {

	public LobbySubCommand(TheWallsPlugin p) {
		super(p);
	}

	@Override
	public boolean mustBePlayer() {
		return true;
	}

	@Override
	public List<String> names() {
		return Arrays.asList("lobby");
	}

	@Override
	public String description() {
		return "Teleports you to the lobby location for The Walls";
	}

	@Override
	public int minArguments() {
		return 0;
	}

	@Override
	public int maxArguments() {
		return 0;
	}

	@Override
	public boolean isAdminCommand() {
		return false;
	}

	@Override
	public String usageMessage() {
		return "lobby";
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) {
		if (plugin.getLobbySpawn() == null) {
			sender.sendMessage(plugin.addPrefix(ChatColor.RED + "The lobby spawn has not been created yet!"));
		} else {
			Player player = (Player) sender;
			player.teleport(plugin.getLobbySpawn(), TeleportCause.COMMAND);
			player.sendMessage(plugin.addPrefix(ChatColor.GRAY + "You have been brought to the lobby!"));
		}
	}

}
