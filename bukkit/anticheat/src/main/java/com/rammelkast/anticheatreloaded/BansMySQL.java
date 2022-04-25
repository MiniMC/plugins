package com.rammelkast.anticheatreloaded;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

public class BansMySQL {
    public static Connection con;
    static ConsoleCommandSender console = Bukkit.getConsoleSender();
    
    private static String host = "een.minimc.nl";
    private static Integer port = 3306;
    private static String database = "bans";
    private static String username = "remote";
    private static String password = "AJHSDGajkygdyiwugauiygsdjyGAJSYGDJYAGsdjy";

    // connect
    public static void connect() {
        if (!isConnected()) {
            try {
                con = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // disconnect
    public static void disconnect() {
        if (isConnected()) {
            try {
                con.close();
                console.sendMessage("[TheWalls] [DEBUG] Disconnected from database!");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // isConnected
    public static boolean isConnected() {
        return (con == null ? false : true);
    }

    // getConnection
    public static Connection getConnection() {
        return con;
    }
}