package org.minimc.BlockShuffle;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class BlockShuffle extends JavaPlugin{
	
	private static BlockShuffle instance;
	
	public BukkitTask runnable;
	public boolean blockShuffleEnabled = false;

	public Integer count = 10;
	public Boolean freezePlayers = true;
	public Boolean gameStarted = false;
	
	private List<Player> votes = new ArrayList<Player>();

	public Map <String, Integer> playerDeaths = new HashMap<String, Integer>();
	public Map <String, Integer> playerBlocksFound = new HashMap<String, Integer>();
	public Map <String, Integer> playerXP = new HashMap<String, Integer>();

	private Random random = new Random();
	
	private long previousShuffle = -1;
	private final int SHUFFLE_TIME_MINUTES = 5;
	//private int previousCountdown = -1;
	
	private boolean countdownEnabled = false;
	private BukkitRunnable countdown = new BukkitRunnable() {
		int count = 10;
		@Override
		public void run() {
			if(count < 1)this.cancel();
			if(!countdownEnabled)this.cancel();
			Bukkit.broadcastMessage(addPrefix("The next round is in " + ChatColor.WHITE + count + ChatColor.GRAY + " seconds"));
			count--;
		};
	};
	
	public HashMap<Player, PlayerState> playersInGame = new HashMap<Player, PlayerState>();
	
	public List<Material> allowedMaterials = new ArrayList<Material>(Arrays.asList( new Material[]{
			Material.COBBLESTONE,
			Material.STONE,
			Material.GRASS_BLOCK,
			Material.DIRT,
			Material.OAK_LOG,
			Material.OAK_LEAVES,
			Material.BLAST_FURNACE,
			Material.OAK_WOOD,
			Material.BRICKS,
			Material.DIAMOND_ORE,
			Material.IRON_ORE,
			Material.COAL_ORE,
			Material.COAL_BLOCK,
			Material.FURNACE,
			Material.JUKEBOX,
			Material.BOOKSHELF,
			Material.SAND,
			Material.SANDSTONE,
			Material.CACTUS,
			Material.NETHERRACK,
			Material.QUARTZ_BLOCK,
			Material.SPRUCE_LOG,
			Material.SPRUCE_LEAVES,
			Material.SPRUCE_WOOD,
			Material.BIRCH_LOG,
			Material.BIRCH_LEAVES,
			Material.BIRCH_WOOD,
			Material.SNOW_BLOCK,
			Material.SAND,
			Material.STONE_BRICKS,
			Material.CRACKED_STONE_BRICKS,
			Material.BRICKS,
			Material.MELON,
			Material.OBSIDIAN,
			Material.BOOKSHELF,
			Material.SANDSTONE,
			Material.COAL_BLOCK,
			Material.SOUL_SAND,
			Material.NETHER_BRICKS,
			Material.NETHERRACK,
			Material.GLOWSTONE,
			Material.GOLD_ORE,
			Material.IRON_ORE,
			Material.COAL_ORE,
			Material.TNT,
			Material.DISPENSER,
			Material.DROPPER,
			Material.NOTE_BLOCK,
			Material.REDSTONE_BLOCK,
			Material.HOPPER,
			Material.COMPOSTER,
			Material.ACACIA_PLANKS,
			Material.ACACIA_WOOD,
			Material.BIRCH_LOG,
			Material.BROWN_STAINED_GLASS,
			Material.STONE_SLAB,
			Material.GRAVEL,
			Material.CAKE,
			Material.BEDROCK,
			Material.STONE_SLAB,
			Material.COBBLESTONE_SLAB,
			Material.STONE_BRICK_SLAB,
			Material.GLASS,
			Material.PISTON,
			Material.WATER,
			Material.LAVA,
			Material.HAY_BLOCK,
			Material.DRIED_KELP_BLOCK,
			Material.BELL,
			Material.BLACK_WOOL,
			Material.BLUE_WOOL,
			Material.CYAN_WOOL,
			Material.RED_WOOL,
			Material.YELLOW_WOOL,
			Material.GREEN_WOOL,
			Material.MAGMA_BLOCK,
			Material.ICE,
			Material.PUMPKIN,
			Material.MELON,
			Material.ANVIL,
			Material.GRAY_CONCRETE,
			Material.CHEST,
			Material.BARREL,
			Material.FLETCHING_TABLE,
			Material.CARTOGRAPHY_TABLE,
			Material.GRINDSTONE,
			Material.LOOM,
			Material.FLOWER_POT,
			Material.WHITE_BED,
			Material.ORANGE_BED,
			Material.MAGENTA_BED,
			Material.OAK_SIGN,
			Material.REDSTONE_LAMP,
			Material.REDSTONE_BLOCK,
			Material.GLASS,
			Material.LAPIS_BLOCK,
			Material.SMOOTH_STONE,
			Material.IRON_BARS,
			Material.IRON_BLOCK,
			Material.LIME_WOOL,
			Material.WHITE_WOOL,
			Material.CRAFTING_TABLE,
			Material.SMOKER,
			Material.RAIL,
			Material.JACK_O_LANTERN,
			Material.BONE_BLOCK,
			Material.JUNGLE_LEAVES,
			Material.OAK_FENCE,
			Material.CAULDRON,
			Material.STONECUTTER,
			Material.GRAVEL,
			Material.REDSTONE_ORE,
			Material.MOSSY_COBBLESTONE,
			Material.FARMLAND,
			Material.MAGENTA_WOOL,
			Material.ORANGE_WOOL
	}));
	
	
	@Override
	public void onEnable() {
		BlockShuffle.instance = this;
		this.getCommand("start").setExecutor(new CommandBlockShuffle());
		this.getCommand("start").setTabCompleter(new CommandBlockShuffle());
		for(Material mat : Material.values()) {
			if(mat.name().startsWith("OAK") && mat.name().contains("BOAT"))
				allowedMaterials.add(mat);	
			else if(mat.name().contains("DIORITE"))
				allowedMaterials.add(mat);
			else if(mat.name().contains("ANDESITE"))
				allowedMaterials.add(mat);
			else if(mat.name().contains("GRANITE"))
				allowedMaterials.add(mat);
		}

		startRunnable();
		
		MySQL.connect(
			"een.minimc.nl", 
			3306, 
			"players", 
			"remote", 
			"AJHSDGajkygdyiwugauiygsdjyGAJSYGDJYAGsdjy"
		);
		MySQL_Server.connect(
			"een.minimc.nl", 
			3306, 
			"games", 
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

				MySQL_Server.connect(
				"een.minimc.nl", 
				3306, 
				"games", 
				"remote", 
				"AJHSDGajkygdyiwugauiygsdjyGAJSYGDJYAGsdjy"
				);
			}
		};
		Timer timer = new Timer("Timer");
		timer.schedule(task, 120000);

		getServer().getPluginManager().registerEvents(new Listeners(this), this);

		/* Add game to database */
		try {
			PreparedStatement ps = MySQL_Server.getConnection().prepareStatement("INSERT IGNORE INTO blockshuffle (server_name) VALUES (?)");
			ps.setString(1, Bukkit.getMotd());
			ps.executeUpdate();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	@Override
	public void onDisable() {
		/* Remove game from database */
		try {
			PreparedStatement ps = MySQL_Server.getConnection().prepareStatement("DELETE FROM blockshuffle WHERE server_name = ?");
			ps.setString(1, Bukkit.getMotd());
			ps.executeUpdate();
		} catch (Exception e) {
			System.out.println(e);
		}

		MySQL.disconnect();
	}
	
	public void startRunnable() {
		runnable = new BukkitRunnable() {
			@Override
			public void run() {
				if(blockShuffleEnabled) {
/* 					if (System.currentTimeMillis() - previousShuffle > (SHUFFLE_TIME_MINUTES * 60000) - 10000) {
						startCountdown();
					} */

					if (System.currentTimeMillis() - previousShuffle > SHUFFLE_TIME_MINUTES * 60000){
						// When a new round starts
						previousShuffle = System.currentTimeMillis();
						
						List<Player> failed = new ArrayList<Player>();
						for(Player all : playersInGame.keySet()) {
							if(!playersInGame.get(all).hasFound) {
								// If a player fails to find their block
								Bukkit.broadcastMessage(addPrefix(ChatColor.WHITE + all.getName() + ChatColor.RED + " failed to find their block!"));
								failed.add(all);
								Bukkit.getPlayer(all.getName()).setGameMode(GameMode.SPECTATOR);
								addPlayerXP(all, 15, "losing");
								try {
									Integer playerblocks = 0;
									Integer playerdeath = 0;
									Integer playerpoints = 0;
									if (playerBlocksFound.get(all.getUniqueId().toString()) == null) { playerblocks = 0; } else { playerblocks = playerBlocksFound.get(all.getUniqueId().toString()); }
									if (playerDeaths.get(all.getUniqueId().toString()) == null) { playerdeath = 0; } else { playerdeath = playerDeaths.get(all.getUniqueId().toString()); }
									if (playerXP.get(all.getUniqueId().toString()) == null) { playerpoints = 0; } else { playerpoints = playerXP.get(all.getUniqueId().toString()); }

									System.out.println("Quering database...");
									PreparedStatement getPlayer = MySQL.getConnection().prepareStatement("SELECT * FROM blockshuffle_players WHERE uuid = ?");
									getPlayer.setString(1, all.getUniqueId().toString());
									ResultSet rs = getPlayer.executeQuery();
									if (rs.next() == true) {
										PreparedStatement insertPlayer = MySQL.getConnection().prepareStatement("UPDATE blockshuffle_players SET uuid=?, name=?, loses=?, blocks_found=?, deaths=?, xp=? WHERE uuid=?");
										insertPlayer.setString(1, all.getUniqueId().toString());
										insertPlayer.setString(2, all.getName());
										insertPlayer.setInt(3, rs.getInt("loses") + 1);
										insertPlayer.setInt(4, rs.getInt("blocks_found") + playerblocks);
										insertPlayer.setInt(5, rs.getInt("deaths") + playerdeath);
										insertPlayer.setInt(6, rs.getInt("xp") + playerpoints);
										insertPlayer.setInt(7, rs.getInt("games_played") + 1);
										insertPlayer.executeUpdate();
										System.out.println("Updated " + all.getName() + " in database");
									} else {
										PreparedStatement insertPlayer = MySQL.getConnection().prepareStatement("INSERT IGNORE INTO blockshuffle_players (uuid,name,loses,blocks_found,deaths,xp,games_played) VALUES (?,?,?,?,?,?,?)");
										insertPlayer.setString(1, all.getUniqueId().toString());
										insertPlayer.setString(2, all.getName());
										insertPlayer.setInt(3, 1);
										insertPlayer.setInt(4, playerblocks);
										insertPlayer.setInt(5, playerdeath);
										insertPlayer.setInt(6, playerpoints);
										insertPlayer.setInt(7, 1);
										insertPlayer.executeUpdate();
										System.out.println("Added " + all.getName() + " to database");
									}
								} catch (Exception e) {
									System.out.println(e);
								}
							}
						}
						
						for(Player p : failed) playersInGame.remove(p);
						
						if(playersInGame.size() == 1 && Bukkit.getOnlinePlayers().size() > 1) {
							blockShuffleEnabled = false;
							for(Player p : playersInGame.keySet()) {
								Bukkit.broadcastMessage(addPrefix(ChatColor.WHITE + p.getName() + ChatColor.GREEN + " won BlockShuffle!"));
								Bukkit.getPlayer(p.getName()).setGameMode(GameMode.SPECTATOR);
								addPlayerXP(p, 100, "winning");
								try {
									Integer playerblocks = 0;
									Integer playerdeath = 0;
									Integer playerpoints = 0;
									if (playerBlocksFound.get(p.getUniqueId().toString()) == null) { playerblocks = 0; } else { playerblocks = playerBlocksFound.get(p.getUniqueId().toString()); }
									if (playerDeaths.get(p.getUniqueId().toString()) == null) { playerdeath = 0; } else { playerdeath = playerDeaths.get(p.getUniqueId().toString()); }
									if (playerXP.get(p.getUniqueId().toString()) == null) { playerpoints = 0; } else { playerpoints = playerXP.get(p.getUniqueId().toString()); }
									System.out.println("Quering database...");
									PreparedStatement getPlayer = MySQL.getConnection().prepareStatement("SELECT * FROM blockshuffle_players WHERE uuid = ?");
									getPlayer.setString(1, p.getUniqueId().toString());
									ResultSet rs = getPlayer.executeQuery();
									if (rs.next() == true) {
										PreparedStatement insertPlayer = MySQL.getConnection().prepareStatement("UPDATE blockshuffle_players SET uuid=?, name=?, wins=?, blocks_found=?, deaths=?, xp=? WHERE uuid=?");
										insertPlayer.setString(1, p.getUniqueId().toString());
										insertPlayer.setString(2, p.getName());
										insertPlayer.setInt(3, rs.getInt("wins") + 1);
										insertPlayer.setInt(4, rs.getInt("blocks_found") + playerblocks);
										insertPlayer.setInt(5, rs.getInt("deaths") + playerdeath);
										insertPlayer.setInt(6, rs.getInt("xp") + playerpoints);
										insertPlayer.setInt(7, rs.getInt("games_played") + 1);
										insertPlayer.executeUpdate();
										System.out.println("Updated " + p.getName() + " in database");
									} else {
										PreparedStatement insertPlayer = MySQL.getConnection().prepareStatement("INSERT IGNORE INTO blockshuffle_players (uuid,name,wins,blocks_found,deaths,xp,games_played) VALUES (?,?,?,?,?,?,?)");
										insertPlayer.setString(1, p.getUniqueId().toString());
										insertPlayer.setString(2, p.getName());
										insertPlayer.setInt(3, 1);
										insertPlayer.setInt(4, playerblocks);
										insertPlayer.setInt(5, playerdeath);
										insertPlayer.setInt(6, playerpoints);
										insertPlayer.setInt(7, 1);
										insertPlayer.executeUpdate();
										System.out.println("Added " + p.getName() + " to database");
									}
								} catch (Exception e) {
									System.out.println(e);
								}
								try {
									Bukkit.broadcastMessage(addPrefix("This game will close in " + ChatColor.WHITE + "30" + ChatColor.GRAY +" seconds"));
									TimeUnit.SECONDS.sleep(30);
									Bukkit.getServer().shutdown();
								} catch (Exception e) {
									System.out.print(e);
									Bukkit.getServer().shutdown();
								}
								break;
							}
							return;
						}else if(playersInGame.size() == 0) {
							Bukkit.broadcastMessage(addPrefix(ChatColor.RED + "Nobody managed to find their block. Game over!"));
							blockShuffleEnabled = false;
						}
						
						
						
						
						for(Player all : playersInGame.keySet()) {
							Material mat = generateNewMaterial();
							playersInGame.get(all).setMaterial(mat);
							playersInGame.get(all).setFound(false);
							
							
							
							all.sendMessage(addPrefix("You must find and stand on a §f" + mat.name().toLowerCase().replaceAll("_", " ")));
							
					
						}
					}
					
					boolean found = true;
					for(Player all : playersInGame.keySet()) {
						if(!playersInGame.get(all).hasFound)
							if(all.getLocation().add(0, -0.75, 0).getBlock().getType() == playersInGame.get(all).getMaterial()) {
								// When their block is found
								playersInGame.get(all).setFound(true);
								Bukkit.broadcastMessage(addPrefix(ChatColor.WHITE + all.getName() + ChatColor.GRAY + " has found their block!"));
								all.playSound(all.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
								addPlayerBlockFound(all);
								addPlayerXP(all, 5, "finding a block");
							}else
								found = false;
					}
					
					if(found) {
						previousShuffle = -1;
					}
				}
			}}.runTaskTimer(this, 0, 5);
	}
	
	public void startGame() {
		try {
			PreparedStatement ps = MySQL_Server.getConnection().prepareStatement("UPDATE blockshuffle SET status='playing' WHERE server_name=?");
			ps.setString(1, Bukkit.getMotd());
			ps.executeUpdate();
		} catch (Exception e) {
			System.out.println(e);
		}

		freezePlayers = false;
		playersInGame.clear();
		for(Player all : Bukkit.getOnlinePlayers())
			playersInGame.put(all, new PlayerState());
		blockShuffleEnabled = true;
		previousShuffle = -1;
		gameStarted = true;

		for (final Player player : Bukkit.getOnlinePlayers()) {
			Bukkit.getServer().setWhitelist(true);
			player.setGameMode(GameMode.SURVIVAL);
		}
	}
	
	public void startCountdown() {	
		countdown.runTaskTimer(this, 0, 20);
		
	}
	
	public Material generateNewMaterial() {	
		return allowedMaterials.get(random.nextInt(allowedMaterials.size() - 1));
	}
	
	public static BlockShuffle getInstance() {
		return BlockShuffle.instance;
	}
	
	public class PlayerState{
		Material material = null;
		boolean hasFound = true;
		
		
		public void setMaterial(Material mat) {
			this.material = mat;
		}
		
		public void setFound(boolean found) {
			this.hasFound = found;
		}
		
		public Material getMaterial() {
			return this.material;
		}
		
		public boolean hasFound() {
			return this.hasFound;
		}
		
	}

	public String addPrefix(String message) {
		return(ChatColor.AQUA + "Block Shuffle "+ ChatColor.DARK_GRAY + ChatColor.BOLD + ">> " + ChatColor.RESET + ChatColor.GRAY + " " + message);
	}

	public void castVote(Player player) {
		if (gameStarted == false) {
			votes.add(player);
			Bukkit.broadcastMessage(addPrefix(ChatColor.WHITE + player.getDisplayName() + ChatColor.GRAY + " has voted to start the game! (" + ChatColor.WHITE + votes.size() + ChatColor.GRAY + "/" + ChatColor.WHITE + Bukkit.getOnlinePlayers().size() + ChatColor.GRAY + ")"));
			if ((votes.size() == Bukkit.getOnlinePlayers().size()) && (Bukkit.getOnlinePlayers().size() > 1)) {
				startGame();
			}
		} else {
			player.sendMessage(addPrefix("This game has already started!"));
		}
	}

	public void addPlayerDeath(Player player) {
		Integer deaths = playerDeaths.get(player.getUniqueId().toString());

		if (deaths != null) {
			playerDeaths.put(player.getUniqueId().toString(), deaths + 1);
		} else {
			playerDeaths.put(player.getUniqueId().toString(), 1);
		}
	}

	public void addPlayerBlockFound(Player player) {
		Integer blocksFound = playerBlocksFound.get(player.getUniqueId().toString());

		if (blocksFound != null) {
			playerBlocksFound.put(player.getUniqueId().toString(), blocksFound + 1);
		} else {
			playerBlocksFound.put(player.getUniqueId().toString(), 1);
		}
	}

	public void addPlayerXP(Player player, Integer amount, String reason) {
		Integer xp = playerXP.get(player.getUniqueId().toString());

		if (xp != null) {
			playerXP.put(player.getUniqueId().toString(), xp + amount);
		} else {
			playerXP.put(player.getUniqueId().toString(), amount);
		}

		player.sendMessage(addPrefix(ChatColor.GREEN + "You recieved " + ChatColor.WHITE + amount + " XP" + ChatColor.GREEN + " for " + reason));
	}

	public void debug(String message) {
		System.out.println("[Debug]: " + message);
	}
}
