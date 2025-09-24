package org.bukkit.plugin.java;

import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;

public abstract class JavaPlugin {
    private Logger logger = Logger.getLogger("MinecraftPlugin");
    private File dataFolder = new File("plugins/MiyotuGame");
    private PluginDescriptionFile description = new PluginDescriptionFile();
    private FileConfiguration config = new FileConfiguration() {};
    private Server server = new Server() {};
    
    public abstract void onEnable();
    public abstract void onDisable();
    
    public Logger getLogger() { return logger; }
    public File getDataFolder() { return dataFolder; }
    public PluginDescriptionFile getDescription() { return description; }
    public FileConfiguration getConfig() { return config; }
    public Server getServer() { return server; }
    
    public void saveDefaultConfig() {}
    public void reloadConfig() {}
    public InputStream getResource(String filename) { return null; }
    public void saveResource(String resourcePath, boolean replace) {}
}