package org.thewalls.utils;

import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.plugin.java.JavaPlugin;

public class WallsUtils extends JavaPlugin {

	private static WallsUtils instance;

	private static WallsUtils plugin;

	@Override
	public void onLoad() {
		// initialize the plugin
		plugin = this;

		getLogger().info("Loaded");
	}

	@Override
	public void onEnable() {
		getLogger().info("Enabled");
		MySQL.connect(
			"een.minimc.nl", 
			3306, 
			"players", 
			"remote", 
			"AJHSDGajkygdyiwugauiygsdjyGAJSYGDJYAGsdjy"
		);


		TimerTask task = new TimerTask() {
			public void run() {
				MySQL.connect(
				"een.minimc.nl", 
				3306, 
				"players", 
				"remote", 
				"AJHSDGajkygdyiwugauiygsdjyGAJSYGDJYAGsdjy"
				);
			}
		};
		Timer timer = new Timer("Timer");
		timer.schedule(task, 120000);
	}

	@Override
	public void onDisable() {
		getLogger().info("Disabled");
		MySQL.disconnect();
	}

	public static org.bukkit.plugin.Plugin getPlugin() {
		return plugin;
	}

	public static WallsUtils getInstance() {
        return instance;
    }
}

