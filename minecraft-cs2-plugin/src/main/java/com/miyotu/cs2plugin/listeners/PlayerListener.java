package com.miyotu.cs2plugin.listeners;

import com.miyotu.cs2plugin.CS2Plugin;
import com.miyotu.cs2plugin.gui.LobbyGUI;
import com.miyotu.cs2plugin.gui.TeamGUI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    private final CS2Plugin plugin;
    
    public PlayerListener(CS2Plugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Load player data
        plugin.getPlayerDataManager().loadPlayer(player);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Remove player from lobby if in one
        var cs2Player = plugin.getPlayerDataManager().getPlayer(player);
        if (cs2Player != null && cs2Player.isInLobby()) {
            cs2Player.getCurrentLobby().removePlayer(player.getUniqueId());
        }
        
        // Unload player data
        plugin.getPlayerDataManager().unloadPlayer(player.getUniqueId());
    }
    
    @EventHandler
    public void onNPCRightClick(NPCRightClickEvent event) {
        Player player = event.getClicker();
        
        // Handle NPC interactions through NPC manager
        plugin.getNPCManager().handleNPCClick(event.getNPC(), player);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        // Handle GUI clicks
        if (title.contains("Select Lobby")) {
            event.setCancelled(true);
            
            if (event.getCurrentItem() != null) {
                LobbyGUI gui = new LobbyGUI(plugin);
                gui.handleClick(player, event.getCurrentItem(), "");
            }
        } else if (title.contains("Select Team")) {
            event.setCancelled(true);
            
            if (event.getCurrentItem() != null) {
                TeamGUI gui = new TeamGUI(plugin);
                gui.handleClick(player, event.getCurrentItem(), event.getSlot());
            }
        }
    }
}