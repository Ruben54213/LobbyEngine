package net.Ruben54213.lobbyEngine.Utility;

import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Verwaltet alle Cosmetics-Features (Partikel, Effekte, Gadgets)
 */
public class CosmeticsFeatures implements Listener {

    private final JavaPlugin plugin;

    // Tracking-Maps für aktive Features
    private final Set<UUID> activeParticles = new HashSet<>();
    private final Set<UUID> activeEffects = new HashSet<>();
    private final Map<UUID, String> playerParticleTypes = new HashMap<>();
    private final Map<UUID, String> playerEffectTypes = new HashMap<>();
    private final Map<UUID, BukkitTask> particleTasks = new HashMap<>();
    private final Map<UUID, BukkitTask> effectTasks = new HashMap<>();
    private final Map<UUID, Location> grappleHooks = new HashMap<>();
    private final Set<UUID> doubleJumpReady = new HashSet<>();

    public CosmeticsFeatures(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // ==================== PARTIKEL FEATURES ====================

    /**
     * Aktiviert Partikel-Effekte für einen Spieler
     */
    public void activateParticles(Player player, String particleType) {
        UUID uuid = player.getUniqueId();

        // Alte Partikel stoppen
        stopParticles(player);

        // Neue Partikel starten
        activeParticles.add(uuid);
        playerParticleTypes.put(uuid, particleType);

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    return;
                }
                spawnParticleEffect(player, particleType);
            }
        }.runTaskTimer(plugin, 0L, 5L); // Alle 0.25 Sekunden

        particleTasks.put(uuid, task);
        player.sendMessage(translateColorCodes("&d&lParticles &7activated: &f" + formatParticleName(particleType)));
    }

    /**
     * Stoppt Partikel-Effekte für einen Spieler
     */
    public void stopParticles(Player player) {
        UUID uuid = player.getUniqueId();
        activeParticles.remove(uuid);
        playerParticleTypes.remove(uuid);

        BukkitTask task = particleTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
    }

    /**
     * Spawnt spezifische Partikel-Effekte
     */
    private void spawnParticleEffect(Player player, String particleType) {
        Location loc = player.getLocation().add(0, 1, 0);
        World world = loc.getWorld();

        switch (particleType.toLowerCase()) {
            case "heart":
                world.spawnParticle(Particle.HEART, loc, 3, 0.5, 0.5, 0.5, 0.1);
                break;
            case "ender":
                world.spawnParticle(Particle.PORTAL, loc, 10, 0.5, 1.0, 0.5, 0.1);
                break;
            case "lava":
                world.spawnParticle(Particle.LAVA, loc, 5, 0.3, 0.3, 0.3, 0.0);
                break;
            case "water":
                world.spawnParticle(Particle.WATER_SPLASH, loc, 8, 0.5, 0.5, 0.5, 0.1);
                break;
            case "fire":
                world.spawnParticle(Particle.FLAME, loc, 6, 0.3, 0.3, 0.3, 0.02);
                break;
            case "raincloud":
                spawnRainCloudEffect(player);
                break;
        }
    }

    /**
     * Spawnt Regenwolken-Effekt über dem Kopf
     */
    private void spawnRainCloudEffect(Player player) {
        Location cloudLoc = player.getLocation().add(0, 2.5, 0);
        World world = cloudLoc.getWorld();

        // Wolken-Partikel
        world.spawnParticle(Particle.CLOUD, cloudLoc, 8, 1.0, 0.2, 1.0, 0.02);

        // Regen-Partikel darunter
        for (int i = 0; i < 5; i++) {
            Location rainLoc = cloudLoc.clone().add(
                    (Math.random() - 0.5) * 2,
                    -Math.random() * 2,
                    (Math.random() - 0.5) * 2
            );
            world.spawnParticle(Particle.WATER_DROP, rainLoc, 1, 0, 0, 0, 0);
        }
    }

    // ==================== EFFEKT FEATURES ====================

    /**
     * Aktiviert spezielle Effekte für einen Spieler
     */
    public void activateEffect(Player player, String effectType) {
        UUID uuid = player.getUniqueId();

        // Alte Effekte stoppen
        stopEffects(player);

        // Neue Effekte starten
        activeEffects.add(uuid);
        playerEffectTypes.put(uuid, effectType);

        switch (effectType.toLowerCase()) {
            case "fly":
                activateFly(player);
                break;
            case "doublejump":
                activateDoubleJump(player);
                break;
            case "jumpboost":
                activateJumpBoost(player);
                break;
            case "nightvision":
                activateNightVision(player);
                break;
            case "speed":
                activateSpeed(player);
                break;
        }

        player.sendMessage(translateColorCodes("&a&lEffect &7activated: &f" + formatEffectName(effectType)));
    }

    /**
     * Stoppt alle Effekte für einen Spieler
     */
    public void stopEffects(Player player) {
        UUID uuid = player.getUniqueId();
        activeEffects.remove(uuid);
        playerEffectTypes.remove(uuid);
        doubleJumpReady.remove(uuid);

        // Potion-Effekte entfernen
        player.removePotionEffect(PotionEffectType.JUMP);
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        player.removePotionEffect(PotionEffectType.SPEED);

        // Fly deaktivieren
        player.setAllowFlight(false);
        player.setFlying(false);

        BukkitTask task = effectTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
    }

    private void activateFly(Player player) {
        player.setAllowFlight(true);
        player.setFlying(true);
    }

    private void activateDoubleJump(Player player) {
        doubleJumpReady.add(player.getUniqueId());
    }

    private void activateJumpBoost(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 2, false, false));
    }

    private void activateNightVision(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
    }

    private void activateSpeed(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
    }

    // ==================== GADGET FEATURES ====================

    /**
     * Gibt einem Spieler ein Gadget
     */
    public void giveGadget(Player player, String gadgetType) {
        // Vorheriges Gadget entfernen
        removeGadget(player);

        ItemStack gadget = createGadgetItem(gadgetType);
        if (gadget != null) {
            // Gadget in Slot 3 setzen (4. Slot der Hotbar, neben Cosmetics)
            player.getInventory().setItem(3, gadget);
            player.sendMessage(translateColorCodes("&e&lGadget &7received: &f" + formatGadgetName(gadgetType)));
        }
    }

    /**
     * Entfernt das aktuelle Gadget vom Spieler
     */
    public void removeGadget(Player player) {
        // Slot 3 leeren (wo Gadgets sind)
        player.getInventory().setItem(3, null);
    }

    /**
     * Erstellt Gadget-Items
     */
    private ItemStack createGadgetItem(String gadgetType) {
        ItemStack item = null;
        ItemMeta meta;

        switch (gadgetType.toLowerCase()) {
            case "teleporter":
                item = new ItemStack(Material.ENDER_EYE);
                meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(translateColorCodes("&5&lTeleporter"));
                    item.setItemMeta(meta);
                }
                break;
            case "grapplehook":
                item = new ItemStack(Material.FISHING_ROD);
                meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(translateColorCodes("&2&lGrapple Hook"));
                    item.setItemMeta(meta);
                }
                break;
            case "lovebow":
                item = new ItemStack(Material.BOW);
                meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(translateColorCodes("&d&lLove Bow"));
                    item.setItemMeta(meta);
                }
                break;
        }

        return item;
    }

    // ==================== EVENT HANDLERS ====================

    /**
     * Behandelt Double Jump
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (doubleJumpReady.contains(uuid) && !player.isOnGround() && event.isFlying()) {
            event.setCancelled(true);
            player.setAllowFlight(false);
            player.setFlying(false);

            // Double Jump ausführen
            Vector velocity = player.getVelocity();
            velocity.setY(0.8);
            player.setVelocity(velocity);

            // Effekte
            player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 10, 0.5, 0.1, 0.5, 0.1);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 2.0f);
        }
    }

    /**
     * Double Jump Reset beim Landen
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (doubleJumpReady.contains(uuid) && player.isOnGround()) {
            player.setAllowFlight(true);
        }
    }

    /**
     * Behandelt Gadget-Interaktionen
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !item.hasItemMeta()) return;

        String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());

        if (displayName.equals("Teleporter")) {
            handleTeleporter(player);
            event.setCancelled(true);
        }
    }

    /**
     * Behandelt Teleporter-Gadget
     */
    private void handleTeleporter(Player player) {
        RayTraceResult result = player.rayTraceBlocks(50);
        if (result != null && result.getHitBlock() != null) {
            Location teleportLoc = result.getHitPosition().toLocation(player.getWorld());
            teleportLoc.setY(teleportLoc.getY() + 1); // Über dem Block spawnen

            // Effekte am Start
            player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.1);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

            // Teleportation
            player.teleport(teleportLoc);

            // Effekte am Ziel
            player.getWorld().spawnParticle(Particle.PORTAL, teleportLoc, 20, 0.5, 1, 0.5, 0.1);
            player.playSound(teleportLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);
        } else {
            player.sendMessage(translateColorCodes("&c&lTeleporter &7failed: No valid target!"));
        }
    }

    /**
     * Behandelt Grapple Hook - Aktualisiert für Bodenerkennung und automatisches Einziehen
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        ItemStack rod = player.getInventory().getItemInMainHand();

        if (rod.hasItemMeta() && ChatColor.stripColor(rod.getItemMeta().getDisplayName()).equals("Grapple Hook")) {
            // Nur wenn der Hook auf dem Boden ist (IN_GROUND State)
            if (event.getState() == PlayerFishEvent.State.IN_GROUND) {
                Location hookLoc = event.getHook().getLocation();

                // Zusätzliche Prüfung: Ist unter dem Hook ein solider Block?
                Location blockBelow = hookLoc.clone().subtract(0, 1, 0);
                if (!blockBelow.getBlock().getType().isSolid()) {
                    player.sendMessage(translateColorCodes("&c&lGrapple Hook &7failed: Hook must be on solid ground!"));
                    return;
                }

                grappleHooks.put(player.getUniqueId(), hookLoc);

                // Spieler zum Hook ziehen
                Vector direction = hookLoc.toVector().subtract(player.getLocation().toVector());
                direction.normalize().multiply(2.0);
                direction.setY(Math.max(direction.getY(), 0.5)); // Mindest-Y für Sprung

                player.setVelocity(direction);
                player.playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 1.0f, 0.8f);

                // Angel automatisch einziehen nach erfolgreicher Verwendung
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // Prüfe ob der Spieler noch die Grapple Hook hält
                        ItemStack currentRod = player.getInventory().getItemInMainHand();
                        if (currentRod.hasItemMeta() &&
                                ChatColor.stripColor(currentRod.getItemMeta().getDisplayName()).equals("Grapple Hook")) {

                            // Simuliere das Einziehen der Angel
                            if (event.getHook() != null && !event.getHook().isDead()) {
                                event.getHook().remove();
                            }
                        }
                    }
                }.runTaskLater(plugin, 5L); // Nach 0.25 Sekunden einziehen

                event.setCancelled(true);
            } else if (event.getState() == PlayerFishEvent.State.REEL_IN) {
                // Verhindere normales Einziehen wenn Hook nicht auf dem Boden ist
                player.sendMessage(translateColorCodes("&c&lGrapple Hook &7failed: Hook must land on solid ground first!"));
                event.setCancelled(true);
            }
        }
    }

    /**
     * Behandelt Love Bow
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onArrowHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Arrow && event.getEntity() instanceof Player) {
            Arrow arrow = (Arrow) event.getDamager();
            Player target = (Player) event.getEntity();

            if (arrow.getShooter() instanceof Player) {
                Player shooter = (Player) arrow.getShooter();
                ItemStack bow = shooter.getInventory().getItemInMainHand();

                if (bow.hasItemMeta() && ChatColor.stripColor(bow.getItemMeta().getDisplayName()).equals("Love Bow")) {
                    event.setCancelled(true); // Kein Schaden

                    // Herz-Effekte
                    target.getWorld().spawnParticle(Particle.HEART, target.getLocation().add(0, 2, 0), 10, 1, 1, 1, 0.1);
                    target.playSound(target.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 2.0f);

                    // Nachricht
                    target.sendMessage(translateColorCodes("&d&l♥ &fYou received love from &d" + shooter.getName() + "&f! &d&l♥"));

                    // Pfeil entfernen
                    arrow.remove();
                }
            }
        }
    }

    /**
     * Entfernt Pfeile automatisch
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getEntity();

            if (arrow.getShooter() instanceof Player) {
                Player shooter = (Player) arrow.getShooter();
                ItemStack bow = shooter.getInventory().getItemInMainHand();

                if (bow.hasItemMeta() && ChatColor.stripColor(bow.getItemMeta().getDisplayName()).equals("Love Bow")) {
                    // Pfeil nach kurzer Zeit entfernen
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            arrow.remove();
                        }
                    }.runTaskLater(plugin, 20L); // 1 Sekunde
                }
            }
        }
    }

    // ==================== UTILITY METHODEN ====================

    /**
     * Bereinigt Spieler-Daten beim Disconnect
     */
    public void cleanupPlayer(Player player) {
        stopParticles(player);
        stopEffects(player);
        removeGadget(player);
        grappleHooks.remove(player.getUniqueId());
        doubleJumpReady.remove(player.getUniqueId());
    }

    /**
     * Formatiert Partikel-Namen für Anzeige
     */
    private String formatParticleName(String type) {
        return type.substring(0, 1).toUpperCase() + type.substring(1).toLowerCase() + " Particles";
    }

    /**
     * Formatiert Effekt-Namen für Anzeige
     */
    private String formatEffectName(String type) {
        switch (type.toLowerCase()) {
            case "doublejump": return "Double Jump";
            case "jumpboost": return "Jump Boost";
            case "nightvision": return "Night Vision";
            default: return type.substring(0, 1).toUpperCase() + type.substring(1).toLowerCase();
        }
    }

    /**
     * Formatiert Gadget-Namen für Anzeige
     */
    private String formatGadgetName(String type) {
        switch (type.toLowerCase()) {
            case "grapplehook": return "Grapple Hook";
            case "lovebow": return "Love Bow";
            default: return type.substring(0, 1).toUpperCase() + type.substring(1).toLowerCase();
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
    }
}