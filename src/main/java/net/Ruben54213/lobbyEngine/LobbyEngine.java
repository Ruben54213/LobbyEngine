package net.Ruben54213.lobbyEngine;

import net.Ruben54213.lobbyEngine.Commands.EntitySpawnCommand;
import net.Ruben54213.lobbyEngine.Commands.LobbyCommand;
import net.Ruben54213.lobbyEngine.Commands.SetSpawnCommand;
import net.Ruben54213.lobbyEngine.Commands.SpawnCommand;
import net.Ruben54213.lobbyEngine.Listeners.BlockDecayListener;
import net.Ruben54213.lobbyEngine.Listeners.BuildProtListener;
import net.Ruben54213.lobbyEngine.Listeners.EntitySpawnListener;
import net.Ruben54213.lobbyEngine.Listeners.InventoryProtListener;
import net.Ruben54213.lobbyEngine.Listeners.NavigatorListener;
import net.Ruben54213.lobbyEngine.Listeners.PlayerJoinListener;
import net.Ruben54213.lobbyEngine.Utility.CompassManager;
import net.Ruben54213.lobbyEngine.Utility.ServerNavigatorGUI;
import net.Ruben54213.lobbyEngine.Utility.SpawnManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class LobbyEngine extends JavaPlugin {

    private SpawnManager spawnManager;
    private CompassManager compassManager;
    private ServerNavigatorGUI navigatorGUI;

    @Override
    public void onEnable() {
        // Plugin startup logic

        // Config laden/erstellen
        saveDefaultConfig();

        // Config neu laden um sicherzustellen dass alle Werte geladen sind
        reloadConfig();

        // SpawnManager initialisieren
        spawnManager = new SpawnManager(this);

        // CompassManager initialisieren
        compassManager = new CompassManager(this);

        // ServerNavigatorGUI initialisieren
        navigatorGUI = new ServerNavigatorGUI(this);

        // Listener registrieren
        registerListeners();

        // Commands registrieren
        registerCommands();

        // Startup-Nachricht
        getLogger().info("LobbyEngine has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("LobbyEngine has been disabled!");
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

        // Player-Join Listener
        PlayerJoinListener playerJoinListener = new PlayerJoinListener(this, spawnManager, compassManager);
        playerJoinListener.register();

        // Navigator Listener
        NavigatorListener navigatorListener = new NavigatorListener(this, compassManager, navigatorGUI);
        navigatorListener.register();

        // Navigator GUI Listener registrieren
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
        if (getCommand("lobby") != null) {
            LobbyCommand lobbyCommand = new LobbyCommand(this, navigatorGUI);
            getCommand("lobby").setExecutor(lobbyCommand);
            getCommand("lobby").setTabCompleter(lobbyCommand);
        } else {
            getLogger().warning("Command 'lobby' not found in plugin.yml!");
        }

        getLogger().info("Commands have been registered!");
    }
}