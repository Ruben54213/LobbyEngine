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
 * Verwaltet den Navigator-Kompass für Spieler
 */
public class CompassManager {

    private final JavaPlugin plugin;

    public CompassManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Gibt einem Spieler den Navigator-Kompass
     */
    public void giveNavigatorCompass(Player player) {
        ItemStack compass = createNavigatorCompass();

        // Kompass in Slot 0 setzen (erstes Slot der Hotbar)
        player.getInventory().setItem(0, compass);
    }

    /**
     * Erstellt den Navigator-Kompass
     */
    private ItemStack createNavigatorCompass() {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();

        if (meta != null) {
            // Name mit Farbcode
            meta.setDisplayName(translateColorCodes("&5&lNavigator"));

            // Lore hinzufügen
            meta.setLore(List.of(
                    translateColorCodes("&7Right-click to open the server selector!"),
                    translateColorCodes("&8Navigate between different servers")
            ));

            compass.setItemMeta(meta);
        }
        return compass;
    }

    /**
     * Prüft ob ein Item der Navigator-Kompass ist
     */
    public boolean isNavigatorCompass(ItemStack item) {
        if (item == null || item.getType() != Material.COMPASS) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }

        String displayName = ChatColor.stripColor(meta.getDisplayName());
        return displayName.equals("Navigator");
    }

    /**
     * Wandelt & Farbcodes in § Farbcodes um
     */
    private String translateColorCodes(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}