package com.miyotu.cs2plugin.gui;

import com.miyotu.cs2plugin.CS2Plugin;
import com.miyotu.cs2plugin.models.CS2Player;
import com.miyotu.cs2plugin.models.Lobby;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class LobbyGUI {
    private final CS2Plugin plugin;
    
    public LobbyGUI(CS2Plugin plugin) {
        this.plugin = plugin;
    }
    
    public void open(Player player) {
        String title = plugin.getConfig().getString("gui.lobby-selector.title", "&6Select Lobby")
                .replace("&", "§");
        int size = plugin.getConfig().getInt("gui.lobby-selector.size", 54);
        
        Inventory gui = Bukkit.createInventory(null, size, title);
        
        var lobbies = plugin.getLobbyManager().getAllLobbies();
        int slot = 0;
        
        for (Lobby lobby : lobbies.values()) {
            if (slot >= size - 9) break; // Leave space for navigation
            
            ItemStack item = createLobbyItem(lobby);
            gui.setItem(slot, item);
            slot++;
        }
        
        // Add close button
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName("§cClose");
        closeItem.setItemMeta(closeMeta);
        gui.setItem(size - 1, closeItem);
        
        player.openInventory(gui);
    }
    
    private ItemStack createLobbyItem(Lobby lobby) {
        Material material = lobby.isFull() ? Material.RED_CONCRETE : 
                           lobby.isGameStarted() ? Material.YELLOW_CONCRETE : Material.GREEN_CONCRETE;
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§6" + lobby.getName());
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Players: §f" + lobby.getCurrentPlayerCount() + "/" + lobby.getMaxPlayers());
        
        if (lobby.isGameStarted()) {
            lore.add("§eStatus: §cIn Game");
        } else if (lobby.isFull()) {
            lore.add("§eStatus: §cFull");
        } else {
            lore.add("§eStatus: §aWaiting");
            lore.add("");
            lore.add("§aClick to join!");
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    public void handleClick(Player player, ItemStack clickedItem, String lobbyName) {
        if (clickedItem.getType() == Material.BARRIER) {
            player.closeInventory();
            return;
        }
        
        // Extract lobby name from item display name
        if (clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName()) {
            String displayName = clickedItem.getItemMeta().getDisplayName();
            String extractedName = displayName.replace("§6", "");
            
            Lobby lobby = plugin.getLobbyManager().getLobby(extractedName);
            if (lobby != null && !lobby.isFull() && !lobby.isGameStarted()) {
                CS2Player cs2Player = plugin.getPlayerDataManager().getPlayer(player);
                if (cs2Player != null && !cs2Player.isInLobby()) {
                    player.closeInventory();
                    player.performCommand("cs2 join " + extractedName);
                }
            }
        }
    }
}