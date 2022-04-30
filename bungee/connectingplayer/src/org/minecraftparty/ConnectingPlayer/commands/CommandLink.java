package org.minecraftparty.ConnectingPlayer.commands;

import org.minecraftparty.ConnectingPlayer.ConnectingPlayer;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandLink extends Command {
    
    public CommandLink(ConnectingPlayer connectingPlayer) {
        super("link", "", "connect");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new ComponentBuilder(ConnectingPlayer.PrefixedMessage() + "This command can only be run by a player!").color(ChatColor.RED).create());
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) sender;

        try {
            String output = new StringBuilder(player.getUniqueId().toString().replace("-", "")).reverse().toString();
            TextComponent message = new TextComponent(ChatColor.AQUA + "System "+ ChatColor.DARK_GRAY + ChatColor.BOLD + ">> " + ChatColor.GRAY + ChatColor.RESET + " Click me to link your account!");
            message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://minimc.nl/link?code=" + output));
            player.sendMessage(message);
        } catch (Exception e) {
            player.sendMessage(new TextComponent(ChatColor.AQUA + "System "+ ChatColor.DARK_GRAY + ChatColor.BOLD + ">> " + ChatColor.GRAY + ChatColor.RESET + ChatColor.RED + "An error occured"));
            System.out.println("Error while encrypting: " + e.toString());
        }
    }
}
