package org.minimc.BlockSwap.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;

import org.minimc.BlockSwap.Main;

public class stopBS implements CommandExecutor{
	
	private Main plugin;

	public stopBS(Main plugin) {
		this.plugin = plugin;
		plugin.getCommand("stopBS").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		Player p = (Player) sender;
		p.getServer().broadcastMessage("Stopping Block Shuffle manually!!! XD");
		p.getServer().getScheduler().cancelTasks(this.plugin);
		
		return false;
	}

}
