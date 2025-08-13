package net.Ruben54213.lobbyEngine.Listeners;

import net.Ruben54213.lobbyEngine.Utility.CompassManager;
import net.Ruben54213.lobbyEngine.Utility.ServerNavigatorGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Behandelt Interaktionen mit dem Navigator-Kompass
 */
public class NavigatorListener implements Listener {

    private final JavaPlugin plugin;
    private final CompassManager compassManager;
    private final ServerNavigatorGUI navigatorGUI;

    public NavigatorListener(JavaPlugin plugin, CompassManager compassManager, ServerNavigatorGUI navigatorGUI) {
        this.plugin = plugin;
        this.compassManager = compassManager;
        this.navigatorGUI = navigatorGUI;
    }

    /**
     * Behandelt Rechtsklick auf Navigator-Kompass
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Prüfen ob Rechtsklick
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // Prüfen ob es der Navigator-Kompass ist
        if (!compassManager.isNavigatorCompass(item)) {
            return;
        }

        // Event abbrechen um Block-Interaktion zu verhindern
        event.setCancelled(true);

        // Navigator-GUI öffnen
        navigatorGUI.openNavigator(player);
    }

    /**
     * Registriert diesen Listener beim Plugin
     */
    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
}