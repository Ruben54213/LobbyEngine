package net.Ruben54213.lobbyEngine;

import net.Ruben54213.lobbyEngine.Commands.*;
import net.Ruben54213.lobbyEngine.Listeners.BlockDecayListener;
import net.Ruben54213.lobbyEngine.Listeners.BuildProtListener;
import net.Ruben54213.lobbyEngine.Listeners.CosmeticsListener;
import net.Ruben54213.lobbyEngine.Listeners.EntitySpawnListener;
import net.Ruben54213.lobbyEngine.Listeners.InventoryProtListener;
import net.Ruben54213.lobbyEngine.Listeners.NavigatorListener;
import net.Ruben54213.lobbyEngine.Listeners.PlayerJoinListener;
import net.Ruben54213.lobbyEngine.Utility.*;
import net.Ruben54213.lobbyEngine.Listeners.PlayerQuitListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class LobbyEngine extends JavaPlugin {

    private SpawnManager spawnManager;
    private CompassManager compassManager;
    private ServerNavigatorGUI navigatorGUI;
    private CosmeticsManager cosmeticsManager;
    private CosmeticsFeatures cosmeticsFeatures;
    private CosmeticsGUI cosmeticsGUI;
    private LobbyProtectionManager protectionManager;
    private PlayerInvulnerabilityManager invulnerabilityManager;
    private PlayerInventoryManager inventoryManager;
    private PlayerHeadManager playerHeadManager;
    private LobbyManager lobbyManager;

    @Override
    public void onEnable() {
        // Plugin startup logic

        // Config laden/erstellen
        saveDefaultConfig();

        // Config neu laden um sicherzustellen dass alle Werte geladen sind
        reloadConfig();

        // Manager initialisieren
        initializeManagers();

        // Listener registrieren
        registerListeners();

        // Commands registrieren
        registerCommands();

        // Startup-Nachricht
        getLogger().info("LobbyEngine has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        // Cleanup beim Shutdown
        if (invulnerabilityManager != null) {
            invulnerabilityManager.removeAllInvulnerability();
        }

        if (inventoryManager != null) {
            inventoryManager.clearAllStoredInventories();
        }

        // Plugin shutdown logic
        getLogger().info("LobbyEngine has been disabled!");
    }

    /**
     * Initialisiert alle Manager
     */
    private void initializeManagers() {
        // SpawnManager initialisieren
        spawnManager = new SpawnManager(this);

        // CompassManager initialisieren
        compassManager = new CompassManager(this);

        // ServerNavigatorGUI initialisieren
        navigatorGUI = new ServerNavigatorGUI(this);

        // CosmeticsManager initialisieren
        cosmeticsManager = new CosmeticsManager(this);

        // CosmeticsFeatures initialisieren
        cosmeticsFeatures = new CosmeticsFeatures(this);

        // CosmeticsGUI initialisieren (mit CosmeticsFeatures)
        cosmeticsGUI = new CosmeticsGUI(this, cosmeticsFeatures);

        // LobbyProtectionManager initialisieren
        protectionManager = new LobbyProtectionManager(this);

        // PlayerInvulnerabilityManager initialisieren
        invulnerabilityManager = new PlayerInvulnerabilityManager(this);

        // PlayerInventoryManager initialisieren
        inventoryManager = new PlayerInventoryManager(this, compassManager, cosmeticsManager);

        // PlayerHeadManager initialisieren
        playerHeadManager = new PlayerHeadManager(this);

        // LobbyManager initialisieren
        lobbyManager = new LobbyManager(this);

        // WICHTIG: Manager-Referenzen setzen NACH der Initialisierung
        inventoryManager.setPlayerHeadManager(playerHeadManager);
        inventoryManager.setLobbyManager(lobbyManager);

        getLogger().info("All managers have been initialized!");
    }

    /**
     * Registriert alle Event-Listener
     */
    private void registerListeners() {

        // Build-Protection Listener
        BuildProtListener buildProtListener = new BuildProtListener(this);
        buildProtListener.register();

        // Inventory-Protection Listener
        InventoryProtListener inventoryProtListener = new InventoryProtListener(this);
        inventoryProtListener.register();

        // Entity-Spawn Listener
        EntitySpawnListener entitySpawnListener = new EntitySpawnListener(this);
        entitySpawnListener.register();

        // Block-Decay Listener
        BlockDecayListener blockDecayListener = new BlockDecayListener(this);
        blockDecayListener.register();

        // Player-Join Listener (mit CosmeticsManager UND LobbyManager)
        PlayerJoinListener playerJoinListener = new PlayerJoinListener(this, spawnManager, compassManager, cosmeticsManager, lobbyManager);
        playerJoinListener.register();

        // Navigator Listener
        NavigatorListener navigatorListener = new NavigatorListener(this, compassManager, navigatorGUI);
        navigatorListener.register();

        // PlayerQuitListener mit allen notwendigen Managern
        PlayerQuitListener playerQuitListener = new PlayerQuitListener(this, cosmeticsFeatures, protectionManager, invulnerabilityManager, inventoryManager);
        playerQuitListener.register();

        // Cosmetics Listener
        CosmeticsListener cosmeticsListener = new CosmeticsListener(this, cosmeticsManager, cosmeticsGUI);
        cosmeticsListener.register();

        // Alle Manager registrieren
        protectionManager.register();
        invulnerabilityManager.register();
        inventoryManager.register();
        playerHeadManager.register();
        lobbyManager.register();
        cosmeticsFeatures.register();
        cosmeticsGUI.register();
        navigatorGUI.register();

        getLogger().info("All listeners have been registered!");
    }

    /**
     * Registriert alle Commands
     */
    private void registerCommands() {
        // Entity-Spawn Command
        if (getCommand("entityspawn") != null) {
            EntitySpawnListener entitySpawnListener = new EntitySpawnListener(this);
            EntitySpawnCommand entitySpawnCommand = new EntitySpawnCommand(this, entitySpawnListener);
            getCommand("entityspawn").setExecutor(entitySpawnCommand);
        } else {
            getLogger().warning("Command 'entityspawn' not found in plugin.yml!");
        }

        // Spawn Commands
        if (getCommand("spawn") != null) {
            SpawnCommand spawnCommand = new SpawnCommand(this, spawnManager);
            getCommand("spawn").setExecutor(spawnCommand);
        } else {
            getLogger().warning("Command 'spawn' not found in plugin.yml!");
        }

        if (getCommand("setspawn") != null) {
            SetSpawnCommand setSpawnCommand = new SetSpawnCommand(this, spawnManager);
            getCommand("setspawn").setExecutor(setSpawnCommand);
        } else {
            getLogger().warning("Command 'setspawn' not found in plugin.yml!");
        }

        // Lobby Command
        if (getCommand("lobbies") != null) {
            LobbiesCommand lobbiesCommand = new LobbiesCommand(this, lobbyManager);
            getCommand("lobbies").setExecutor(lobbiesCommand);
            getCommand("lobbies").setTabCompleter(lobbiesCommand);
        } else {
            getLogger().warning("Command 'lobbies' not found in plugin.yml!");
        }

        getLogger().info("Commands have been registered!");
    }

    // ==================== GETTER METHODEN ====================

    /**
     * Gibt den SpawnManager zurück
     */
    public SpawnManager getSpawnManager() {
        return spawnManager;
    }

    /**
     * Gibt den CompassManager zurück
     */
    public CompassManager getCompassManager() {
        return compassManager;
    }

    /**
     * Gibt den CosmeticsManager zurück
     */
    public CosmeticsManager getCosmeticsManager() {
        return cosmeticsManager;
    }

    /**
     * Gibt den PlayerInvulnerabilityManager zurück
     */
    public PlayerInvulnerabilityManager getInvulnerabilityManager() {
        return invulnerabilityManager;
    }

    /**
     * Gibt den PlayerInventoryManager zurück
     */
    public PlayerInventoryManager getInventoryManager() {
        return inventoryManager;
    }

    /**
     * Gibt den LobbyProtectionManager zurück
     */
    public LobbyProtectionManager getProtectionManager() {
        return protectionManager;
    }

    /**
     * Gibt den PlayerHeadManager zurück
     */
    public PlayerHeadManager getPlayerHeadManager() {
        return playerHeadManager;
    }

    /**
     * Gibt den LobbyManager zurück
     */
    public LobbyManager getLobbyManager() {
        return lobbyManager;
    }
}