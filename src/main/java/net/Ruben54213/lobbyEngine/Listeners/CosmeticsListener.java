package net.Ruben54213.lobbyEngine.Listeners;

import net.Ruben54213.lobbyEngine.Utility.CosmeticsGUI;
import net.Ruben54213.lobbyEngine.Utility.CosmeticsManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Behandelt Interaktionen mit dem Cosmetics-Item
 */
public class CosmeticsListener implements Listener {

    private final JavaPlugin plugin;
    private final CosmeticsManager cosmeticsManager;
    private final CosmeticsGUI cosmeticsGUI;

    public CosmeticsListener(JavaPlugin plugin, CosmeticsManager cosmeticsManager, CosmeticsGUI cosmeticsGUI) {
        this.plugin = plugin;
        this.cosmeticsManager = cosmeticsManager;
        this.cosmeticsGUI = cosmeticsGUI;
    }

    /**
     * Behandelt Rechtsklick auf Cosmetics-Item
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Prüfen ob Rechtsklick
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // Prüfen ob es das Cosmetics-Item ist
        if (!cosmeticsManager.isCosmeticsItem(item)) {
            return;
        }

        // Event abbrechen um Block-Interaktion zu verhindern
        event.setCancelled(true);

        // Cosmetics-GUI öffnen
        cosmeticsGUI.openMainCosmeticsGUI(player);
    }

    /**
     * Registriert diesen Listener beim Plugin
     */
    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
}