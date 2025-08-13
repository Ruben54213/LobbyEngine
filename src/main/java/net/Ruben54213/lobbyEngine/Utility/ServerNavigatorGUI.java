package net.Ruben54213.lobbyEngine.Utility;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Verwaltet das Server-Navigator GUI
 */
public class ServerNavigatorGUI implements Listener {

    private final JavaPlugin plugin;
    private final Map<Integer, String> serverSlots = new HashMap<>();

    public ServerNavigatorGUI(JavaPlugin plugin) {
        this.plugin = plugin;
        loadServersFromConfig();
    }

    /**
     * Lädt Server-Konfiguration aus der Config
     */
    private void loadServersFromConfig() {
        serverSlots.clear();
        ConfigurationSection serversSection = plugin.getConfig().getConfigurationSection("navigator.servers");

        if (serversSection != null) {
            for (String key : serversSection.getKeys(false)) {
                try {
                    int slot = Integer.parseInt(key);
                    serverSlots.put(slot, key);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid slot number in navigator config: " + key);
                }
            }
        }
    }

    /**
     * Öffnet das Navigator-GUI für einen Spieler
     */
    public void openNavigator(Player player) {
        // GUI erstellen (5 Zeilen = 45 Slots)
        Inventory gui = Bukkit.createInventory(null, 45, translateColorCodes("&d&lServer Navigator"));

        // Server-Items hinzufügen
        for (Map.Entry<Integer, String> entry : serverSlots.entrySet()) {
            int slot = entry.getKey();
            String serverKey = entry.getValue();

            if (slot >= 0 && slot < 45) {
                ItemStack serverItem = createServerItem(serverKey);
                if (serverItem != null) {
                    gui.setItem(slot, serverItem);
                }
            }
        }

        // Sound abspielen
        String openSound = plugin.getConfig().getString("navigator.sounds.open", "UI_BUTTON_CLICK");
        float volume = (float) plugin.getConfig().getDouble("navigator.sounds.volume", 1.0);
        float pitch = (float) plugin.getConfig().getDouble("navigator.sounds.pitch", 1.0);

        try {
            player.playSound(player.getLocation(), Sound.valueOf(openSound), volume, pitch);
        } catch (IllegalArgumentException e) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }

        // GUI öffnen
        player.openInventory(gui);
    }

    /**
     * Erstellt ein Server-Item basierend auf der Config
     */
    private ItemStack createServerItem(String serverKey) {
        ConfigurationSection serverConfig = plugin.getConfig().getConfigurationSection("navigator.servers." + serverKey);

        if (serverConfig == null) {
            return null;
        }

        String materialName = serverConfig.getString("material", "GRASS_BLOCK");
        String displayName = serverConfig.getString("name", "&aServer");
        String serverName = serverConfig.getString("server", "lobby");

        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid material for server " + serverKey + ": " + materialName);
            material = Material.GRASS_BLOCK;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(translateColorCodes(displayName));
            meta.setLore(List.of(
                    translateColorCodes("&7Click to connect to:"),
                    translateColorCodes("&b" + serverName),
                    "",
                    translateColorCodes("&e► Click to join!")
            ));
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Behandelt Klicks im Navigator-GUI
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        // Prüfen ob es das Navigator-GUI ist
        if (!event.getView().getTitle().equals(translateColorCodes(plugin.getConfig().getString("shortprefix", "") + "&7Select a &eServer"))) {
            return;
        }

        // Alle Klicks im Navigator-GUI verhindern
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        // Server aus dem geklickten Slot ermitteln
        int slot = event.getSlot();
        String serverKey = serverSlots.get(slot);

        if (serverKey != null) {
            ConfigurationSection serverConfig = plugin.getConfig().getConfigurationSection("navigator.servers." + serverKey);
            if (serverConfig != null) {
                String serverName = serverConfig.getString("server", "lobby");
                connectToServer(player, serverName);
            }
        }
    }

    /**
     * Verbindet einen Spieler zu einem Server (Velocity/BungeeCord)
     */
    private void connectToServer(Player player, String serverName) {
        // Verbindungs-Nachricht
        String connectMessage = translateColorCodes(
                plugin.getConfig().getString("messages.navigator.connecting",
                                "&aYou will now be connected to &b%server%&a...")
                        .replace("%server%", serverName)
        );
        player.sendMessage(connectMessage);

        // Debug-Nachricht
        plugin.getLogger().info("Attempting to connect " + player.getName() + " to server: " + serverName);

        // GUI schließen
        player.closeInventory();

        // Kanäle registrieren falls noch nicht geschehen
        registerChannels();

        // BungeeCord Connection (mit verbesserter Logik)
        if (sendBungeeConnect(player, serverName)) {
            plugin.getLogger().info("BungeeCord connect message sent for " + player.getName() + " to " + serverName);
        } else {
            plugin.getLogger().warning("Failed to send BungeeCord connect message for " + player.getName());
        }
    }

    /**
     * Registriert Messaging-Kanäle
     */
    private void registerChannels() {
        try {
            if (!plugin.getServer().getMessenger().isOutgoingChannelRegistered(plugin, "BungeeCord")) {
                plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
                plugin.getLogger().info("Registered BungeeCord messaging channel");
            }
            if (!plugin.getServer().getMessenger().isOutgoingChannelRegistered(plugin, "velocity:main")) {
                plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "velocity:main");
                plugin.getLogger().info("Registered Velocity messaging channel");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to register messaging channels: " + e.getMessage());
        }
    }

    /**
     * Sendet Velocity-Connect Nachricht
     */
    private void sendVelocityConnect(Player player, String serverName) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            out.writeUTF("Connect");
            out.writeUTF(serverName);

            player.sendPluginMessage(plugin, "velocity:main", b.toByteArray());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send Velocity connect message: " + e.getMessage());
            sendBungeeConnect(player, serverName);
        }
    }

    /**
     * Sendet BungeeCord-Connect Nachricht
     */
    private boolean sendBungeeConnect(Player player, String serverName) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            out.writeUTF("Connect");
            out.writeUTF(serverName);
            out.close();

            player.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
            plugin.getLogger().info("Sent BungeeCord connect message: " + serverName);
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send BungeeCord connect message: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Wandelt & Farbcodes in § Farbcodes um
     */
    private String translateColorCodes(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Lädt die Server-Konfiguration neu
     */
    public void reloadServers() {
        loadServersFromConfig();
    }

    /**
     * Registriert diesen Listener beim Plugin
     */
    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        registerChannels();
    }
}