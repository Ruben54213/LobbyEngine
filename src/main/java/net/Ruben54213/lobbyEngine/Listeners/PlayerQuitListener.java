package net.Ruben54213.lobbyEngine.Listeners;

import net.Ruben54213.lobbyEngine.Utility.CosmeticsFeatures;
import net.Ruben54213.lobbyEngine.Utility.LobbyProtectionManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Behandelt Player-Quit Events für Cleanup
 */
public class PlayerQuitListener implements Listener {

    private final JavaPlugin plugin;
    private final CosmeticsFeatures cosmeticsFeatures;
    private final LobbyProtectionManager protectionManager;

    public PlayerQuitListener(JavaPlugin plugin, CosmeticsFeatures cosmeticsFeatures, LobbyProtectionManager protectionManager) {
        this.plugin = plugin;
        this.cosmeticsFeatures = cosmeticsFeatures;
        this.protectionManager = protectionManager;
    }

    /**
     * Bereinigt Spieler-Daten beim Verlassen
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Alle Cosmetics-Features bereinigen
        cosmeticsFeatures.cleanupPlayer(player);
        protectionManager.cleanupPlayer(player);

        plugin.getLogger().info("Cleaned up cosmetics data for: " + player.getName());
    }

    /**
     * Registriert diesen Listener beim Plugin
     */
    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
}