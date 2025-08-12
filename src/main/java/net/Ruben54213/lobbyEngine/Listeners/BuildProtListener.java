package net.Ruben54213.lobbyEngine.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Verhindert das Platzieren und Abbauen von Blöcken für Spieler ohne lobbyengine.build Permission
 * im Creative Modus (GM 1)
 */
public class BuildProtListener implements Listener {

    private final JavaPlugin plugin;
    private static final String BUILD_PERMISSION = "lobbyengine.build";

    public BuildProtListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Verhindert das Platzieren von Blöcken für Spieler ohne Permission im Creative Modus
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        // Nur im Creative Modus (GM 1) prüfen
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        // Permission prüfen
        if (!player.hasPermission(BUILD_PERMISSION)) {
            event.setCancelled(true);

            // Nachricht aus config.yml senden, wenn aktiviert
            if (plugin.getConfig().getBoolean("messages.buildprot.feedback", true)) {
                String placeMessage = plugin.getConfig().getString("messages.buildprot.place", "");
                if (!placeMessage.isEmpty()) {
                    String prefix = plugin.getConfig().getString("prefix", "");
                    String fullMessage = translateColorCodes(prefix + placeMessage);
                    player.sendMessage(fullMessage);

                }
            }
        }
    }

    /**
     * Verhindert das Abbauen von Blöcken für Spieler ohne Permission im Creative Modus
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        // Nur im Creative Modus (GM 1) prüfen
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        // Permission prüfen
        if (!player.hasPermission(BUILD_PERMISSION)) {
            event.setCancelled(true);

            // Nachricht aus config.yml senden, wenn aktiviert
            if (plugin.getConfig().getBoolean("messages.buildprot.feedback", true)) {
                String breakMessage = plugin.getConfig().getString("messages.buildprot.break", "");
                if (!breakMessage.isEmpty()) {
                    String prefix = plugin.getConfig().getString("prefix", "");
                    String fullMessage = translateColorCodes(prefix + breakMessage);
                    player.sendMessage(fullMessage);

                }
            }
        }
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

    /**
     * Prüft ob ein Spieler die Build-Permission hat
     * Utility-Methode für andere Teile des Systems
     */
    public static boolean hasBuildPermission(Player player) {
        return player.hasPermission(BUILD_PERMISSION);
    }
}
