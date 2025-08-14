package net.Ruben54213.lobbyEngine.Listeners;

import net.Ruben54213.lobbyEngine.Utility.CompassManager;
import net.Ruben54213.lobbyEngine.Utility.CosmeticsManager;
import net.Ruben54213.lobbyEngine.Utility.SpawnManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * Behandelt Player-Join Events für Spawn-Teleportation
 */
public class PlayerJoinListener implements Listener {

    private final JavaPlugin plugin;
    private final SpawnManager spawnManager;
    private final CompassManager compassManager;
    private final CosmeticsManager cosmeticsManager;

    public PlayerJoinListener(JavaPlugin plugin, SpawnManager spawnManager, CompassManager compassManager, CosmeticsManager cosmeticsManager) {
        this.plugin = plugin;
        this.spawnManager = spawnManager;
        this.compassManager = compassManager;
        this.cosmeticsManager = cosmeticsManager;
    }

    /**
     * Teleportiert Spieler beim Join zum Spawn mit geilen Effekten
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // WICHTIG: Debug-Nachricht um zu sehen ob Event überhaupt ausgelöst wird
        plugin.getLogger().info("PlayerJoinEvent triggered for: " + player.getName());

        // Navigator-Kompass und Cosmetics-Item geben
        giveLobbyItems(player);

        // Willkommensnachricht senden
        sendWelcomeMessage(player);

        // Prüfen ob Spawn gesetzt ist
        if (!spawnManager.hasSpawn()) {
            plugin.getLogger().info("No spawn location set - skipping teleportation");
            return;
        }

        // Prüfen ob Auto-Teleport aktiviert ist
        if (!plugin.getConfig().getBoolean("spawn.teleport-on-join", true)) {
            plugin.getLogger().info("Auto-teleport disabled in config - skipping teleportation");
            return;
        }

        plugin.getLogger().info("Starting teleportation process for: " + player.getName());

        // Kurze Verzögerung für bessere Effekte (1 Tick nach Join)
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getLogger().info("Executing teleportation for: " + player.getName());

                // Zum Spawn teleportieren mit KRASSEN EFFEKTEN!
                if (spawnManager.teleportToSpawn(player)) {
                    plugin.getLogger().info("Successfully teleported " + player.getName() + " to spawn!");

                    // Zusätzliche Join-Effekte nach weiterer kurzer Verzögerung
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            // Zweite Welle von Partikeln für epischen Effekt
                            spawnManager.playSpawnEffects(player);
                        }
                    }.runTaskLater(plugin, 10L); // 0.5 Sekunden später
                } else {
                    plugin.getLogger().warning("Failed to teleport " + player.getName() + " to spawn!");
                }
            }
        }.runTaskLater(plugin, 1L); // 1 Tick Verzögerung
    }

    /**
     * Gibt dem Spieler alle Lobby-Items (Navigator + Cosmetics)
     */
    private void giveLobbyItems(Player player) {
        // Verzögerung damit Inventar richtig geladen ist
        new BukkitRunnable() {
            @Override
            public void run() {
                // Navigator-Kompass (Slot 0)
                compassManager.giveNavigatorCompass(player);
                plugin.getLogger().info("Navigator compass given to " + player.getName());

                // Cosmetics-Item (Slot 4)
                cosmeticsManager.giveCosmeticsItem(player);
                plugin.getLogger().info("Cosmetics item given to " + player.getName());
            }
        }.runTaskLater(plugin, 5L); // 0.25 Sekunden
    }

    /**
     * Sendet eine mehrzeilige Willkommensnachricht an den Spieler
     */
    private void sendWelcomeMessage(Player player) {
        plugin.getLogger().info("Checking welcome message settings...");

        // Prüfen ob Willkommensnachrichten aktiviert sind
        if (!plugin.getConfig().getBoolean("messages.welcome.enabled", true)) {
            plugin.getLogger().info("Welcome messages are disabled in config");
            return;
        }

        // Nachrichtenliste aus Config laden
        List<String> welcomeMessages = plugin.getConfig().getStringList("messages.welcome.messages");

        // Wenn keine Nachrichten konfiguriert sind, Standard verwenden
        if (welcomeMessages.isEmpty()) {
            plugin.getLogger().info("No welcome messages in config, using defaults");
            welcomeMessages = getDefaultWelcomeMessages();
        }

        plugin.getLogger().info("Sending " + welcomeMessages.size() + " welcome messages to " + player.getName());

        // Verzögerung für bessere Darstellung (nach Teleportation)
        List<String> finalWelcomeMessages = welcomeMessages;
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getLogger().info("Executing welcome message sending for: " + player.getName());

                // Jede Zeile der Willkommensnachricht senden
                for (String message : finalWelcomeMessages) {
                    // Platzhalter ersetzen
                    String formattedMessage = message
                            .replace("%player%", player.getName())
                            .replace("%displayname%", player.getDisplayName())
                            .replace("%server%", plugin.getServer().getName())
                            .replace("%online%", String.valueOf(plugin.getServer().getOnlinePlayers().size()))
                            .replace("%max%", String.valueOf(plugin.getServer().getMaxPlayers()));

                    // Farbcodes übersetzen und senden
                    player.sendMessage(translateColorCodes(formattedMessage));
                }

                plugin.getLogger().info("Welcome messages sent to " + player.getName());
            }
        }.runTaskLater(plugin, 20L); // 1 Sekunde Verzögerung
    }

    /**
     * Standard-Willkommensnachrichten falls keine in Config definiert
     */
    private List<String> getDefaultWelcomeMessages() {
        return List.of(
                "",
                "&e&l◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆",
                "&6&l                    Welcome to our Server!",
                "",
                "&f                      Hello &b%player%&f!",
                "&f                   We hope you enjoy your stay!",
                "",
                "&7                    Online Players: &a%online%&7/&a%max%",
                "",
                "&e&l◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆ ◆",
                ""
        );
    }

    /**
     * Wandelt & Farbcodes in § Farbcodes um
     */
    private String translateColorCodes(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Registriert diesen Listener beim Plugin
     */
    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
}