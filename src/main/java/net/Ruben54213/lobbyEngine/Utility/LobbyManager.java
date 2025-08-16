package net.Ruben54213.lobbyEngine.Utility;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.*;

/**
 * Verwaltet das Lobby-Switching System mit GUI für BungeeCord/Velocity
 */
public class LobbyManager implements Listener {

    private final JavaPlugin plugin;
    private final Set<String> registeredLobbies = new HashSet<>();
    private final String currentServer;
    private final Map<String, String> lobbyToServerMapping = new HashMap<>();

    public LobbyManager(JavaPlugin plugin) {
        this.plugin = plugin;
        // Server-Name aus BungeeCord/Velocity oder Config ermitteln
        this.currentServer = getCurrentServerName();
        loadLobbiesFromConfig();
        setupBungeeChannel();
    }

    /**
     * Registriert BungeeCord Plugin Channel
     */
    private void setupBungeeChannel() {
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
        plugin.getLogger().info("BungeeCord plugin channel registered!");
    }

    /**
     * Gibt einem Spieler das Lobby-Item
     */
    public void giveLobbyItem(Player player) {
        ItemStack lobbyItem = createLobbyItem();

        // Lobby-Item in Slot 1 setzen (2. Slot der Hotbar)
        player.getInventory().setItem(1, lobbyItem);

        plugin.getLogger().info("Lobby item given to player: " + player.getName());
    }

    /**
     * Erstellt das Lobby-Item
     */
    private ItemStack createLobbyItem() {
        ItemStack item = new ItemStack(Material.GLOWSTONE_DUST);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // Name aus Config oder Standard
            String itemName = plugin.getConfig().getString("lobby.item-name", "&6&lLobby &8&l| &7Rechtsklick");
            meta.setDisplayName(translateColorCodes(itemName));

            // Lore aus Config oder Standard
            List<String> loreList = plugin.getConfig().getStringList("lobby.item-lore");
            if (loreList.isEmpty()) {
                // Standard-Lore wenn nichts in Config
                loreList = List.of(
                        "&7Klicke um Lobbies zu wechseln!",
                        "",
                        "&8Aktuelle Lobby: &e" + getCurrentLobbyDisplayName()
                );
            }

            // Platzhalter in Lore ersetzen
            List<String> translatedLore = loreList.stream()
                    .map(line -> line.replace("%current_server%", getCurrentLobbyDisplayName()))
                    .map(this::translateColorCodes)
                    .toList();

            meta.setLore(translatedLore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Gibt den Display-Namen der aktuellen Lobby zurück (Lobby-1, Lobby-2, etc.)
     */
    private String getCurrentLobbyDisplayName() {
        // Finde die Lobby-Nummer basierend auf dem aktuellen Server
        for (Map.Entry<String, String> entry : lobbyToServerMapping.entrySet()) {
            if (entry.getValue().equals(currentServer)) {
                return entry.getKey();
            }
        }
        // Fallback: Verwende Lobby-1 als Standard
        return "Lobby-1";
    }

    /**
     * Prüft ob ein Item das Lobby-Item ist
     */
    public boolean isLobbyItem(ItemStack item) {
        if (item == null || item.getType() != Material.GLOWSTONE_DUST) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }

        // Hole den erwarteten Namen aus der Config
        String expectedName = translateColorCodes(plugin.getConfig().getString("lobby.item-name", "&6&lLobby &8&l| &7Rechtsklick"));
        String actualName = meta.getDisplayName();

        return expectedName.equals(actualName);
    }

    /**
     * Öffnet das Lobby-Switching GUI
     */
    public void openLobbyGUI(Player player) {
        // 3 Zeilen GUI erstellen
        String guiTitle = plugin.getConfig().getString("shortprefix", "") + plugin.getConfig().getString("lobby.gui-title", "");
        Inventory gui = Bukkit.createInventory(null, 27, translateColorCodes(guiTitle));

        // Glass Borders erstellen
        fillBorders(gui);

        // Lobbies hinzufügen (Slots 10-16 für max. 7 Lobbies)
        int slot = 10;
        for (int i = 1; i <= 7; i++) {
            String lobbyName = "Lobby-" + i;
            if (registeredLobbies.contains(lobbyName)) {
                boolean isCurrentLobby = lobbyName.equals(getCurrentLobbyDisplayName());
                ItemStack lobbyItem = createLobbyItemForGUI(lobbyName, isCurrentLobby);
                gui.setItem(slot, lobbyItem);
            }
            slot++;
            if (slot > 16) break;
        }

        // Sound abspielen
        String sound = plugin.getConfig().getString("lobby.sound", "UI_BUTTON_CLICK");
        float volume = (float) plugin.getConfig().getDouble("lobby.sound-volume", 1.0);
        float pitch = (float) plugin.getConfig().getDouble("lobby.sound-pitch", 1.0);

        try {
            player.playSound(player.getLocation(), Sound.valueOf(sound), volume, pitch);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound in lobby config: " + sound);
        }

        // GUI öffnen
        player.openInventory(gui);
    }

    /**
     * Erstellt ein Lobby-Item für das GUI
     */
    private ItemStack createLobbyItemForGUI(String lobbyName, boolean isCurrentLobby) {
        // Material basierend auf aktueller Lobby - FIXED: Aktuelle Lobby = GLOWSTONE_DUST
        Material material = isCurrentLobby ? Material.GLOWSTONE_DUST : Material.SUGAR;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // Name formatieren
            String displayName = plugin.getConfig().getString("lobby.lobby-item-name", "&e&l%lobby%")
                    .replace("%lobby%", lobbyName);
            meta.setDisplayName(translateColorCodes(displayName));

            // Lore basierend auf Status
            List<String> lore = new ArrayList<>();
            if (isCurrentLobby) {
                // Aktuelle Lobby
                List<String> currentLore = plugin.getConfig().getStringList("lobby.current-lobby-lore");
                if (currentLore.isEmpty()) {
                    currentLore = List.of(
                            "&a✓ Aktuelle Lobby",
                            "",
                            "&7Du bist bereits hier!"
                    );
                }
                lore = currentLore;
            } else {
                // Andere Lobby - OHNE "Lobby: server" Zeile
                List<String> otherLore = plugin.getConfig().getStringList("lobby.other-lobby-lore");
                if (otherLore.isEmpty()) {
                    otherLore = List.of(
                            "&7Klicke um zu wechseln!"
                    );
                }
                lore = otherLore;
            }

            // Platzhalter ersetzen und Farbcodes übersetzen (ohne Server-Info)
            List<String> translatedLore = lore.stream()
                    .map(line -> line.replace("%lobby%", lobbyName))
                    .map(this::translateColorCodes)
                    .toList();

            meta.setLore(translatedLore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Füllt die Ränder mit Glass Panes
     */
    private void fillBorders(Inventory gui) {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(translateColorCodes("&7"));
            glass.setItemMeta(glassMeta);
        }

        // Erste und letzte Reihe
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, glass);
            gui.setItem(i + 18, glass);
        }

        // Seitenränder
        gui.setItem(9, glass);
        gui.setItem(17, glass);
    }

