package net.Ruben54213.lobbyEngine;

import net.Ruben54213.lobbyEngine.Commands.EntitySpawnCommand;
import net.Ruben54213.lobbyEngine.Commands.SetSpawnCommand;
import net.Ruben54213.lobbyEngine.Commands.SpawnCommand;
import net.Ruben54213.lobbyEngine.Listeners.BlockDecayListener;
import net.Ruben54213.lobbyEngine.Listeners.BuildProtListener;
import net.Ruben54213.lobbyEngine.Listeners.EntitySpawnListener;
import net.Ruben54213.lobbyEngine.Listeners.InventoryProtListener;
import net.Ruben54213.lobbyEngine.Listeners.PlayerJoinListener;
import net.Ruben54213.lobbyEngine.Utility.SpawnManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class LobbyEngine extends JavaPlugin {

    private SpawnManager spawnManager;

    @Override
    public void onEnable() {
        // Plugin startup logic

        // Config laden/erstellen
        saveDefaultConfig();

        // SpawnManager initialisieren
        spawnManager = new SpawnManager(this);

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
        PlayerJoinListener playerJoinListener = new PlayerJoinListener(this, spawnManager);
        playerJoinListener.register();

        getLogger().info("All listeners have been registered!");
    }

    /**
     * Registriert alle Commands
     */
    private void registerCommands() {
        // Entity-Spawn Command
        EntitySpawnListener entitySpawnListener = new EntitySpawnListener(this);
        EntitySpawnCommand entitySpawnCommand = new EntitySpawnCommand(this, entitySpawnListener);
        getCommand("entityspawn").setExecutor(entitySpawnCommand);

        // Spawn Commands
        SpawnCommand spawnCommand = new SpawnCommand(this, spawnManager);
        getCommand("spawn").setExecutor(spawnCommand);

        SetSpawnCommand setSpawnCommand = new SetSpawnCommand(this, spawnManager);
        getCommand("setspawn").setExecutor(setSpawnCommand);

        getLogger().info("All commands have been registered!");
    }
}