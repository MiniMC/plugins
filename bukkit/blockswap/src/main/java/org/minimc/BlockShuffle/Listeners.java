package org.minimc.BlockShuffle;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

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

}
