package net.Ruben54213.lobbyEngine.Utility;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Verwaltet Spawn-Location und Effekte
 */
public class SpawnManager {

    private final JavaPlugin plugin;
    private Location spawnLocation;

    public SpawnManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadSpawnFromConfig();
    }

    /**
     * Lädt die Spawn-Location aus der Config
     */
    private void loadSpawnFromConfig() {
        if (plugin.getConfig().contains("spawn.world")) {
            String worldName = plugin.getConfig().getString("spawn.world");

            // Prüfe ob worldName nicht null oder leer ist
            if (worldName == null || worldName.isEmpty()) {
                plugin.getLogger().info("No spawn world configured yet.");
                return;
            }

            double x = plugin.getConfig().getDouble("spawn.x");
            double y = plugin.getConfig().getDouble("spawn.y");
            double z = plugin.getConfig().getDouble("spawn.z");
            float yaw = (float) plugin.getConfig().getDouble("spawn.yaw");
            float pitch = (float) plugin.getConfig().getDouble("spawn.pitch");

            World world = plugin.getServer().getWorld(worldName);
            if (world != null) {
                spawnLocation = new Location(world, x, y, z, yaw, pitch);
                plugin.getLogger().info("Spawn location loaded for world: " + worldName);
            } else {
                plugin.getLogger().warning("Spawn world '" + worldName + "' not found!");
            }
        } else {
            plugin.getLogger().info("No spawn location configured yet. Use /setspawn to set one.");
        }
    }

    /**
     * Setzt die Spawn-Location
     */
    public void setSpawn(Location location) {
        this.spawnLocation = location;

        // In Config speichern
        plugin.getConfig().set("spawn.world", location.getWorld().getName());
        plugin.getConfig().set("spawn.x", location.getX());
        plugin.getConfig().set("spawn.y", location.getY());
        plugin.getConfig().set("spawn.z", location.getZ());
        plugin.getConfig().set("spawn.yaw", location.getYaw());
        plugin.getConfig().set("spawn.pitch", location.getPitch());
        plugin.saveConfig();
    }

    /**
     * Teleportiert einen Spieler zum Spawn mit Effekten
     */
    public boolean teleportToSpawn(Player player) {
        if (spawnLocation == null) {
            return false;
        }

        // Teleportation
        player.teleport(spawnLocation);

        // Effekte abspielen
        playSpawnEffects(player);

        return true;
    }

    /**
     * Spielt die geilen Spawn-Effekte ab
     */
    public void playSpawnEffects(Player player) {
        if (spawnLocation == null) return;

        Location effectLocation = spawnLocation.clone().add(0, 1, 0);

        // GEILE SOUNDS aus der Config!
        String sound1 = plugin.getConfig().getString("spawn.sounds.teleport", "ENTITY_ENDERMAN_TELEPORT");
        String sound2 = plugin.getConfig().getString("spawn.sounds.levelup", "ENTITY_PLAYER_LEVELUP");
        String sound3 = plugin.getConfig().getString("spawn.sounds.beacon", "BLOCK_BEACON_ACTIVATE");

        float volume1 = (float) plugin.getConfig().getDouble("spawn.sounds.volume1", 1.0);
        float volume2 = (float) plugin.getConfig().getDouble("spawn.sounds.volume2", 0.7);
        float volume3 = (float) plugin.getConfig().getDouble("spawn.sounds.volume3", 0.5);

        float pitch1 = (float) plugin.getConfig().getDouble("spawn.sounds.pitch1", 1.0);
        float pitch2 = (float) plugin.getConfig().getDouble("spawn.sounds.pitch2", 1.5);
        float pitch3 = (float) plugin.getConfig().getDouble("spawn.sounds.pitch3", 2.0);

        try {
            player.playSound(effectLocation, Sound.valueOf(sound1), volume1, pitch1);
            player.playSound(effectLocation, Sound.valueOf(sound2), volume2, pitch2);
            player.playSound(effectLocation, Sound.valueOf(sound3), volume3, pitch3);
        } catch (IllegalArgumentException e) {
            // Fallback auf Standard-Sounds falls ungültiger Sound in Config
            player.playSound(effectLocation, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            player.playSound(effectLocation, Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.5f);
            player.playSound(effectLocation, Sound.BLOCK_BEACON_ACTIVATE, 0.5f, 2.0f);
        }

        // KRASSE PARTIKEL RUND UM DEN SPAWN!
        spawnParticleCircle(effectLocation, Particle.PORTAL, 30, 2.0, 0.5);
        spawnParticleCircle(effectLocation, Particle.ENCHANTMENT_TABLE, 20, 1.5, 1.0);
        spawnParticleCircle(effectLocation, Particle.END_ROD, 15, 1.0, 1.5);

        // Zentrale Explosion von Partikeln
        effectLocation.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, effectLocation, 50, 0.5, 1.0, 0.5, 0.1);
        effectLocation.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, effectLocation, 20, 1.0, 0.5, 1.0, 0.0);
        effectLocation.getWorld().spawnParticle(Particle.TOTEM, effectLocation, 25, 0.8, 1.2, 0.8, 0.1);

        // Spirale nach oben
        spawnParticleSpiral(effectLocation, Particle.DRAGON_BREATH, 15, 2.0, 3.0);
    }

    /**
     * Spawnt Partikel in einem Kreis um eine Location
     */
    private void spawnParticleCircle(Location center, Particle particle, int count, double radius, double height) {
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            double y = center.getY() + height;

            Location particleLocation = new Location(center.getWorld(), x, y, z);
            center.getWorld().spawnParticle(particle, particleLocation, 1, 0.1, 0.1, 0.1, 0.0);
        }
    }

    /**
     * Spawnt eine Partikel-Spirale nach oben
     */
    private void spawnParticleSpiral(Location center, Particle particle, int count, double radius, double height) {
        for (int i = 0; i < count; i++) {
            double progress = (double) i / count;
            double angle = progress * 4 * Math.PI; // 2 komplette Umdrehungen
            double currentRadius = radius * (1 - progress * 0.5); // Radius wird kleiner

            double x = center.getX() + currentRadius * Math.cos(angle);
            double z = center.getZ() + currentRadius * Math.sin(angle);
            double y = center.getY() + progress * height;

            Location particleLocation = new Location(center.getWorld(), x, y, z);
            center.getWorld().spawnParticle(particle, particleLocation, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    /**
     * Prüft ob ein Spawn gesetzt ist
     */
    public boolean hasSpawn() {
        return spawnLocation != null;
    }

    /**
     * Gibt die Spawn-Location zurück
     */
    public Location getSpawnLocation() {
        return spawnLocation;
    }
}