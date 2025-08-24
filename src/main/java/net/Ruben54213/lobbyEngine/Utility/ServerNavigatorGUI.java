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
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Verwaltet das Server-Navigator GUI
 */
public class ServerNavigatorGUI implements Listener, PluginMessageListener {

    private final JavaPlugin plugin;
    private final Map<Integer, String> serverSlots = new HashMap<>();
    private final Map<Player, Inventory> smashGUIs = new HashMap<>();
    private final Map<String, Boolean> serverOnlineStatus = new HashMap<>();
    private final Map<String, Integer> serverPlayerCount = new HashMap<>();
    private final List<String> onlineSmashServers = new ArrayList<>();
    private boolean requestInProgress = false;
    private long lastResponseTime = 0;

    public ServerNavigatorGUI(JavaPlugin plugin) {
        this.plugin = plugin;
        loadServersFromConfig();
        initializeSmashServers();
        registerPluginMessageListener();
        startSmashServerUpdater();
    }

    /**
     * Registriert den PluginMessageListener für Velocity-Antworten
     */
    private void registerPluginMessageListener() {
        // Registriere beide möglichen Kanäle
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "velocity:main", this);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "bungeecord:main", this);

        // Auch ausgehende Kanäle registrieren
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "velocity:main");
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "bungeecord:main");

        plugin.getLogger().info("[Channel] Registered plugin message listeners for velocity:main and bungeecord:main");
    }

    /**
     * Behandelt eingehende Plugin-Nachrichten von Velocity
     */
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("velocity:main") && !channel.equals("bungeecord:main")) {
            return;
        }

        lastResponseTime = System.currentTimeMillis();
        plugin.getLogger().info("[Velocity Response] Received message on channel: " + channel);

        try {
            ByteArrayInputStream stream = new ByteArrayInputStream(message);
            DataInputStream in = new DataInputStream(stream);

            String subchannel = in.readUTF();
            plugin.getLogger().info("[Velocity Response] Subchannel: " + subchannel);

            if (subchannel.equals("GetServers")) {
                String serverList = in.readUTF();
                plugin.getLogger().info("[Velocity] Raw server list: " + serverList);

                // Parse server list
                String[] servers = serverList.split(", ");

                // Reset all Smash servers to offline first
                synchronized (this) {
                    for (int i = 1; i <= 18; i++) {
                        serverOnlineStatus.put("Smash-" + i, false);
                        serverPlayerCount.put("Smash-" + i, 0); // Reset auch player counts
                    }

                    // Mark found Smash servers as online
                    int foundSmashServers = 0;
                    for (String serverName : servers) {
                        serverName = serverName.trim();
                        if (serverName.startsWith("Smash-") && !serverName.toLowerCase().contains("bauserver")) {
                            serverOnlineStatus.put(serverName, true);
                            foundSmashServers++;
                            plugin.getLogger().info("[Velocity] Found online Smash server: " + serverName);
                        }
                    }

                    plugin.getLogger().info("[Velocity] Total Smash servers found: " + foundSmashServers);
                    updateOnlineServersList();

                    // Update GUIs sofort im nächsten Tick
                    Bukkit.getScheduler().runTask(plugin, this::updateSmashGUIs);
                }

            } else if (subchannel.equals("PlayerCount")) {
                String serverName = in.readUTF();
                int playerCount = in.readInt();

                if (serverName.startsWith("Smash-")) {
                    synchronized (this) {
                        // NUR Player Count aktualisieren, wenn Server bereits als online erkannt wurde
                        if (serverOnlineStatus.getOrDefault(serverName, false)) {
                            serverPlayerCount.put(serverName, playerCount);
                            plugin.getLogger().info("[Velocity] " + serverName + " -> " + playerCount + " players (already known online)");
                        } else {
                            plugin.getLogger().info("[Velocity] Ignoring PlayerCount for " + serverName + " (not in online list)");
                        }
                    }

                    // Update GUIs sofort im nächsten Tick
                    Bukkit.getScheduler().runTask(plugin, this::updateSmashGUIs);
                }
            }

        } catch (Exception e) {
            plugin.getLogger().severe("[Velocity] Error processing message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Initialisiert die Smash-Server Liste
     */
    private void initializeSmashServers() {
        synchronized (this) {
            serverOnlineStatus.clear();
            serverPlayerCount.clear();

            for (int i = 1; i <= 18; i++) {
                String serverName = "Smash-" + i;
                serverOnlineStatus.put(serverName, false);
                serverPlayerCount.put(serverName, 0);
            }
        }

        plugin.getLogger().info("[Init] Initialized 18 Smash server slots");
    }

    /**
     * Startet den Server-Update Task
     */
    private void startSmashServerUpdater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (requestInProgress) {
                    plugin.getLogger().info("[Update] Request already in progress, skipping...");
                    return;
                }

                requestInProgress = true;
                plugin.getLogger().info("[Update] Starting server status update cycle");

                // Nur GetServers um die online Server zu finden
                requestServerList();

                // Nach 3 Sekunden für bekannte online Server PlayerCounts abfragen
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    requestAllPlayerCounts(); // Nur für bereits als online erkannte Server
                }, 60L); // 3 Sekunden

                // Nach 5 Sekunden GUIs aktualisieren und Request freigeben
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    updateSmashGUIs();
                    requestInProgress = false;
                    plugin.getLogger().info("[Update] Server status update cycle completed");

                    synchronized (ServerNavigatorGUI.this) {
                        plugin.getLogger().info("[Update] Current online servers: " + onlineSmashServers);
                    }
                }, 100L); // 5 Sekunden
            }
        }.runTaskTimer(plugin, 60L, 20L * 15); // Nach 3 Sekunden starten, dann alle 15 Sekunden
    }

    /**
     * Fragt die komplette Server-Liste von Velocity ab
     */
    private void requestServerList() {
        if (Bukkit.getOnlinePlayers().isEmpty()) {
            plugin.getLogger().warning("[Request] No players online, cannot request server list");
            return;
        }

        Player anyPlayer = Bukkit.getOnlinePlayers().iterator().next();

        // Versuche beide Kanäle
        String[] channels = {"velocity:main", "bungeecord:main"};

        for (String channel : channels) {
            try {
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(b);
                out.writeUTF("GetServers");
                out.close();

                anyPlayer.sendPluginMessage(plugin, channel, b.toByteArray());
                plugin.getLogger().info("[Request] Sent GetServers request via " + anyPlayer.getName() + " on channel " + channel);

            } catch (Exception e) {
                plugin.getLogger().warning("[Request] Failed to send GetServers on " + channel + ": " + e.getMessage());
            }
        }
    }

    /**
     * Fragt direkt Spieleranzahlen für alle möglichen Smash-Server ab (Fallback)
     */
    private void requestAllPlayerCountsDirect() {
        if (Bukkit.getOnlinePlayers().isEmpty()) {
            plugin.getLogger().warning("[Request] No players online, cannot request player counts");
            return;
        }

        Player anyPlayer = Bukkit.getOnlinePlayers().iterator().next();
        String[] channels = {"velocity:main", "bungeecord:main"};

        plugin.getLogger().info("[Request] Requesting player counts for all Smash servers (direct method)");

        // Frage alle 18 möglichen Smash-Server ab
        for (int i = 1; i <= 18; i++) {
            String serverName = "Smash-" + i;

            for (String channel : channels) {
                try {
                    ByteArrayOutputStream b = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(b);
                    out.writeUTF("PlayerCount");
                    out.writeUTF(serverName);
                    out.close();

                    anyPlayer.sendPluginMessage(plugin, channel, b.toByteArray());

                    // Kleine Verzögerung zwischen den Anfragen
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                } catch (Exception e) {
                    plugin.getLogger().warning("[Request] Failed PlayerCount for " + serverName + " on " + channel + ": " + e.getMessage());
                }
            }
        }

        plugin.getLogger().info("[Request] Sent direct PlayerCount requests for Smash-1 to Smash-18");
    }

    /**
     * Fragt Spieleranzahlen für online Server ab
     */
    private void requestAllPlayerCounts() {
        if (Bukkit.getOnlinePlayers().isEmpty()) {
            plugin.getLogger().warning("[Request] No players online, cannot request player counts");
            return;
        }

        Player anyPlayer = Bukkit.getOnlinePlayers().iterator().next();
        String[] channels = {"velocity:main", "bungeecord:main"};

        // Nur für die Server anfragen, die als online erkannt wurden
        List<String> serversToCheck = new ArrayList<>();
        synchronized (this) {
            for (int i = 1; i <= 18; i++) {
                String serverName = "Smash-" + i;
                if (serverOnlineStatus.getOrDefault(serverName, false)) {
                    serversToCheck.add(serverName);
                }
            }
        }

        plugin.getLogger().info("[Request] Requesting player counts for " + serversToCheck.size() + " online servers");

        for (String serverName : serversToCheck) {
            for (String channel : channels) {
                try {
                    ByteArrayOutputStream b = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(b);
                    out.writeUTF("PlayerCount");
                    out.writeUTF(serverName);
                    out.close();

                    anyPlayer.sendPluginMessage(plugin, channel, b.toByteArray());

                    // Kleine Verzögerung zwischen den Anfragen
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                } catch (Exception e) {
                    plugin.getLogger().warning("[Request] Failed PlayerCount for " + serverName + " on " + channel + ": " + e.getMessage());
                }
            }
        }

        plugin.getLogger().info("[Request] Sent PlayerCount requests for " + serversToCheck.size() + " servers");
    }

    /**
     * Aktualisiert die Liste der online Smash-Server
     */
    private void updateOnlineServersList() {
        List<String> previousOnlineServers = new ArrayList<>(onlineSmashServers);
        onlineSmashServers.clear();

        for (int i = 1; i <= 18; i++) {
            String serverName = "Smash-" + i;
            if (serverOnlineStatus.getOrDefault(serverName, false)) {
                onlineSmashServers.add(serverName);
            }
        }

        // Log changes
        if (!onlineSmashServers.equals(previousOnlineServers)) {
            plugin.getLogger().info("[Status Change] Online Smash servers updated:");
            plugin.getLogger().info("[Status Change] Previous: " + previousOnlineServers);
            plugin.getLogger().info("[Status Change] Current: " + onlineSmashServers);
        }
    }

    /**
     * Aktualisiert alle offenen Smash-GUIs
     */
    private void updateSmashGUIs() {
        List<Player> playersToRemove = new ArrayList<>();

        synchronized (this) {
            for (Map.Entry<Player, Inventory> entry : smashGUIs.entrySet()) {
                Player player = entry.getKey();
                Inventory gui = entry.getValue();

                if (player.isOnline() && gui.equals(player.getOpenInventory().getTopInventory())) {
                    // GUI sofort aktualisieren
                    Bukkit.getScheduler().runTask(plugin, () -> updateSmashGUI(gui));
                    plugin.getLogger().info("[GUI Update] Updated Smash GUI for " + player.getName() +
                            " with " + onlineSmashServers.size() + " servers");
                } else {
                    playersToRemove.add(player);
                }
            }
        }

        playersToRemove.forEach(smashGUIs::remove);
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
        Inventory gui = Bukkit.createInventory(null, 45, translateColorCodes(plugin.getConfig().getString("shortprefix", "") + "&7Select a &eServer"));

        // Gray Stained Glass Panes für Ränder erstellen
        ItemStack glassBorder = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glassBorder.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(translateColorCodes("&7"));
            glassBorder.setItemMeta(glassMeta);
        }

        // Ränder mit Gray Stained Glass Panes füllen
        fillBorders(gui, glassBorder);

        // Server-Items nur in erlaubte Slots hinzufügen
        for (Map.Entry<Integer, String> entry : serverSlots.entrySet()) {
            int slot = entry.getKey();
            String serverKey = entry.getValue();

            if (isValidServerSlot(slot)) {
                ItemStack serverItem = createServerItem(serverKey);
                if (serverItem != null) {
                    gui.setItem(slot, serverItem);
                }
            }
        }

        // Smash-Item auf Slot 15 hinzufügen (unabhängig von Config)
        ItemStack smashItem = createSmashItem();
        gui.setItem(15, smashItem);

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
     * Erstellt das Smash-Item für Slot 15
     */
    private ItemStack createSmashItem() {
        ItemStack item = new ItemStack(Material.GOLDEN_SWORD);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            int onlineServers;
            synchronized (this) {
                onlineServers = onlineSmashServers.size();
            }
            meta.setDisplayName(translateColorCodes("&a&lSmash &7(1.21.5+)"));
            meta.setLore(List.of(
                    translateColorCodes("&7Trete den Smash-Servern bei!"),
                    translateColorCodes("&7Online Server: &a" + onlineServers),
                    "",
                    translateColorCodes("&e► Klicke hier!")
            ));
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Öffnet das Smash-GUI für einen Spieler
     */
    public void openSmashGUI(Player player) {
        plugin.getLogger().info("[GUI] Opening Smash GUI for " + player.getName());

        synchronized (this) {
            plugin.getLogger().info("[GUI] Current online servers: " + onlineSmashServers);
        }

        // Aktuelle Server-Status sofort abfragen
        if (!requestInProgress) {
            requestInProgress = true;

            // Erst GetServers, dann PlayerCounts
            requestServerList();

            // Nach kurzer Verzögerung GUI öffnen - VERKÜRZT von 30L auf 10L
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                createAndOpenSmashGUI(player);
                requestInProgress = false;
            }, 10L); // 0.5 Sekunden statt 1.5 Sekunden
        } else {
            // Falls bereits Request läuft, GUI sofort öffnen
            createAndOpenSmashGUI(player);
        }
    }

    /**
     * Erstellt und öffnet das Smash-GUI
     */
    private void createAndOpenSmashGUI(Player player) {
        // 4x9 GUI erstellen (36 Slots)
        Inventory gui = Bukkit.createInventory(null, 36, translateColorCodes("&a&lSmash &7Serverübersicht"));

        // Glasscheiben für obere und untere Reihe
        ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glassPane.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(translateColorCodes("&7"));
            glassPane.setItemMeta(glassMeta);
        }

        // Obere Reihe (Slots 0-8)
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, glassPane);
        }

        // Untere Reihe (Slots 27-35)
        for (int i = 27; i < 36; i++) {
            gui.setItem(i, glassPane);
        }

        // Server anzeigen
        updateSmashGUI(gui);

        // GUI speichern
        smashGUIs.put(player, gui);

        // GUI öffnen
        player.openInventory(gui);

        // Sound abspielen
        try {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        } catch (Exception e) {
            // Fallback
        }
    }

    /**
     * Aktualisiert die Smash-Server Items im GUI
     */
    private void updateSmashGUI(Inventory gui) {
        // Mittlere Slots leeren
        for (int i = 9; i < 27; i++) {
            gui.setItem(i, null);
        }

        int currentSlot = 9;
        List<String> currentOnlineServers;

        synchronized (this) {
            currentOnlineServers = new ArrayList<>(onlineSmashServers);
        }

        plugin.getLogger().info("[GUI Update] Updating GUI with " + currentOnlineServers.size() + " online servers");

        // Online Server anzeigen
        for (String serverName : currentOnlineServers) {
            if (currentSlot >= 27) break;

            ItemStack serverItem = createSmashServerItem(serverName);
            gui.setItem(currentSlot, serverItem);
            plugin.getLogger().info("[GUI Update] Added " + serverName + " to slot " + currentSlot);
            currentSlot++;
        }

        // Wenn keine Server online sind
        if (currentOnlineServers.isEmpty()) {
            ItemStack infoItem = new ItemStack(Material.BARRIER);
            ItemMeta meta = infoItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(translateColorCodes("&c&lKeine Server online"));
                meta.setLore(List.of(
                        translateColorCodes("&7Momentan sind keine"),
                        translateColorCodes("&7Smash-Server verfügbar.")
                ));
                infoItem.setItemMeta(meta);
            }
            gui.setItem(9, infoItem);
            plugin.getLogger().info("[GUI Update] No servers online, showing info item");
        }
    }

    /**
     * Erstellt ein Smash-Server Item
     */
    private ItemStack createSmashServerItem(String serverName) {
        ItemStack item = new ItemStack(Material.LIME_TERRACOTTA);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            int currentPlayers;
            synchronized (this) {
                currentPlayers = serverPlayerCount.getOrDefault(serverName, 0);
            }
            int maxPlayers = 8;

            String statusColor = "&a";
            String statusText = "Verfügbar";

            if (currentPlayers >= maxPlayers) {
                statusColor = "&c";
                statusText = "Voll";
            } else if (currentPlayers >= maxPlayers * 0.8) {
                statusColor = "&e";
                statusText = "Fast voll";
            }

            meta.setDisplayName(translateColorCodes("&a&l" + serverName));
            meta.setLore(List.of(
                    translateColorCodes("&7Spieler: &a" + currentPlayers + "&7/&c" + maxPlayers),
                    translateColorCodes("&7Status: &aLobby"),
                    translateColorCodes("&7Karte: &aVoting")
            ));
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Aktualisiert den Online-Status für einen Server
     */
    public void updateServerOnlineStatus(String serverName, boolean isOnline) {
        synchronized (this) {
            serverOnlineStatus.put(serverName, isOnline);
        }
        plugin.getLogger().info("Updated online status for " + serverName + ": " + isOnline);
        updateOnlineServersList();
    }

    /**
     * Aktualisiert die Spieleranzahl für einen Server
     */
    public void updateServerPlayerCount(String serverName, int playerCount) {
        synchronized (this) {
            serverPlayerCount.put(serverName, playerCount);
        }
        plugin.getLogger().info("Updated player count for " + serverName + ": " + playerCount);
    }

    /**
     * Füllt die Ränder des GUIs mit Gray Stained Glass Panes
     */
    private void fillBorders(Inventory gui, ItemStack borderItem) {
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, borderItem);
        }
        for (int i = 36; i < 45; i++) {
            gui.setItem(i, borderItem);
        }
        for (int row = 1; row < 4; row++) {
            gui.setItem(row * 9, borderItem);
            gui.setItem(row * 9 + 8, borderItem);
        }
    }

    /**
     * Prüft ob ein Slot für Server-Items erlaubt ist
     */
    private boolean isValidServerSlot(int slot) {
        if (slot < 0 || slot >= 45) return false;
        if (slot < 9 || slot >= 36) return false;
        int column = slot % 9;
        if (column == 0 || column == 8) return false;
        return true;
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
        String guiTitle = event.getView().getTitle();
        String navigatorTitle = translateColorCodes(plugin.getConfig().getString("shortprefix", "") + "&7Select a &eServer");
        String smashTitle = translateColorCodes("&a&lSmash &7Serverübersicht");

        // Navigator-GUI Behandlung
        if (guiTitle.equals(navigatorTitle)) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return;
            }

            int slot = event.getSlot();

            // Smash-Item auf Slot 15 prüfen
            if (slot == 15 && clickedItem.getType() == Material.GOLDEN_SWORD) {
                openSmashGUI(player);
                return;
            }

            // Normale Server-Items
            String serverKey = serverSlots.get(slot);
            if (serverKey != null) {
                ConfigurationSection serverConfig = plugin.getConfig().getConfigurationSection("navigator.servers." + serverKey);
                if (serverConfig != null) {
                    String serverName = serverConfig.getString("server", "lobby");
                    connectToServer(player, serverName);
                }
            }
            return;
        }

        // Smash-GUI Behandlung
        if (guiTitle.equals(smashTitle)) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR ||
                    clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE || clickedItem.getType() == Material.BARRIER) {
                return;
            }

            int slot = event.getSlot();

            if (slot >= 9 && slot <= 26) { // Mittlere 2 Reihen
                int serverIndex = slot - 9;

                List<String> currentOnlineServers;
                synchronized (this) {
                    currentOnlineServers = new ArrayList<>(onlineSmashServers);
                }

                if (serverIndex < currentOnlineServers.size()) {
                    String serverName = currentOnlineServers.get(serverIndex);
                    connectToServer(player, serverName);
                    smashGUIs.remove(player);
                }
            }
        }
    }

    /**
     * Verbindet einen Spieler zu einem Server (nur Velocity)
     */
    private void connectToServer(Player player, String serverName) {
        plugin.getLogger().info("[Connect] " + player.getName() + " -> " + serverName);

        String connectMessage = translateColorCodes(
                plugin.getConfig().getString("messages.navigator.connecting",
                                "&aVerbinde dich mit &b%server%&a...")
                        .replace("%server%", serverName)
        );
        player.sendMessage(connectMessage);

        player.closeInventory();
        smashGUIs.remove(player);

        registerChannels();

        // Versuche beide Kanäle für Verbindung
        String[] channels = {"velocity:main", "bungeecord:main"};

        for (String channel : channels) {
            try {
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(b);
                out.writeUTF("Connect");
                out.writeUTF(serverName);
                out.close();

                player.sendPluginMessage(plugin, channel, b.toByteArray());
                plugin.getLogger().info("[Connect] Sent to " + serverName + " via " + channel);

            } catch (Exception e) {
                plugin.getLogger().warning("[Connect] Failed on " + channel + ": " + e.getMessage());
            }
        }
    }

    /**
     * Registriert Velocity-Kanäle
     */
    private void registerChannels() {
        try {
            if (!plugin.getServer().getMessenger().isOutgoingChannelRegistered(plugin, "velocity:main")) {
                plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "velocity:main");
            }
            if (!plugin.getServer().getMessenger().isOutgoingChannelRegistered(plugin, "bungeecord:main")) {
                plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "bungeecord:main");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to register channels: " + e.getMessage());
        }
    }

    private String translateColorCodes(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public void reloadServers() {
        loadServersFromConfig();
        initializeSmashServers();
    }

    public void cleanupPlayer(Player player) {
        smashGUIs.remove(player);
    }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        registerChannels();
    }

    /**
     * Debug-Methode: Zeigt aktuellen Server-Status
     */
    public void debugServerStatus() {
        plugin.getLogger().info("=== DEBUG SERVER STATUS ===");
        plugin.getLogger().info("Last response time: " + lastResponseTime);
        plugin.getLogger().info("Request in progress: " + requestInProgress);

        synchronized (this) {
            plugin.getLogger().info("Online servers: " + onlineSmashServers);
            plugin.getLogger().info("Server status map:");
            for (int i = 1; i <= 18; i++) {
                String serverName = "Smash-" + i;
                boolean isOnline = serverOnlineStatus.getOrDefault(serverName, false);
                int playerCount = serverPlayerCount.getOrDefault(serverName, 0);
                if (isOnline || playerCount > 0) {
                    plugin.getLogger().info("  " + serverName + ": online=" + isOnline + ", players=" + playerCount);
                }
            }
        }
        plugin.getLogger().info("=== END DEBUG ===");
    }

    /**
     * Manueller Test für Server-Erkennung
     */
    public void forceServerCheck() {
        plugin.getLogger().info("[Force Check] Starting manual server check...");

        if (requestInProgress) {
            plugin.getLogger().info("[Force Check] Request already in progress, aborting");
            return;
        }

        requestInProgress = true;

        // Erst alle Server als offline markieren
        synchronized (this) {
            for (int i = 1; i <= 18; i++) {
                serverOnlineStatus.put("Smash-" + i, false);
                serverPlayerCount.put("Smash-" + i, 0);
            }
            updateOnlineServersList();
        }

        // Dann direkte PlayerCount-Anfragen für alle Server
        requestAllPlayerCountsDirect();

        // Nach 2 Sekunden GUIs aktualisieren
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            updateSmashGUIs();
            debugServerStatus();
            requestInProgress = false;
            plugin.getLogger().info("[Force Check] Manual server check completed");
        }, 40L);
    }
}