    /**
     * Lädt registrierte Lobbies aus der Config
     */
    private void loadLobbiesFromConfig() {
        // Lade alle konfigurierten Server (Lobby-1 bis Lobby-7)
        for (int i = 1; i <= 7; i++) {
            String lobbySlot = "Lobby-" + i;
            String serverName = plugin.getConfig().getString("lobby.servers.slot" + i + ".server", null);

            if (serverName != null && plugin.getConfig().getBoolean("lobby.servers.slot" + i + ".enabled", false)) {
                registeredLobbies.add(lobbySlot);
                lobbyToServerMapping.put(lobbySlot, serverName);
            }
        }

        plugin.getLogger().info("Loaded " + registeredLobbies.size() + " lobbies from config");
        plugin.getLogger().info("Lobby mappings: " + lobbyToServerMapping);
    }

    /**
     * Fügt eine neue Lobby hinzu
     */
    public boolean addLobby(String serverName) {
        if (registeredLobbies.size() >= 7) {
            return false; // Max. 7 Lobbies
        }

        // Finde den nächsten freien Slot
        int nextSlot = -1;
        for (int i = 1; i <= 7; i++) {
            String lobbyName = "Lobby-" + i;
            if (!registeredLobbies.contains(lobbyName)) {
                nextSlot = i;
                break;
            }
        }

        if (nextSlot == -1) {
            return false; // Kein freier Slot
        }

        String lobbyName = "Lobby-" + nextSlot;
        registeredLobbies.add(lobbyName);
        lobbyToServerMapping.put(lobbyName, serverName);

        // In Config speichern
        plugin.getConfig().set("lobby.servers.slot" + nextSlot + ".server", serverName);
        plugin.getConfig().set("lobby.servers.slot" + nextSlot + ".enabled", true);
        plugin.saveConfig();

        plugin.getLogger().info("Added new lobby: " + lobbyName + " -> " + serverName);
        return true;
    }

    /**
     * Entfernt eine Lobby
     */
    public boolean removeLobby(String lobbyName) {
        if (!registeredLobbies.contains(lobbyName)) {
            return false; // Lobby existiert nicht
        }

        // Slot-Nummer extrahieren
        int slotNumber = Integer.parseInt(lobbyName.substring(6)); // "Lobby-1" -> 1

        registeredLobbies.remove(lobbyName);
        lobbyToServerMapping.remove(lobbyName);

        // Aus Config entfernen
        plugin.getConfig().set("lobby.servers.slot" + slotNumber, null);
        plugin.saveConfig();

        plugin.getLogger().info("Removed lobby: " + lobbyName);
        return true;
    }

