package org.minecraftparty.ConnectingPlayer;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import net.md_5.bungee.api.chat.TextComponent;
import net.ME1312.Galaxi.Library.Version.Version;
import net.ME1312.SubServers.Bungee.Host.Host;
import net.ME1312.SubServers.Bungee.Host.Server;
import net.ME1312.SubServers.Bungee.Host.SubServer.StopAction;

public class KickListener implements Listener {
    private ConnectingPlayer connectingPlayer;

    public KickListener(ConnectingPlayer connectingPlayer) {
        this.connectingPlayer = connectingPlayer;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerKickEvent(ServerKickEvent event) {

        String reason = BaseComponent.toLegacyText(event.getKickReasonComponent());
        event.setCancelled(true);

        if(reason.contains("banned")) {
            event.getPlayer().disconnect(event.getKickReasonComponent());
            return;
        }

        Map<String, Server> servers = getConnectingPlayer().getSubAPI().getServers();

        /* Get all lobby servers */
        Map<String, Server> lobbyServers = new HashMap<>();
        for (Server server : servers.values()) {
            try {
                getConnectingPlayer().getSubAPI().getSubServer(server.getName()).setStopAction(StopAction.DELETE_SERVER);
                System.out.println("[ConnectingPlayer] server " + server.getName() + " has been marked for deletion at shutdown");
            } catch (Exception e) {
                System.out.println(e);
            }
            if (server.getName().startsWith("LO")) {
                lobbyServers.put(server.getName(), server);
            }
        }

        /* Get highest player count lobby which isn't full yet */
        Map<String, Integer> lobbyServersForPlayer = new TreeMap<>(Collections.reverseOrder());
        for (Server server : servers.values()) {
            if (server.getName().startsWith("LO")) {
                if (server.getPlayers().size() > 45) {
                    break;
                }
                lobbyServersForPlayer.put(server.getName(), server.getPlayers().size());
            }
        }
        
        // Send the player to the lobby
        if (lobbyServersForPlayer.size() != 0) {
            String serverNameHigest = Collections.max(lobbyServersForPlayer.entrySet(), Map.Entry.comparingByValue()).getKey();
            event.getPlayer().connect(getConnectingPlayer().getSubAPI().getSubServer(serverNameHigest));
            System.out.println("[ConnectingPlayer] Server highest players: " + serverNameHigest);
            System.out.println("[ConnectingPlayer] Free lobbies available: " + lobbyServersForPlayer.size() + "/" + lobbyServers.size());
        } else {
            event.getPlayer().disconnect(new TextComponent("Server is starting! Please try again in a few moments."));
        }

        /* Check if there are more than 2 available lobbies */
        if (lobbyServersForPlayer.size() < 2) {
            System.out.println("[ConnectingPlayer] Less than 2 lobbies available! Creating new lobby...");
            // Get a random host server to create the lobby server on
            List<Host> hostsList = new ArrayList<Host>(getConnectingPlayer().getSubAPI().getHosts().values());
            int randomHostIndex = new Random().nextInt(hostsList.size());
            Host randomHost = hostsList.get(randomHostIndex);
            String newLobbyName = "LO" + (lobbyServers.size() + 1);
            // Create the server
            try {
                randomHost.getCreator().create(newLobbyName, randomHost.getCreator().getTemplate("Lobby"), Version.fromString("1.18.2"), null);
            } catch (Exception e) {
                System.out.println("[ConnectingPlayer] Couldn't create lobby as there are no hosts available!");
            }

            System.out.println("[ConnectingPlayer] Created lobby with identifier: " + newLobbyName);
        }
    }
    
    public ConnectingPlayer getConnectingPlayer() {
        return connectingPlayer;
    }
}
