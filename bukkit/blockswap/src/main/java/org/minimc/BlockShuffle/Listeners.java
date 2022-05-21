package org.minimc.BlockShuffle;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class Listeners implements Listener {

    private BlockShuffle plugin;

    public Listeners(BlockShuffle plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        Player player = (Player) event.getEntity();
        plugin.addPlayerDeath(player);
        
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = (Player) event.getPlayer();
        player.sendMessage(plugin.addPrefix("The game has not started yet! Vote to start with /start!"));
        player.setGameMode(GameMode.ADVENTURE);
    }

    @EventHandler()
    public void onPlayerMove(PlayerMoveEvent event) {
        if (plugin.freezePlayers) {
            Player player = event.getPlayer();
            Location location = player.getLocation();
            player.teleport(location);
        }
    }

}
