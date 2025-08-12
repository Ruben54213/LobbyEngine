package net.Ruben54213.lobbyEngine.Commands;

import net.Ruben54213.lobbyEngine.Listeners.EntitySpawnListener;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Command zum Togglen des Entity-Spawns
 */
public class EntitySpawnCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final EntitySpawnListener entitySpawnListener;

    public EntitySpawnCommand(JavaPlugin plugin, EntitySpawnListener entitySpawnListener) {
        this.plugin = plugin;
        this.entitySpawnListener = entitySpawnListener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Permission Check
        if (!sender.hasPermission("lobbyengine.entityspawn")) {
            String noPermMessage = translateColorCodes(
                    plugin.getConfig().getString("prefix", "") +
                            plugin.getConfig().getString("messages.noperm", "&cYou don't have permission!")
            );
            sender.sendMessage(noPermMessage);
            return true;
        }

        // Toggle Entity-Spawn
        boolean nowBlocked = entitySpawnListener.toggleEntitySpawn();

        // Nachrichten aus Config laden
        String prefix = plugin.getConfig().getString("prefix", "");
        String statusMessage;

        if (nowBlocked) {
            statusMessage = plugin.getConfig().getString("messages.entityspawn.disabled",
                    "&cEntity spawning has been &4disabled&c!");
        } else {
            statusMessage = plugin.getConfig().getString("messages.entityspawn.enabled",
                    "&aEntity spawning has been &2enabled&a!");
        }

        String fullMessage = translateColorCodes(prefix + statusMessage);
        sender.sendMessage(fullMessage);

        // Sound für Spieler
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (nowBlocked) {
                player.playSound(player.getLocation(), "minecraft:block.note_block.bass", 0.5f, 0.5f);
            } else {
                player.playSound(player.getLocation(), "minecraft:block.note_block.pling", 0.5f, 1.0f);
            }
        }

        return true;
    }

    /**
     * Wandelt & Farbcodes in § Farbcodes um
     */
    private String translateColorCodes(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}