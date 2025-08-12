package net.Ruben54213.lobbyEngine.Listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Verhindert das Spawnen von Entities (Mobs, Tiere, etc.)
 */
public class EntitySpawnListener implements Listener {

    private final JavaPlugin plugin;
    private boolean entitySpawnBlocked;

    public EntitySpawnListener(JavaPlugin plugin) {
        this.plugin = plugin;
        // Standard: Entity-Spawn ist blockiert
        this.entitySpawnBlocked = plugin.getConfig().getBoolean("entityspawn.blocked", true);
    }

    /**
     * Verhindert das Spawnen von Entities wenn blockiert
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        // Wenn Entity-Spawn nicht blockiert ist, erlaube alle Spawns
        if (!entitySpawnBlocked) {
            return;
        }

        // Erlaube Spieler-Spawns immer
        if (event.getEntityType() == EntityType.PLAYER) {
            return;
        }

        // Blockiere alle anderen Entities
        event.setCancelled(true);
    }

    /**
     * Togglet den Entity-Spawn Status
     *
     * @return true wenn jetzt blockiert, false wenn erlaubt
     */
    public boolean toggleEntitySpawn() {
        entitySpawnBlocked = !entitySpawnBlocked;

        // In Config speichern
        plugin.getConfig().set("entityspawn.blocked", entitySpawnBlocked);
        plugin.saveConfig();

        return entitySpawnBlocked;
    }

    /**
     * Prüft ob Entity-Spawn blockiert ist
     *
     * @return true wenn blockiert
     */
    public boolean isEntitySpawnBlocked() {
        return entitySpawnBlocked;
    }

    /**
     * Setzt den Entity-Spawn Status
     *
     * @param blocked true um zu blockieren, false um zu erlauben
     */
    public void setEntitySpawnBlocked(boolean blocked) {
        this.entitySpawnBlocked = blocked;
        plugin.getConfig().set("entityspawn.blocked", blocked);
        plugin.saveConfig();
    }

    /**
     * Registriert diesen Listener beim Plugin
     */
    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
}