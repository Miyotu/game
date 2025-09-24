package com.miyotu.cs2plugin.gui;

import com.miyotu.cs2plugin.CS2Plugin;
import com.miyotu.cs2plugin.enums.Team;
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

public class TeamGUI {
    private final CS2Plugin plugin;
    
    public TeamGUI(CS2Plugin plugin) {
        this.plugin = plugin;
    }
    
    public void open(Player player) {
        CS2Player cs2Player = plugin.getPlayerDataManager().getPlayer(player);
        if (cs2Player == null || !cs2Player.isInLobby()) {
            player.sendMessage("§cYou must be in a lobby to select a team!");
            return;
        }
        
        String title = plugin.getConfig().getString("gui.team-selector.title", "&6Select Team")
                .replace("&", "§");
        int size = plugin.getConfig().getInt("gui.team-selector.size", 27);
        
        Inventory gui = Bukkit.createInventory(null, size, title);
        
        Lobby lobby = cs2Player.getCurrentLobby();
        
        // CT Team item
        ItemStack ctItem = createTeamItem(Team.CT, lobby);
        gui.setItem(11, ctItem);
        
        // T Team item
        ItemStack tItem = createTeamItem(Team.T, lobby);
        gui.setItem(15, tItem);
        
        // Auto-balance item
        ItemStack autoItem = new ItemStack(Material.COMPASS);
        ItemMeta autoMeta = autoItem.getItemMeta();
        autoMeta.setDisplayName("§eAuto-Balance");
        List<String> autoLore = new ArrayList<>();
        autoLore.add("§7Automatically balance teams");
        autoLore.add("§aClick to auto-assign!");
        autoMeta.setLore(autoLore);
        autoItem.setItemMeta(autoMeta);
        gui.setItem(13, autoItem);
        
        // Close button
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName("§cClose");
        closeItem.setItemMeta(closeMeta);
        gui.setItem(size - 1, closeItem);
        
        player.openInventory(gui);
    }
    
    private ItemStack createTeamItem(Team team, Lobby lobby) {
        Material material = team == Team.CT ? Material.BLUE_CONCRETE : Material.RED_CONCRETE;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName(team.getColor() + team.getDisplayName());
        
        List<String> lore = new ArrayList<>();
        int teamSize = lobby.getTeamPlayers(team).size();
        lore.add("§7Players: §f" + teamSize + "/5");
        
        if (teamSize >= 5) {
            lore.add("§cTeam is full!");
        } else {
            lore.add("");
            lore.add("§aClick to join this team!");
        }
        
        // Show team members
        if (teamSize > 0) {
            lore.add("");
            lore.add("§eMembers:");
            for (var playerId : lobby.getTeamPlayers(team)) {
                var teamPlayer = lobby.getPlayers().get(playerId);
                if (teamPlayer != null) {
                    lore.add("§7- " + teamPlayer.getName());
                }
            }
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    public void handleClick(Player player, ItemStack clickedItem, int slot) {
        if (clickedItem.getType() == Material.BARRIER) {
            player.closeInventory();
            return;
        }
        
        CS2Player cs2Player = plugin.getPlayerDataManager().getPlayer(player);
        if (cs2Player == null || !cs2Player.isInLobby()) {
            player.closeInventory();
            return;
        }
        
        switch (slot) {
            case 11: // CT Team
                player.closeInventory();
                player.performCommand("cs2 team ct");
                break;
            case 15: // T Team
                player.closeInventory();
                player.performCommand("cs2 team t");
                break;
            case 13: // Auto-balance
                Lobby lobby = cs2Player.getCurrentLobby();
                lobby.autoBalanceTeams();
                player.closeInventory();
                player.sendMessage("§aTeams have been auto-balanced!");
                break;
        }
    }
}