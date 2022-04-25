package org.minecraftparty.ConnectingPlayer;

import net.ME1312.SubServers.Bungee.SubAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.IOException;

import org.minecraftparty.ConnectingPlayer.api.ConfigurationHandler;
import org.minecraftparty.ConnectingPlayer.commands.CommandLobby;
import org.minecraftparty.ConnectingPlayer.commands.CommandWalls;

public class ConnectingPlayer extends Plugin {

    private static ConnectingPlayer instance;

    private ConfigurationHandler configuration;
    private SubAPI subAPI;

    public ConnectingPlayer() {
        instance = this;
    }

    private BaseComponent[] messagePrefix = new ComponentBuilder("PlayerConnecting").color(ChatColor.AQUA).append(" >> ").color(ChatColor.WHITE).create();

    @Override
    public void onEnable() {
        try {
            configuration = new ConfigurationHandler(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* Init SubAPI */
        subAPI = SubAPI.getInstance();

        /* Init Commands */
        getProxy().getPluginManager().registerCommand(this, new CommandLobby(this));
        getProxy().getPluginManager().registerCommand(this, new CommandWalls(this));
        
        /* Init Listeners */
        getProxy().getPluginManager().registerListener(this, new PostLogin(this));
        getProxy().getPluginManager().registerListener(this, new KickListener(this));
    }

    @Override
    public void onDisable() {
        ProxyServer.getInstance().getPluginManager().unregisterCommands(this);
    }

    public static ConnectingPlayer getInstance() {
        return instance;
    }

    public ConfigurationHandler getConfig() {
        return this.configuration;
    }

    public SubAPI getSubAPI() {
        return subAPI;
    }

    public BaseComponent[] getMessagePrefix() {
        return messagePrefix;
    }

    public static String PrefixedMessage() {
        return ChatColor.AQUA + "MiniMC" + ChatColor.DARK_GRAY + ChatColor.BOLD + " >> " + ChatColor.RESET + ChatColor.GRAY;
    }
}