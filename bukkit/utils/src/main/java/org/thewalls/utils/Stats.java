package org.thewalls.utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.bukkit.entity.Player;

public class Stats {
	public String getServername(Player p) {
        return p.getServer().getMotd();
    }

    public String getStats(Player p, String identifier) {
        String out = "";
        try {
            PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT * FROM walls_players WHERE uuid = ?");
            ps.setString(1, p.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            
            if (rs.next() == true) {
                Integer wins = rs.getInt("wins");
                Integer loses = rs.getInt("loses");
                Integer xp = rs.getInt("xp");
                Integer kills = rs.getInt("kills");
                Integer deaths = rs.getInt("deaths");
                if (identifier.equals("wins")) {
                    out = wins.toString();
                }
                if (identifier.equals("loses")) {
                    out = loses.toString();
                }
                if (identifier.equals("xp")) {
                    out =  xp.toString();
                }
                if (identifier.equals("kills")) {
                    out = kills.toString();
                }
                if (identifier.equals("deaths")) {
                    out = deaths.toString();
                }
                System.out.printf("[TheWallsPlaceholder] [INFO] Made a SQL request");
            }
        } catch (Exception e) {
            System.out.println(e);
            return "";
        }
        return out;
    }
}
