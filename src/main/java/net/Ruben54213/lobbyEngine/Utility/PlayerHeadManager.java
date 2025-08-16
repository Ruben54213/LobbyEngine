package net.Ruben54213.lobbyEngine.Utility;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Verwaltet den Spielerkopf im letzten Slot für das Friends-System
 */
public class PlayerHeadManager implements Listener {

    private final JavaPlugin plugin;

    public PlayerHeadManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Gibt einem Spieler seinen eigenen Kopf im letzten Slot
     */
    public void giveFriendsHead(Player player) {
        ItemStack playerHead = createPlayerHead(player);

        // Kopf in Slot 8 setzen (letzter Slot der Hotbar)
        player.getInventory().setItem(8, playerHead);

        plugin.getLogger().info("Friends head given to player: " + player.getName());
    }

    /**
     * Erstellt den Spielerkopf mit Friends-Design
     */
    private ItemStack createPlayerHead(Player player) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        if (meta != null) {
            // Spielerkopf setzen
            meta.setOwningPlayer(player);

            // Name aus Config oder Standard
            String headName = plugin.getConfig().getString("friends.head-name", "&a&lFriends &8&l| &7Rightclick");
            meta.setDisplayName(translateColorCodes(headName));

            // Lore aus Config oder Standard
            List<String> loreList = plugin.getConfig().getStringList("friends.head-lore");
            if (loreList.isEmpty()) {
                // Standard-Lore wenn nichts in Config
                loreList = List.of(
                        "&7Click to open friends menu!",
                        "",
                        "&8Your personal social hub"
                );
            }

            // Lore mit Farbcodes übersetzen
            List<String> translatedLore = loreList.stream()
                    .map(this::translateColorCodes)
                    .toList();

            meta.setLore(translatedLore);
            head.setItemMeta(meta);
        }

        return head;
    }

    /**
     * Prüft ob ein Item der Friends-Kopf ist
     */
    public boolean isFriendsHead(ItemStack item) {
        if (item == null || item.getType() != Material.PLAYER_HEAD) {
            return false;
        }

        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }

        // Hole den erwarteten Namen aus der Config
        String expectedName = translateColorCodes(plugin.getConfig().getString("friends.head-name", "&a&lFriends &8&l| &7Rightclick"));
        String actualName = meta.getDisplayName();

        // Vergleiche die Namen direkt (mit Farbcodes)
        return expectedName.equals(actualName);
    }

    /**
     * Entfernt den Friends-Kopf vom Spieler
     */
    public void removeFriendsHead(Player player) {
        // Slot 8 leeren (wo der Friends-Kopf ist)
        player.getInventory().setItem(8, null);
        plugin.getLogger().info("Friends head removed from player: " + player.getName());
    }

    // ==================== EVENT HANDLERS ====================

    /**
     * Behandelt Rechtsklick auf Friends-Kopf
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Prüfen ob Rechtsklick
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // Prüfen ob es der Friends-Kopf ist
        if (!isFriendsHead(item)) {
            return;
        }

        // Event abbrechen um Block-Interaktion zu verhindern
        event.setCancelled(true);

        // Command aus Config ausführen
        executeFriendsCommand(player);
    }

    /**
     * Führt den konfigurierten Friends-Command aus
     */
    private void executeFriendsCommand(Player player) {
        // Command aus Config lesen
        String command = plugin.getConfig().getString("friends.command", "friends");

        if (command.isEmpty()) {
            player.sendMessage(translateColorCodes("&c&lFriends &7system is not configured properly!"));
            plugin.getLogger().warning("Friends command not configured in config.yml!");
            return;
        }

        // Platzhalter ersetzen
        String finalCommand = command
                .replace("%player%", player.getName())
                .replace("%uuid%", player.getUniqueId().toString())
                .replace("%displayname%", player.getDisplayName());

        try {
            // Command als Spieler ausführen
            if (finalCommand.startsWith("/")) {
                // Als Spieler-Command ausführen
                player.performCommand(finalCommand.substring(1));
            } else {
                // Als Console-Command ausführen
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
            }

            // Erfolgs-Sound abspielen
            String sound = plugin.getConfig().getString("friends.sound", "UI_BUTTON_CLICK");
            float volume = (float) plugin.getConfig().getDouble("friends.sound-volume", 1.0);
            float pitch = (float) plugin.getConfig().getDouble("friends.sound-pitch", 1.0);

            try {
                player.playSound(player.getLocation(), org.bukkit.Sound.valueOf(sound), volume, pitch);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid sound in config: " + sound);
            }

        } catch (Exception e) {
            player.sendMessage(translateColorCodes("&c&lError &7executing friends command!"));
            plugin.getLogger().severe("Error executing friends command: " + e.getMessage());
        }
    }

    // ==================== UTILITY METHODEN ====================

    /**
     * Aktualisiert den Friends-Kopf eines Spielers (bei Config-Reload)
     */
    public void updateFriendsHead(Player player) {
        removeFriendsHead(player);
        giveFriendsHead(player);
    }

    /**
     * Gibt allen Online-Spielern den Friends-Kopf
     */
    public void giveAllPlayersFriendsHead() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            giveFriendsHead(player);
        }
    }

    /**
     * Prüft die Config-Einstellungen und gibt Warnungen aus
     */
    public void validateConfig() {
        if (plugin.getConfig().getString("friends.command", "").isEmpty()) {
            plugin.getLogger().warning("Friends command not configured! Add 'friends.command' to config.yml");
        }

        if (plugin.getConfig().getString("friends.head-name", "").isEmpty()) {
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
        validateConfig();
        plugin.getLogger().info("PlayerHeadManager registered!");
    }
}