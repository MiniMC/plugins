package org.minecraftparty.ConnectingPlayer;

import java.awt.Color;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;

public class Listeners implements Listener {
    
    public void PlayerDisconnectEvent(ProxiedPlayer player) {
        try {
            DiscordWebhook webhook = new DiscordWebhook("https://discord.com/api/webhooks/971757084381171733/Vw7gndtECyPAo51TU45m2SBZIwl1zvFRWKSICBqhgYCnbVkmTJSIDJsgrRuu1hTrCrKu");
            webhook.setContent("");
            webhook.setAvatarUrl("https://cdn.minimc.nl/logo.png");
            webhook.setUsername("MiniMC");
            webhook.setTts(false);
            webhook.addEmbed(new DiscordWebhook.EmbedObject()
                .setTitle(player.getDisplayName() + " has connected")
                .setDescription(
                    "**IP: **`" + player.getSocketAddress() + "`\n" +
                    "**Displayname: **`" + player.getDisplayName() + "`\n" +
                    "**UUID: **`" + player.getUniqueId() + "`\n" +
                    "**Ping: **`" + player.getPing() + "`\n" +
                    "**Groups: **`" + player.getGroups() + "`\n"
                )
                .setColor(Color.RED)
                .setFooter("MiniMC", "https://cdn.minimc.nl/logo.png"));
            webhook.execute();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
