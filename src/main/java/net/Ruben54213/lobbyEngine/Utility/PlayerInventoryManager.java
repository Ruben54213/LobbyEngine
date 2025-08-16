package net.Ruben54213.lobbyEngine.Utility;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Verwaltet Spieler-Inventare: Verhindert Item-Drops und stellt Inventare nach dem Tod wieder her
 */
public class PlayerInventoryManager implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, ItemStack[]> savedInventories = new HashMap<>();
    private final Map<UUID, ItemStack[]> savedArmor = new HashMap<>();
    private final CompassManager compassManager;
    private final CosmeticsManager cosmeticsManager;
    private PlayerHeadManager playerHeadManager;
    private LobbyManager lobbyManager; // LobbyManager hinzugefügt

    public PlayerInventoryManager(JavaPlugin plugin, CompassManager compassManager, CosmeticsManager cosmeticsManager) {
        this.plugin = plugin;
        this.compassManager = compassManager;
        this.cosmeticsManager = cosmeticsManager;
        // PlayerHeadManager und LobbyManager werden später gesetzt
        this.playerHeadManager = null;
        this.lobbyManager = null;
    }

    /**
     * Setzt den PlayerHeadManager (wird nach Initialisierung aufgerufen)
     */
    public void setPlayerHeadManager(PlayerHeadManager playerHeadManager) {
        this.playerHeadManager = playerHeadManager;
    }

    /**
     * Setzt den LobbyManager (wird nach Initialisierung aufgerufen)
     */
    public void setLobbyManager(LobbyManager lobbyManager) {
        this.lobbyManager = lobbyManager;
    }

    /**
     * Speichert das Inventar eines Spielers
     */
    public void savePlayerInventory(Player player) {
        UUID uuid = player.getUniqueId();

        // Inventar-Inhalt speichern
        ItemStack[] inventory = new ItemStack[player.getInventory().getSize()];
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            inventory[i] = item != null ? item.clone() : null;
        }
        savedInventories.put(uuid, inventory);

        // Rüstung speichern
        ItemStack[] armor = new ItemStack[4];
        armor[0] = player.getInventory().getHelmet() != null ? player.getInventory().getHelmet().clone() : null;
        armor[1] = player.getInventory().getChestplate() != null ? player.getInventory().getChestplate().clone() : null;
        armor[2] = player.getInventory().getLeggings() != null ? player.getInventory().getLeggings().clone() : null;
        armor[3] = player.getInventory().getBoots() != null ? player.getInventory().getBoots().clone() : null;
        savedArmor.put(uuid, armor);

        plugin.getLogger().info("Saved inventory for player: " + player.getName());
    }

    /**
     * Stellt das Inventar eines Spielers wieder her
     */
    public void restorePlayerInventory(Player player) {
        UUID uuid = player.getUniqueId();

        // Inventar wiederherstellen
        if (savedInventories.containsKey(uuid)) {
            ItemStack[] inventory = savedInventories.get(uuid);
            player.getInventory().clear();

            for (int i = 0; i < inventory.length && i < player.getInventory().getSize(); i++) {
                if (inventory[i] != null) {
                    player.getInventory().setItem(i, inventory[i].clone());
                }
            }
        }

        // Rüstung wiederherstellen
        if (savedArmor.containsKey(uuid)) {
            ItemStack[] armor = savedArmor.get(uuid);
            player.getInventory().setHelmet(armor[0] != null ? armor[0].clone() : null);
            player.getInventory().setChestplate(armor[1] != null ? armor[1].clone() : null);
            player.getInventory().setLeggings(armor[2] != null ? armor[2].clone() : null);
            player.getInventory().setBoots(armor[3] != null ? armor[3].clone() : null);
        }

        player.updateInventory();
        plugin.getLogger().info("Restored inventory for player: " + player.getName());
    }

    /**
     * Gibt einem Spieler die Standard-Lobby-Items
     */
    public void giveLobbyItems(Player player) {
        // Inventar leeren
        player.getInventory().clear();

        // Standard-Items geben
        compassManager.giveNavigatorCompass(player);  // Slot 0

        // LOBBY-ITEM in Slot 1 geben
        if (lobbyManager != null) {
            lobbyManager.giveLobbyItem(player); // Slot 1
        }

        cosmeticsManager.giveCosmeticsItem(player);   // Slot 4

        // Friends-Kopf geben (Slot 8)
        if (playerHeadManager != null) {
            playerHeadManager.giveFriendsHead(player); // Slot 8
        }

        // Inventar speichern nach Items geben
        savePlayerInventory(player);

        player.updateInventory();
        plugin.getLogger().info("Gave lobby items to player: " + player.getName());
    }

    /**
     * Prüft ob ein Spieler ein gespeichertes Inventar hat
     */
    public boolean hasStoredInventory(Player player) {
        return savedInventories.containsKey(player.getUniqueId());
    }

    // ==================== EVENT HANDLERS ====================

    /**
     * Gibt neuen Spielern Lobby-Items und speichert sie
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Nach kurzer Verzögerung Items geben
        new BukkitRunnable() {
            @Override
            public void run() {
                giveLobbyItems(player);
            }
        }.runTaskLater(plugin, 10L); // 0.5 Sekunden Verzögerung
    }

    /**
     * Verhindert Item-Drops beim Tod und speichert Inventar
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity().getPlayer();

        // Inventar vor dem Tod speichern (falls es sich geändert hat)
        savePlayerInventory(player);

        // Alle Drops verhindern
        event.getDrops().clear();
        event.setDroppedExp(0);

        // Keep Inventory aktivieren
        event.setKeepInventory(true);
        event.setKeepLevel(true);

        plugin.getLogger().info("Prevented item drops for player: " + player.getName());
    }

    /**
     * Stellt Inventar nach Respawn wieder her
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        // Nach kurzer Verzögerung Inventar wiederherstellen
        new BukkitRunnable() {
            @Override
            public void run() {
                restorePlayerInventory(player);

                // Gesundheit und Hunger wiederherstellen
                player.setHealth(player.getMaxHealth());
                player.setFoodLevel(20);
                player.setSaturation(20.0f);
            }
        }.runTaskLater(plugin, 3L); // 0.15 Sekunden Verzögerung
    }

    // ==================== UTILITY METHODEN ====================

    /**
     * Bereinigt Spieler-Daten beim Disconnect
     */
    public void cleanupPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        savedInventories.remove(uuid);
        savedArmor.remove(uuid);
        plugin.getLogger().info("Cleaned up inventory data for player: " + player.getName());
    }

    /**
     * Aktualisiert das gespeicherte Inventar eines Spielers
     */
    public void updateStoredInventory(Player player) {
        savePlayerInventory(player);
    }

    /**
     * Löscht alle gespeicherten Inventare (für Plugin-Shutdown)
     */
    public void clearAllStoredInventories() {
        savedInventories.clear();
        savedArmor.clear();
        plugin.getLogger().info("Cleared all stored inventories");
    }

    /**
     * Gibt die Anzahl gespeicherter Inventare zurück
     */
    public int getStoredInventoryCount() {
        return savedInventories.size();
    }

    /**
     * Gibt einem Spieler manuell Lobby-Items (für Commands)
     */
    public void giveItemsCommand(Player player) {
        giveLobbyItems(player);
        player.sendMessage("§aLobby items have been restored!");
    }

    /**
     * Registriert diesen Listener beim Plugin
     */
    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("PlayerInventoryManager registered!");
    }
}