package com.github.pocketkid2.thewalls;

import com.sk89q.worldedit.entity.Player;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class Placeholders extends PlaceholderExpansion {
    public Integer gameMinutesLeft = 10;

     /*
    The identifier, shouldn't contain any _ or %
     */
    public String getIdentifier() {
        return "wallsgame";
    }

    public String getPlugin() {
        return null;
    }


    /*
     The author of the Placeholder
     This cannot be null
     */
    public String getAuthor() {
        return "GSBRT";
    }

    /*
     Same with #getAuthor() but for versioon
     This cannot be null
     */

    public String getVersion() {
        return "1.0";
    }

    /*
    Use this method to setup placeholders
    This is somewhat similar to EZPlaceholderhook
     */
    public String onPlaceholderRequest(Player player, String identifier) {
        /*
         %tutorial_onlines%
         Returns the number of online players
          */
        if(identifier.equalsIgnoreCase("minutesleft")){
            return gameMinutesLeft.toString();
        }
 
        return null;
    }

    public void updateGameMinutesLeft() {
        gameMinutesLeft--;
        return;
    }
}
