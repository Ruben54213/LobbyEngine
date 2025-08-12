package net.Ruben54213.lobbyEngine.Listeners;

import net.Ruben54213.lobbyEngine.Utility.SpawnManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Behandelt Player-Join Events für Spawn-Teleportation
 */
public class PlayerJoinListener implements Listener {

    private final JavaPlugin plugin;
    private final SpawnManager spawnManager;

    public PlayerJoinListener(JavaPlugin plugin, SpawnManager spawnManager) {
        this.plugin = plugin;
        this.spawnManager = spawnManager;
    }

    /**
     * Teleportiert Spieler beim Join zum Spawn mit geilen Effekten
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Prüfen ob Spawn gesetzt ist und Auto-Teleport aktiviert ist
        if (!spawnManager.hasSpawn()) {
            return;
        }

        if (!plugin.getConfig().getBoolean("spawn.teleport-on-join", true)) {
            return;
        }

        // Kurze Verzögerung für bessere Effekte (1 Tick nach Join)
        new BukkitRunnable() {
            @Override
            public void run() {
                // Zum Spawn teleportieren mit KRASSEN EFFEKTEN!
                if (spawnManager.teleportToSpawn(player)) {

                    // Zusätzliche Join-Effekte nach weiterer kurzer Verzögerung
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            // Zweite Welle von Partikeln für epischen Effekt
                            spawnManager.playSpawnEffects(player);
                        }
                    }.runTaskLater(plugin, 10L); // 0.5 Sekunden später
                }
            }
        }.runTaskLater(plugin, 1L); // 1 Tick Verzögerung
    }

    /**
     * Registriert diesen Listener beim Plugin
     */
    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
}