package com.github.pocketkid2.thewalls;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.pocketkid2.thewalls.tasks.StartGameTask;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;

public class Arena implements ConfigurationSerializable {

	private TheWallsPlugin plugin;

	static {
		ConfigurationSerialization.registerClass(Arena.class);
	}

	private String name;
	private Status status;

	private Location joinSign;
	private Location playerSign;

	private TheWallsRegion arena;

	private List<TheWallsRegion> walls;

	private List<Location> spawns;

	private List<Player> players;

	private BlockArrayClipboard savedState;

	private List<Player> votes;

	// Initial constructor (when nothing but a name is given)
	public Arena(TheWallsPlugin p, String n) {
		plugin = p;

		name = n;
		status = Status.INCOMPLETE;

		joinSign = null;
		playerSign = null;

		arena = null;

		walls = new ArrayList<TheWallsRegion>();

		spawns = new ArrayList<Location>();

		players = new ArrayList<Player>();

		votes = new ArrayList<Player>();
	}

	//
	// DESERIALIZER
	//
	@SuppressWarnings("unchecked")
	public Arena(Map<String, Object> map) {
		plugin = (TheWallsPlugin) Bukkit.getServer().getPluginManager().getPlugin("TheWalls");

		name = (String) map.get("name");

		joinSign = (Location) map.get("join-sign");
		playerSign = (Location) map.get("player-sign");

		arena = (TheWallsRegion) map.get("arena-region");

		walls = (List<TheWallsRegion>) map.get("wall-regions");
		if (walls == null) {
			walls = new ArrayList<TheWallsRegion>();
		}

		spawns = (List<Location>) map.get("spawn-locations");
		if (spawns == null) {
			spawns = new ArrayList<Location>();
		}

		players = new ArrayList<Player>();

		status = Status.INCOMPLETE;

		votes = new ArrayList<Player>();

		checkStatus();
	}

	//
	// SERIALIZER
	//
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>();

		map.put("name", name);

		map.put("join-sign", joinSign);
		map.put("player-sign", playerSign);

		map.put("arena-region", arena);

		map.put("wall-regions", walls);

		map.put("spawn-locations", spawns);

