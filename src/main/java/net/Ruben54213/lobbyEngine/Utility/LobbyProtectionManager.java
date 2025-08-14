package net.Ruben54213.lobbyEngine.Utility;

import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Verwaltet Lobby-Schutz: Pfeile für Love Bow, PvP-Schutz, Falldamage-Schutz
 */
public class LobbyProtectionManager implements Listener {

    private final JavaPlugin plugin;
    private final Set<UUID> playersWithLoveBow = new HashSet<>();

    // Arrow Item für Love Bow
    private static final ItemStack LOVE_ARROW = createLoveArrow();

    public LobbyProtectionManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // ==================== PFEIL MANAGEMENT ====================

    /**
     * Erstellt den speziellen Love Arrow
     */
    private static ItemStack createLoveArrow() {
        ItemStack arrow = new ItemStack(Material.ARROW, 1);
        ItemMeta meta = arrow.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&d&lLove Arrow"));
            arrow.setItemMeta(meta);
        }
        return arrow;
    }

    /**
     * Gibt einem Spieler den Love Arrow (falls er einen Love Bow hat)
     */
    public void giveLoveArrowIfNeeded(Player player) {
        // Prüfe ob Spieler Love Bow hat
        if (hasLoveBow(player)) {
            // Stelle sicher, dass ein Love Arrow im ersten Slot ist
            ItemStack firstSlot = player.getInventory().getItem(0);

            if (firstSlot == null || !isLoveArrow(firstSlot)) {
                player.getInventory().setItem(0, LOVE_ARROW.clone());
            }

            playersWithLoveBow.add(player.getUniqueId());
        }
    }

    /**
     * Entfernt Love Arrow vom Spieler
     */
    public void removeLoveArrow(Player player) {
        playersWithLoveBow.remove(player.getUniqueId());

        // Entferne Love Arrow aus Inventar
        ItemStack firstSlot = player.getInventory().getItem(0);
        if (firstSlot != null && isLoveArrow(firstSlot)) {
            player.getInventory().setItem(0, null);
        }
    }

    /**
     * Prüft ob Spieler einen Love Bow hat
     */
    private boolean hasLoveBow(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.hasItemMeta()) {
                String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
                if ("Love Bow".equals(displayName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Prüft ob Item ein Love Arrow ist
     */
    private boolean isLoveArrow(ItemStack item) {
        if (item == null || item.getType() != Material.ARROW) return false;
        if (!item.hasItemMeta()) return false;

        String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        return "Love Arrow".equals(displayName);
    }

    /**
     * Überwacht alle Spieler und stellt sicher, dass sie Love Arrows haben
     */
    public void startArrowMonitoring() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (playersWithLoveBow.contains(player.getUniqueId())) {
                        // Prüfe ob Spieler noch Love Bow hat
                        if (hasLoveBow(player)) {
                            giveLoveArrowIfNeeded(player);
                        } else {
                            removeLoveArrow(player);
                        }
                    } else if (hasLoveBow(player)) {
                        // Neuer Love Bow erkannt
                        giveLoveArrowIfNeeded(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Jede Sekunde prüfen
    }

    // ==================== EVENT HANDLERS ====================

    /**
     * Behandelt Spieler-Beitritt
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Nach kurzer Verzögerung Arrows geben (damit Inventar geladen ist)
        new BukkitRunnable() {
            @Override
            public void run() {
                giveLoveArrowIfNeeded(player);
            }
        }.runTaskLater(plugin, 5L);
    }

    /**
     * Behandelt Spieler-Respawn
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        // Nach Respawn Arrows wiedergeben
        new BukkitRunnable() {
            @Override
            public void run() {
                giveLoveArrowIfNeeded(player);
            }
        }.runTaskLater(plugin, 5L);
    }

    /**
     * Behandelt Bow-Schuss und füllt Arrows wieder auf
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBowShoot(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            ItemStack bow = event.getBow();

            if (bow != null && bow.hasItemMeta()) {
                String displayName = ChatColor.stripColor(bow.getItemMeta().getDisplayName());
                if ("Love Bow".equals(displayName)) {
                    // Nach dem Schuss einen neuen Arrow geben
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            giveLoveArrowIfNeeded(player);
                        }
                    }.runTaskLater(plugin, 1L);
                }
            }
        }
    }

    /**
     * Verhindert das Verschieben von Love Arrows
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            ItemStack item = event.getCurrentItem();

            if (item != null && isLoveArrow(item)) {
                // Love Arrows können nicht verschoben werden
                event.setCancelled(true);
            }
        }
    }

    // ==================== SCHUTZ SYSTEME ====================

    /**
     * Verhindert PvP-Schaden zwischen Spielern
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Spieler gegen Spieler Schaden verhindern
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            event.setCancelled(true);
            return;
        }

        // Pfeil-Schaden von Spielern verhindern
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();
            if (arrow.getShooter() instanceof Player) {
                event.setCancelled(true);
                return;
            }
        }

        // Alle anderen Projektile von Spielern verhindern
        if (event.getEntity() instanceof Player && event.getDamager().getType() == EntityType.ARROW) {
            event.setCancelled(true);
        }
    }

    /**
     * Verhindert Falldamage und anderen Umweltschaden
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            EntityDamageEvent.DamageCause cause = event.getCause();

            switch (cause) {
                case FALL:
                case LAVA:
                case FIRE:
                case FIRE_TICK:
                case DROWNING:
                case SUFFOCATION:
                case STARVATION:
                case POISON:
                case WITHER:
                case LIGHTNING:
                case FREEZE:
                case HOT_FLOOR:
                    event.setCancelled(true);
                    break;
                default:
                    break;
            }
        }
    }

    // ==================== UTILITY METHODEN ====================

    /**
     * Bereinigt Spieler-Daten beim Disconnect
     */
    public void cleanupPlayer(Player player) {
        playersWithLoveBow.remove(player.getUniqueId());
    }

    /**
     * Überprüft Inventar aller Online-Spieler (für externe Aufrufe)
     */
    public void checkAllPlayersInventory() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            giveLoveArrowIfNeeded(player);
        }
    }

    /**
     * Aktiviert Schutz für einen Spieler manuell
     */
    public void enableProtectionForPlayer(Player player) {
        giveLoveArrowIfNeeded(player);
    }

    /**
     * Registriert diesen Listener beim Plugin
     */
    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        startArrowMonitoring();
    }
}