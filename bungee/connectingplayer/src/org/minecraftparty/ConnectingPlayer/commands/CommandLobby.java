package org.minecraftparty.ConnectingPlayer.commands;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.minecraftparty.ConnectingPlayer.ConnectingPlayer;

import net.ME1312.SubServers.Bungee.Host.Server;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandLobby extends Command {

    private ConnectingPlayer connectingPlayer;

    public CommandLobby(ConnectingPlayer connectingPlayer) {
        super("lobby", "", "hub");
        this.connectingPlayer = connectingPlayer;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new ComponentBuilder("This command can only be run by a player!").color(ChatColor.RED).create());
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (player.getServer().getInfo().getName().startsWith("LO")) {
            player.sendMessage(new ComponentBuilder("You are already in the lobby!").color(ChatColor.RED).create());
            return;
        }

        Map<String, Server> servers = getConnectingPlayer().getSubAPI().getServers();

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
            player.connect(getConnectingPlayer().getSubAPI().getSubServer(serverNameHigest));
        } else {
            player.sendMessage(new ComponentBuilder(ConnectingPlayer.PrefixedMessage() + "An error occured.").color(ChatColor.RED).create());
        }
    }

    public ConnectingPlayer getConnectingPlayer() {
        return connectingPlayer;
    }
}
