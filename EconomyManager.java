package com.miyotu.cs2plugin.managers;

import com.miyotu.cs2plugin.CS2Plugin;
import com.miyotu.cs2plugin.enums.Team;
import com.miyotu.cs2plugin.models.CS2Player;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class EconomyManager {
    private final CS2Plugin plugin;
    private final Map<Player, Integer> playerMoney = new HashMap<>();
    
    // Starting money and limits
    private static final int STARTING_MONEY = 800;
    private static final int MAX_MONEY = 16000;
    
    public EconomyManager(CS2Plugin plugin) {
        this.plugin = plugin;
    }
    
    public void setMoney(Player player, int amount) {
        playerMoney.put(player, Math.min(amount, MAX_MONEY));
        updateMoneyDisplay(player);
    }
    
    public int getMoney(Player player) {
        return playerMoney.getOrDefault(player, STARTING_MONEY);
    }
    
    public boolean hasMoney(Player player, int amount) {
        return getMoney(player) >= amount;
    }
    
    public boolean takeMoney(Player player, int amount) {
        int currentMoney = getMoney(player);
        if (currentMoney >= amount) {
            setMoney(player, currentMoney - amount);
            return true;
        }
        return false;
    }
    
    public void addMoney(Player player, int amount) {
        int currentMoney = getMoney(player);
        setMoney(player, currentMoney + amount);
    }
    
    private void updateMoneyDisplay(Player player) {
        int money = getMoney(player);
        String moneyText = String.format("§2$%,d", money);
        
        // Send action bar with money
        player.sendTitle("", moneyText, 0, 20, 10);
    }
    
    // Kill rewards
    public void awardKillMoney(Player killer, Player victim) {
        addMoney(killer, 300);
        killer.sendMessage("§a+$300 §7(Kill Reward)");
    }
    
    public void awardBombPlantMoney(Player planter) {
        addMoney(planter, 300);
        planter.sendMessage("§a+$300 §7(Bomb Plant)");
    }
    
    public void awardBombDefuseMoney(Player defuser) {
        addMoney(defuser, 300);
        defuser.sendMessage("§a+$300 §7(Bomb Defuse)");
    }
    
    // Weapon prices
    public static class WeaponPrices {
        public static final Map<String, Integer> PRICES = new HashMap<>();
        
        static {
            // Pistols
            PRICES.put("GLOCK", 200);
            PRICES.put("USP-S", 200);
            PRICES.put("P250", 300);
            PRICES.put("FIVE-SEVEN", 500);
            PRICES.put("TEC-9", 500);
            PRICES.put("CZ75-AUTO", 500);
            PRICES.put("DUAL_BERETTAS", 400);
            PRICES.put("P2000", 200);
            PRICES.put("DESERT_EAGLE", 700);
            PRICES.put("R8_REVOLVER", 600);
            
            // SMGs
            PRICES.put("MAC-10", 1050);
            PRICES.put("MP9", 1250);
            PRICES.put("MP7", 1500);
            PRICES.put("UMP-45", 1200);
            PRICES.put("P90", 2350);
            PRICES.put("PP-BIZON", 1400);
            PRICES.put("MP5-SD", 1500);
            
            // Rifles
            PRICES.put("FAMAS", 2050);
            PRICES.put("GALIL_AR", 1800);
            PRICES.put("AK-47", 2700);
            PRICES.put("M4A4", 3100);
            PRICES.put("M4A1-S", 2900);
            PRICES.put("SG_553", 3000);
            PRICES.put("AUG", 3300);
            
            // Snipers
            PRICES.put("SSG_08", 1700);
            PRICES.put("AWP", 4750);
            PRICES.put("G3SG1", 5000);
            PRICES.put("SCAR-20", 5000);
            
            // Shotguns
            PRICES.put("NOVA", 1050);
            PRICES.put("XM1014", 2000);
            PRICES.put("SAWED-OFF", 1100);
            PRICES.put("MAG-7", 1300);
            
            // Machine Guns
            PRICES.put("M249", 5200);
            PRICES.put("NEGEV", 1700);
            
            // Equipment
            PRICES.put("KEVLAR", 650);
            PRICES.put("KEVLAR_HELMET", 1000);
            PRICES.put("DEFUSE_KIT", 400);
            PRICES.put("ZEUS", 200);
            
            // Grenades
            PRICES.put("HE_GRENADE", 300);
            PRICES.put("FLASHBANG", 200);
            PRICES.put("SMOKE_GRENADE", 300);
            PRICES.put("INCENDIARY", 600);
            PRICES.put("MOLOTOV", 400);
            PRICES.put("DECOY", 50);
        }
    }
    
    public int getWeaponPrice(String weaponName) {
        return WeaponPrices.PRICES.getOrDefault(weaponName, 0);
    }
    
    public boolean canBuyWeapon(Player player, String weaponName) {
        CS2Player cs2Player = plugin.getPlayerManager().getCS2Player(player);
        if (cs2Player == null) return false;
        
        // Check if weapon is available for player's team
        if (!isWeaponAvailableForTeam(weaponName, cs2Player.getTeam())) {
            return false;
        }
        
        // Check if player has enough money
        int price = getWeaponPrice(weaponName);
        return hasMoney(player, price);
    }
    
    private boolean isWeaponAvailableForTeam(String weaponName, Team team) {
        // CT-only weapons
        String[] ctOnlyWeapons = {"USP-S", "P2000", "FIVE-SEVEN", "MP9", "M4A4", "M4A1-S", 
                                 "FAMAS", "AUG", "SCAR-20", "MAG-7", "DEFUSE_KIT"};
        
        // T-only weapons  
        String[] tOnlyWeapons = {"GLOCK", "TEC-9", "MAC-10", "AK-47", "GALIL_AR", 
                                "SG_553", "G3SG1", "SAWED-OFF", "MOLOTOV"};
        
        if (team == Team.CT) {
            return !Arrays.asList(tOnlyWeapons).contains(weaponName);
        } else if (team == Team.T) {
            return !Arrays.asList(ctOnlyWeapons).contains(weaponName);
        }
        
        return false; // Spectators can't buy
    }
    
    public ItemStack createShopItem(String weaponName, Team team) {
        Material material = getWeaponMaterial(weaponName);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§f" + weaponName.replace("_", " "));
            
            int price = getWeaponPrice(weaponName);
            boolean available = isWeaponAvailableForTeam(weaponName, team);
            
            if (available) {
                meta.setLore(Arrays.asList(
                    "§7Price: §a$" + price,
                    "§7Team: §b" + (isWeaponAvailableForTeam(weaponName, Team.CT) && 
                                   isWeaponAvailableForTeam(weaponName, Team.T) ? "Both" : team.name()),
                    "",
                    "§eClick to purchase!"
                ));
            } else {
                meta.setLore(Arrays.asList(
                    "§7Price: §c$" + price,
                    "§cNot available for your team!",
                    "",
                    "§cCannot purchase"
                ));
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    private Material getWeaponMaterial(String weaponName) {
        switch (weaponName) {
            // Pistols
            case "GLOCK":
            case "USP-S":
            case "P2000":
            case "P250":
            case "FIVE-SEVEN":
            case "TEC-9":
            case "CZ75-AUTO":
            case "DUAL_BERETTAS":
            case "DESERT_EAGLE":
            case "R8_REVOLVER":
                return Material.IRON_HOE;
                
            // SMGs
            case "MAC-10":
            case "MP9":
            case "MP7":
            case "UMP-45":
            case "P90":
            case "PP-BIZON":
            case "MP5-SD":
                return Material.GOLDEN_HOE;
                
            // Rifles
            case "FAMAS":
            case "GALIL_AR":
            case "AK-47":
            case "M4A4":
            case "M4A1-S":
            case "SG_553":
            case "AUG":
                return Material.DIAMOND_HOE;
                
            // Snipers
            case "SSG_08":
            case "AWP":
            case "G3SG1":
            case "SCAR-20":
                return Material.NETHERITE_HOE;
                
            // Shotguns
            case "NOVA":
            case "XM1014":
            case "SAWED-OFF":
            case "MAG-7":
                return Material.STONE_HOE;
                
            // Machine Guns
            case "M249":
            case "NEGEV":
                return Material.WOODEN_HOE;
                
            // Equipment
            case "KEVLAR":
            case "KEVLAR_HELMET":
                return Material.CHAINMAIL_CHESTPLATE;
            case "DEFUSE_KIT":
                return Material.SHEARS;
            case "ZEUS":
                return Material.BLAZE_ROD;
                
            // Grenades
            case "HE_GRENADE":
                return Material.TNT;
            case "FLASHBANG":
                return Material.GLOWSTONE_DUST;
            case "SMOKE_GRENADE":
                return Material.GRAY_DYE;
            case "INCENDIARY":
            case "MOLOTOV":
                return Material.FIRE_CHARGE;
            case "DECOY":
                return Material.PAPER;
                
            default:
                return Material.STICK;
        }
    }
    
    public void resetPlayerMoney(Player player) {
        playerMoney.remove(player);
        setMoney(player, STARTING_MONEY);
    }
    
    public void clearAllMoney() {
        playerMoney.clear();
    }
}