    /**
     * Ermittelt den aktuellen Server-Namen
     */
    private String getCurrentServerName() {
        // Versuche Server-Name aus verschiedenen Quellen zu ermitteln
        String serverName = plugin.getConfig().getString("lobby.current-server", "lobby-1");

        // TODO: Hier könnte BungeeCord/Velocity API Integration hinzugefügt werden
        // Für jetzt nutzen wir den Config-Wert

        return serverName;
    }

    /**
     * Verbindet einen Spieler mit einer anderen Lobby über BungeeCord/Velocity
     */
    private void connectToLobby(Player player, String lobbyName) {
        String currentLobby = getCurrentLobbyDisplayName();

        if (lobbyName.equals(currentLobby)) {
            String message = plugin.getConfig().getString("lobby.already-connected", "&cDu bist bereits mit dieser Lobby verbunden!");
            player.sendMessage(translateColorCodes(message));
            return;
        }

        // Server-Name aus Mapping holen
        String targetServer = lobbyToServerMapping.get(lobbyName);
        if (targetServer == null) {
            plugin.getLogger().warning("No server mapping found for lobby: " + lobbyName);
            return;
        }

        String connectingMessage = plugin.getConfig().getString("prefix", "") + plugin.getConfig().getString("lobby.connecting", "&7Du wirst mit &e%lobby%&7 verbunden...")
                .replace("%lobby%", lobbyName);
        player.sendMessage(translateColorCodes(connectingMessage));

        // BungeeCord/Velocity Connect
        connectPlayerToServer(player, targetServer);

        plugin.getLogger().info("Player " + player.getName() + " connecting to lobby: " + lobbyName + " (server: " + targetServer + ")");
    }

    /**
     * Sendet BungeeCord Connect Command
     */
    private void connectPlayerToServer(Player player, String serverName) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);

            out.writeUTF("Connect");
            out.writeUTF(serverName);

            player.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());

            plugin.getLogger().info("Sent BungeeCord connect message for " + player.getName() + " to server: " + serverName);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to send BungeeCord connect message: " + e.getMessage());

            String errorMessage = plugin.getConfig().getString("lobby.connection-error", "&cVerbindung zu %lobby% fehlgeschlagen!");
            player.sendMessage(translateColorCodes(errorMessage.replace("%lobby%", serverName)));
        }
    }

    // ==================== EVENT HANDLERS ====================

    /**
     * Behandelt Rechtsklick auf Lobby-Item
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Prüfen ob Rechtsklick
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // Prüfen ob es das Lobby-Item ist
        if (!isLobbyItem(item)) {
            return;
        }

        // Event abbrechen um Block-Interaktion zu verhindern
        event.setCancelled(true);

        // Lobby-GUI öffnen
        openLobbyGUI(player);
    }

    /**
     * Behandelt Klicks im Lobby-GUI
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        String expectedTitle = translateColorCodes(plugin.getConfig().getString("shortprefix", "") + plugin.getConfig().getString("lobby.gui-title", ""));

        // Prüfen ob es das Lobby-GUI ist
        if (!title.equals(expectedTitle)) {
            return;
        }

        // Alle Klicks verhindern
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        // Nur auf Lobby-Items reagieren (Slot 10-16)
        int slot = event.getSlot();
        if (slot < 10 || slot > 16) {
            return;
        }

        // Lobby-Name aus Slot ableiten
        int lobbyNumber = slot - 9; // Slot 10 = Lobby-1, Slot 11 = Lobby-2, etc.
        String lobbyName = "Lobby-" + lobbyNumber;

        if (registeredLobbies.contains(lobbyName)) {
            connectToLobby(player, lobbyName);
            player.closeInventory();
        }
    }

    // ==================== UTILITY METHODEN ====================

    /**
     * Gibt alle registrierten Lobbies zurück
     */
    public Set<String> getRegisteredLobbies() {
        return new HashSet<>(registeredLobbies);
    }

    /**
     * Gibt die maximale Anzahl von Lobbies zurück
     */
    public int getMaxLobbies() {
        return 7;
    }

    /**
     * Gibt den aktuellen Server zurück
     */
    public String getCurrentServer() {
        return currentServer;
    }

    /**
     * Gibt den letzten hinzugefügten Lobby-Namen zurück (für Erfolgs-Nachricht)
     */
    public String getLastAddedLobbyName() {
        // Finde den höchsten Lobby-Slot
        for (int i = 7; i >= 1; i--) {
            String lobbyName = "Lobby-" + i;
            if (registeredLobbies.contains(lobbyName)) {
                return lobbyName;
            }
        }
        return "Lobby-1";
    }

    /**
     * Entfernt das Lobby-Item vom Spieler
     */
    public void removeLobbyItem(Player player) {
        player.getInventory().setItem(1, null);
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
        plugin.getLogger().info("LobbyManager registered!");
    }
}