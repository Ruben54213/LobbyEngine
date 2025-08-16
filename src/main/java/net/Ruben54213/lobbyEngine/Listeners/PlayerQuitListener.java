package net.Ruben54213.lobbyEngine.Listeners;

import net.Ruben54213.lobbyEngine.Utility.CosmeticsFeatures;
import net.Ruben54213.lobbyEngine.Utility.LobbyProtectionManager;
import net.Ruben54213.lobbyEngine.Utility.PlayerInvulnerabilityManager;
import net.Ruben54213.lobbyEngine.Utility.PlayerInventoryManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Behandelt Player-Quit Events für Cleanup aller Manager
 */
public class PlayerQuitListener implements Listener {

    private final JavaPlugin plugin;
    private final CosmeticsFeatures cosmeticsFeatures;
    private final LobbyProtectionManager protectionManager;
    private final PlayerInvulnerabilityManager invulnerabilityManager;
    private final PlayerInventoryManager inventoryManager;

    public PlayerQuitListener(JavaPlugin plugin, CosmeticsFeatures cosmeticsFeatures,
                              LobbyProtectionManager protectionManager,
                              PlayerInvulnerabilityManager invulnerabilityManager,
                              PlayerInventoryManager inventoryManager) {
        this.plugin = plugin;
        this.cosmeticsFeatures = cosmeticsFeatures;
        this.protectionManager = protectionManager;
        this.invulnerabilityManager = invulnerabilityManager;
        this.inventoryManager = inventoryManager;
    }

    /**
     * Bereinigt alle Spieler-Daten beim Verlassen
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Alle Manager bereinigen
        cosmeticsFeatures.cleanupPlayer(player);
        protectionManager.cleanupPlayer(player);
        invulnerabilityManager.cleanupPlayer(player);
        inventoryManager.cleanupPlayer(player);

        plugin.getLogger().info("Cleaned up all data for player: " + player.getName());
    }

    /**
     * Registriert diesen Listener beim Plugin
     */
    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
}