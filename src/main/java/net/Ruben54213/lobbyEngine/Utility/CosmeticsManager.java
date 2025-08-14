package net.Ruben54213.lobbyEngine.Utility;

import net.Ruben54213.lobbyEngine.Listeners.InventoryProtListener;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Verwaltet das Cosmetics-Item für Spieler
 */
public class CosmeticsManager {

    private final JavaPlugin plugin;

    public CosmeticsManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Gibt einem Spieler das Cosmetics-Item
     */
    public void giveCosmeticsItem(Player player) {
        ItemStack cosmetics = createCosmeticsItem();

        // Cosmetics in Slot 4 setzen (5. Slot der Hotbar)
        player.getInventory().setItem(4, cosmetics);
    }

    /**
     * Erstellt das Cosmetics-Item
     */
    private ItemStack createCosmeticsItem() {
        ItemStack cosmetics = new ItemStack(Material.CHEST);
        ItemMeta meta = cosmetics.getItemMeta();

        if (meta != null) {
            // Name mit Farbcode
            meta.setDisplayName(translateColorCodes("&c&lCosmetics"));

            // Lore hinzufügen
            meta.setLore(List.of(
                    translateColorCodes("&7Right-click to open cosmetics!")
            ));

            cosmetics.setItemMeta(meta);
        }

        // Als Lobby-Item markieren damit es nicht verschoben werden kann
        return cosmetics;
    }

    /**
     * Prüft ob ein Item das Cosmetics-Item ist
     */
    public boolean isCosmeticsItem(ItemStack item) {
        if (item == null || item.getType() != Material.CHEST) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }

        String displayName = ChatColor.stripColor(meta.getDisplayName());
        return displayName.equals("Cosmetics");
    }

    /**
     * Wandelt & Farbcodes in § Farbcodes um
     */
    private String translateColorCodes(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}