package com.github.pocketkid2.thewalls;

import java.io.File;
import java.io.IOException;
import java.util.Timer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.pocketkid2.thewalls.commands.StartCommand;
import com.github.pocketkid2.thewalls.commands.TheWallsBaseCommand;
import com.github.pocketkid2.thewalls.listeners.PlayerListener;

public class TheWallsPlugin extends JavaPlugin {

	private boolean debug;
	public int serverId;

	private File dataFile;
	private FileConfiguration dataConfig;
	private Location lobbySpawn;
	private GameManager gm;

	public Timer timer = new Timer();

	@Override
	public void onEnable() {
		if( Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")){
			new Placeholders().register();
        }

		ConfigurationSerialization.registerClass(Arena.class);
		ConfigurationSerialization.registerClass(TheWallsRegion.class);

		saveDefaultConfig();
		createDataConfig();

		debug = getConfig().getBoolean("debug", false);
		debug("Debug messages turned on!");

		MySQL.connect(
			getConfig().getString("host"), 
			getConfig().getInt("port"), 
			getConfig().getString("database"), 
			getConfig().getString("username"), 
			getConfig().getString("password")
		);

		getCommand("thewalls").setExecutor(new TheWallsBaseCommand(this));
		getCommand("start").setExecutor(new StartCommand(this));

		lobbySpawn = dataConfig.getSerializable("lobby-spawn-location", Location.class);
		debug("Lobby spawn loaded as " + (lobbySpawn == null ? "null" : TheWallsUtils.printLoc(lobbySpawn)));

		gm = new GameManager(this);

		getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
	}

	@Override
	public void onDisable() {
		timer.cancel();
		gm.shutdown();

		dataConfig.set("lobby-spawn-location", lobbySpawn);
		dataConfig.set("arena-data", gm.getArenas());

		saveConfig();
		try {
			dataConfig.save(dataFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

		MySQL.disconnect();
	}

	private void createDataConfig() {
		dataFile = new File(getDataFolder(), "data.yml");
		if (!dataFile.exists()) {
			dataFile.getParentFile().mkdirs();
			saveResource("data.yml", false);
		}

		dataConfig = new YamlConfiguration();
		try {
			dataConfig.load(dataFile);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	public FileConfiguration getDataConfig() {
		return dataConfig;
	}

	public void log(String message) {
		getLogger().info(message);
	}

	public void warn(String message) {
		getLogger().warning(message);
	}

	public void error(String message) {
		getLogger().severe(message);
	}

	public void debug(String message) {
		if (debug) {
			getLogger().info("[DEBUG] " + message);
		}
	}

	public Location getLobbySpawn() {
		return lobbySpawn;
	}

	public void setLobbySpawn(Location loc) {
		lobbySpawn = loc;
	}

	public GameManager getGM() {
		return gm;
	}

	public String addPrefix(String message) {
		return(ChatColor.AQUA + "Walls "+ ChatColor.DARK_GRAY + ChatColor.BOLD + ">> " + ChatColor.GRAY + ChatColor.RESET + message);
	}

	// Sends a message to all players not in a game
	public void broadcastExcept(String message, Arena arena) {
		for (Player player : getServer().getOnlinePlayers()) {
			if (!arena.isPlayer(player)) {
				player.sendMessage(addPrefix(message));
			}
		}
	}

	public void broadcast(String message) {
		for (Player player : getServer().getOnlinePlayers()) {
			player.sendMessage(message);
		}
	}

	public Integer portNumber = getServer().getPort();
}
