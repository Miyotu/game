package com.miyotu.cs2plugin.managers;

import com.miyotu.cs2plugin.CS2Plugin;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class NPCManager {
    private final CS2Plugin plugin;
    private final Map<String, NPC> npcs;
    private NPCRegistry registry;
    
    public NPCManager(CS2Plugin plugin) {
        this.plugin = plugin;
        this.npcs = new HashMap<>();
        
        if (plugin.getServer().getPluginManager().getPlugin("Citizens") != null) {
            this.registry = CitizensAPI.createAnonymousNPCRegistry();
        } else {
            plugin.getLogger().warning("Citizens plugin not found! NPC functionality will be disabled.");
        }
    }
    
    public boolean createJoinNPC(Location location) {
        if (registry == null) {
            return false;
        }
        
        String npcName = plugin.getConfig().getString("npc.name", "&6Join Game");
        NPC npc = registry.createNPC(EntityType.PLAYER, npcName);
        
        // Set skin if specified
        String skin = plugin.getConfig().getString("npc.skin", "Notch");
        if (!skin.isEmpty()) {
            npc.data().setPersistent("player-skin-name", skin);
        }
        
        npc.spawn(location);
        npcs.put("join_game", npc);
        
        return true;
    }
    
    public void handleNPCClick(NPC npc, Player player) {
        if (npcs.containsValue(npc)) {
            // Handle join game NPC click
            if (npcs.get("join_game").equals(npc)) {
                handleJoinGameClick(player);
            }
        }
    }
    
    private void handleJoinGameClick(Player player) {
        // Open lobby selection GUI or auto-join
        LobbyManager lobbyManager = plugin.getLobbyManager();
        
        // Find available lobby
        for (var lobby : lobbyManager.getAllLobbies().values()) {
            if (!lobby.isFull() && !lobby.isGameStarted()) {
                // Try to join this lobby
                plugin.getServer().dispatchCommand(player, "cs2 join " + lobby.getName());
                return;
            }
        }
        
        // No available lobbies
        String message = plugin.getConfig().getString("messages.prefix", "&8[&6CS2&8]&r ") +
                        "&cNo available lobbies at the moment!";
        player.sendMessage(message.replace("&", "§"));
    }
    
    public void cleanup() {
        if (registry != null) {
            for (NPC npc : npcs.values()) {
                if (npc.isSpawned()) {
                    npc.despawn();
                }
                npc.destroy();
            }
            npcs.clear();
        }
    }
    
    public NPC getNPC(String key) {
        return npcs.get(key);
    }
}