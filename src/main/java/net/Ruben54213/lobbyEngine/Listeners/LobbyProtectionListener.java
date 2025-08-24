package net.Ruben54213.lobbyEngine.Listeners;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class LobbyProtectionListener implements Listener {

    private final JavaPlugin plugin;

    public LobbyProtectionListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.isOp() || player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        // Deaktiviert generellen Schaden inkl. Fallschaden und Void-Schaden
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVoidFall(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.isDead()) return;

        // Trigger nur, wenn von >=0 nach <0 gewechselt wird, um Spam zu vermeiden
        if (event.getFrom() != null && event.getTo() != null) {
            double fromY = event.getFrom().getY();
            double toY = event.getTo().getY();
            if (fromY >= 0.0 && toY < 0.0) {
                // Führe /spawn als Spieler aus (Async-sicher im nächsten Tick)
                plugin.getServer().getScheduler().runTask(plugin, () -> player.performCommand("spawn"));
            }
        }
    }
}