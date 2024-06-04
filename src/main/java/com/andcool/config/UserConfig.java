package com.andcool.config;

import com.andcool.OAuthServer;
import com.andcool.sillyLogger.Level;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class UserConfig {
    public static int PORT_SERVER = 25565;
    public static int PORT_API = 8089;
    public static String SERVER_ID = "mc-oauth";
    public static String MOTD = "";
    public static String SERVER_VERSION = "1.20.4";
    public static int PLAYERS_MAX = 0;
    public static int PLAYERS_NOW = 0;
    public static int PROTOCOL_VERSION = -1;

    /*
    Save config to file
     */
    public static void save() {
        final File configFile = new File("./config.json");
        JSONObject jsonConfig = new JSONObject();
        jsonConfig.put("PORT_SERVER", PORT_SERVER);
        jsonConfig.put("PORT_API", PORT_API);
        jsonConfig.put("SERVER_ID", SERVER_ID);
        jsonConfig.put("MOTD", MOTD);
        jsonConfig.put("SERVER_VERSION", SERVER_VERSION);
        jsonConfig.put("PLAYERS_MAX", PLAYERS_MAX);
        jsonConfig.put("PLAYERS_NOW", PLAYERS_NOW);
        jsonConfig.put("PROTOCOL_VERSION", PROTOCOL_VERSION);
        try {
            Files.createDirectories(configFile.toPath().getParent());
            Files.writeString(configFile.toPath(), jsonConfig.toString(4));
        } catch (IOException e) {
            OAuthServer.logger.log(Level.ERROR, e.toString());
        }
    }

    /*
    Load config from file
     */
    public static void load() {
        final File configFile = new File("./config.json");
        try {
            JSONObject jsonConfig = new JSONObject(Files.readString(configFile.toPath()));
            for (String key : jsonConfig.keySet()) {
                switch (key) {
                    case "PORT_SERVER" -> PORT_SERVER = jsonConfig.getInt(key);
                    case "PORT_API" -> PORT_API = jsonConfig.getInt(key);
                    case "SERVER_ID" -> SERVER_ID = jsonConfig.getString(key);
                    case "MOTD" -> MOTD = jsonConfig.getString(key);
                    case "SERVER_VERSION" -> SERVER_VERSION = jsonConfig.getString(key);
                    case "PLAYERS_MAX" -> PLAYERS_MAX = jsonConfig.getInt(key);
                    case "PLAYERS_NOW" -> PLAYERS_NOW = jsonConfig.getInt(key);
                    case "PROTOCOL_VERSION" -> PROTOCOL_VERSION = jsonConfig.getInt(key);
                }
            }
        } catch (Exception e) {
            OAuthServer.logger.log(Level.WARN, e.toString());
            save();
        }
    }
}