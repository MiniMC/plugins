package org.minecraftparty.ConnectingPlayer.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.minecraftparty.ConnectingPlayer.ConnectingPlayer;

import net.ME1312.Galaxi.Library.Version.Version;
import net.ME1312.SubServers.Bungee.Host.Host;
import net.ME1312.SubServers.Bungee.Host.Server;
import net.ME1312.SubServers.Bungee.Host.SubServer.StopAction;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandWalls extends Command {
    
    private ConnectingPlayer connectingPlayer;
    
    public CommandWalls(ConnectingPlayer connectingPlayer) {
        super("walls", "");
        this.connectingPlayer = connectingPlayer;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new ComponentBuilder(ConnectingPlayer.PrefixedMessage() + "This command can only be run by a player!").color(ChatColor.RED).create());
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (player.getServer().getInfo().getName().contains("WA")) {
            player.sendMessage(new ComponentBuilder(ConnectingPlayer.PrefixedMessage() + "You are already connected to a walls game!").color(ChatColor.RED).create());
            return;
        }
        
        Map<String, Server> servers = getConnectingPlayer().getSubAPI().getServers();

        /* Get all walls servers */
        Map<String, Server> wallsServers = new HashMap<>();
        for (Server server : servers.values()) {
            if (server.getName().startsWith("WA")) {
                wallsServers.put(server.getName(), server);
            }
        }

        /* Get highest player count walls game which isn't full yet */
        Map<String, Integer> wallsServersForPlayer = new TreeMap<>(Collections.reverseOrder());
        for (Server server : servers.values()) {
            if (server.getName().startsWith("WA")) {
                if (server.getPlayers().size() >= 4) {
                    return;
                }
                wallsServersForPlayer.put(server.getName(), server.getPlayers().size());
            }
        }
        
        // Send the player to the walls
        if (wallsServersForPlayer.size() != 0) {
            String serverNameHigest = Collections.max(wallsServersForPlayer.entrySet(), Map.Entry.comparingByValue()).getKey();
            player.connect(getConnectingPlayer().getSubAPI().getSubServer(serverNameHigest));
            System.out.println("[ConnectingPlayer] Server highest players: " + serverNameHigest);
            System.out.println("[ConnectingPlayer] Free walls games available: " + wallsServersForPlayer.size() + "/" + wallsServers.size());
        } else {
            player.sendMessage(new TextComponent(ConnectingPlayer.PrefixedMessage() + "Server is starting! Please try again in a few moments."));
        }

        /* Check if there are more than 2 available walls games */
        if (wallsServersForPlayer.size() < 2) {
            System.out.println("[ConnectingPlayer] Less than 2 walls games available! Creating new game server...");
            // Get a random host server to create the lobby server on
            List<Host> hostsList = new ArrayList<Host>(getConnectingPlayer().getSubAPI().getHosts().values());
            int randomHostIndex = new Random().nextInt(hostsList.size());
            Host randomHost = hostsList.get(randomHostIndex);
            String newWallsGameName = "WA" + (wallsServers.size() + 1);
            // Create the server
            try {
                randomHost.getCreator().create(newWallsGameName, randomHost.getCreator().getTemplate("Walls"), Version.fromString("1.18.2"), null);
                connectingPlayer.getSubAPI().getSubServer(newWallsGameName).setStopAction(StopAction.DELETE_SERVER);
                System.out.println("[ConnectingPlayer] Created walls game with identifier: " + newWallsGameName);
            } catch (Exception e) {
                System.out.println("[ConnectingPlayer] Couldn't create walls game as there are no hosts available!");
                System.out.println(e);
            }
        }
    }

    public ConnectingPlayer getConnectingPlayer() {
        return connectingPlayer;
    }
}