		return map;
	}

	public void checkStatus() {
		if ((status == Status.READY) || (status == Status.INCOMPLETE)) {
			if ((arena != null) && (walls.size() > 0) && (spawns.size() > 1)) {
				status = Status.READY;
			} else {
				status = Status.INCOMPLETE;
			}
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String newName) {
		name = newName;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status newStatus) {
		status = newStatus;
	}

	public Location getJoinSign() {
		return joinSign;
	}

	public void setJoinSign(Location newLoc) {
		joinSign = newLoc;
		checkStatus();
	}

	public Location getPlayerSign() {
		return playerSign;
	}

	public void setPlayerSign(Location newLoc) {
		playerSign = newLoc;
		checkStatus();
	}

	public TheWallsRegion getArenaRegion() {
		return arena;
	}

	public void setArenaRegion(TheWallsRegion newRegion) {
		arena = newRegion;
		checkStatus();
	}

	public List<TheWallsRegion> getWallRegions() {
		return walls;
	}

	public void addWallRegion(TheWallsRegion newRegion) {
		walls.add(newRegion);
		checkStatus();
	}

	public void clearWallRegions() {
		walls.clear();
		checkStatus();
	}

	public List<Location> getSpawnLocations() {
		return spawns;
	}

	public void addSpawnLocation(Location newLoc) {
		spawns.add(newLoc);
		checkStatus();
	}

	public void clearSpawnLocations() {
		spawns.clear();
		checkStatus();
	}

	public List<Player> getPlayers() {
		return players;
	}

	public boolean isPlayer(Player p) {
		return players.contains(p);
	}

	public void broadcast(String message) {
		for (Player p : players) {
			p.sendMessage(plugin.addPrefix(message));
		}
	}

	public void endGame() {
		plugin.debug("endGame() was called on arena " + name + " with status " + status.toString() + " with player count " + players.size());
		if (status == Status.INGAME) {
			// First change status
			status = Status.RESETTING;

			if (players.size() == 1) {
				// We have a winner
				String playerUUID = players.get(0).getUniqueId().toString();
				try {
					PreparedStatement getStats = MySQL.getConnection().prepareStatement("SELECT * FROM walls_players WHERE uuid = ?");
					getStats.setString(1, playerUUID);
					ResultSet rs = getStats.executeQuery();
					if (rs.next() == true) {
						if (rs.getString("name") != "") {
							PreparedStatement updateStats = MySQL.getConnection().prepareStatement("UPDATE walls_players SET name=?, wins=?, xp=? WHERE uuid=?");
							updateStats.setString(1, players.get(0).getDisplayName()); /* name */
							updateStats.setInt(2, rs.getInt("wins") + 1); 
							updateStats.setInt(3, rs.getInt("xp") + 100); 
							updateStats.setString(4, playerUUID); /* UUID */
							updateStats.executeUpdate();
						}
					} else {
						PreparedStatement updateStats = MySQL.getConnection().prepareStatement("INSERT IGNORE INTO walls_players (uuid,loses,xp,deaths,name,wins) VALUES (?,?,?,?,?,?)");
							updateStats.setString(1, playerUUID); /* UUID */
							updateStats.setInt(2, 0); /* Loses */
							updateStats.setInt(3, 100); /* XP */
							updateStats.setInt(4, 0); /* Deaths */
							updateStats.setString(5, players.get(0).getDisplayName()); /* name */
							updateStats.setInt(6, 1); /* Wins */
							updateStats.executeUpdate();
					}
				} catch(Exception e) {
					plugin.debug("An error occured: " + e);
				}
				players.get(0).setGameMode(GameMode.SPECTATOR);
				plugin.broadcast(ChatColor.WHITE + players.get(0).getDisplayName() + ChatColor.GRAY + " won TheWalls!");
			} else {
				// Maybe we have to force stop?
				plugin.broadcast(ChatColor.DARK_RED + "TheWalls arena " + ChatColor.GOLD + " was forced stopped with " + players.size() + " players");
			}

			// Schedule all players be reset
			for (Player player : players) {
				new BukkitRunnable() {
					@Override
					public void run() {
						player.getInventory().clear();
						player.setHealth(20);
						player.setFoodLevel(20);
						player.setExp(0);
						player.setLevel(0);
						player.setExhaustion(20);
						player.teleport(plugin.getLobbySpawn());
						players.remove(player);
						plugin.debug("Resetting player " + player.getName());
					}
				}.runTask(plugin);
			}

			// Schedule the arena to reset
			new BukkitRunnable() {
				@Override
				public void run() {
					restoreState();
					plugin.debug("Resetting arena");
				}
			}.runTask(plugin);

			// Schedule the status to reset after a few seconds
			new BukkitRunnable() {
				@Override
				public void run() {
					status = Status.READY;
					checkStatus();
					plugin.debug("Reset arena status");
				}
			}.runTask(plugin);


			broadcast("This game is shutting down!");
			plugin.getServer().shutdown();
		} else {
			plugin.log("endGame() called on arena " + name + " that was not in game");
		}
	}

	public void saveState() {
		CuboidRegion region = arena.getWorldEditRegion();
		savedState = new BlockArrayClipboard(region);
		try (EditSession session = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(arena.getWorld()))) {
			ForwardExtentCopy extent = new ForwardExtentCopy(session, region, savedState, region.getMinimumPoint());
			extent.setRemovingEntities(true);
			Operations.complete(extent);
		} catch (WorldEditException e) {
			plugin.warn("WorldEdit ran into an exception while TheWalls was executing saveState():");
			e.printStackTrace();
		}
	}

	public void restoreState() {
		if (savedState == null) {
			plugin.warn("Oh no, restoreState() is being called while there is no savedState!");
			return;
		}
		CuboidRegion region = arena.getWorldEditRegion();
		try (EditSession session = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(arena.getWorld()))) {
			Operation operation = new ClipboardHolder(savedState).createPaste(session).to(region.getMinimumPoint()).build();
			Operations.complete(operation);
		} catch (WorldEditException e) {
			plugin.warn("WorldEdit ran into an exception while TheWalls was executing restoreState():");
			e.printStackTrace();
		}
	}

	public boolean isProtected(Block block) {
		for (TheWallsRegion region : walls) {
			if (region.getWorldEditRegion().contains(BlockVector3.at(block.getX(), block.getY(), block.getZ())))
				return true;
		}
		return false;
	}

	public void addPlayer(Player player) {
		players.add(player);
	}

	public void removePlayer(Player player) {
		players.remove(player);
	}

	public boolean hasVoted(Player player) {
		return votes.contains(player);
	}

	public void castVote(Player player) {
		votes.add(player);
		broadcast(ChatColor.GRAY + player.getDisplayName() + " has voted to start the game! (" + ChatColor.WHITE + votes.size() + ChatColor.GRAY + "/" + ChatColor.WHITE + players.size() + ChatColor.GRAY
				+ ")");
		if ((votes.size() == players.size()) && (players.size() > 1)) {
			beginGame();
		}
	}

	private void beginGame() {
		votes.clear();
		status = Status.STARTING;
		new StartGameTask(plugin, this).runTaskTimer(plugin, 0, 20);
		
	}

	public void dropWalls() {
		for (TheWallsRegion region : walls) {
			for (BlockVector3 bv : region.getWorldEditRegion()) {
				arena.getWorld().getBlockAt(bv.getX(), bv.getY(), bv.getZ()).setType(Material.AIR);
			}
		}
	}
}
