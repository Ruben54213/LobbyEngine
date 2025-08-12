package net.Ruben54213.lobbyEngine.Commands;

import net.Ruben54213.lobbyEngine.Utility.SpawnManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Command zum Teleportieren zum Spawn
 */
public class SpawnCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final SpawnManager spawnManager;

    public SpawnCommand(JavaPlugin plugin, SpawnManager spawnManager) {
        this.plugin = plugin;
        this.spawnManager = spawnManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Nur Spieler können sich teleportieren
        if (!(sender instanceof Player)) {
            sender.sendMessage(translateColorCodes("&cOnly players can use this command!"));
            return true;
        }

        Player player = (Player) sender;

        // Permission Check
        if (!player.hasPermission("lobbyengine.spawn")) {
            String noPermMessage = translateColorCodes(
                    plugin.getConfig().getString("prefix", "") +
                            plugin.getConfig().getString("messages.noperm", "&cYou don't have permission!")
            );
            player.sendMessage(noPermMessage);
            return true;
        }

        // Prüfen ob Spawn gesetzt ist
        if (!spawnManager.hasSpawn()) {
            String noSpawnMessage = translateColorCodes(
                    plugin.getConfig().getString("prefix", "") +
                            plugin.getConfig().getString("messages.spawn.notset", "&cNo spawn location set!")
            );
            player.sendMessage(noSpawnMessage);
            return true;
        }

        // Zum Spawn teleportieren
        if (spawnManager.teleportToSpawn(player)) {
            String successMessage = translateColorCodes(
                    plugin.getConfig().getString("prefix", "") +
                            plugin.getConfig().getString("messages.spawn.teleported", "&aTeleported to spawn!")
            );
            player.sendMessage(successMessage);
        } else {
            String errorMessage = translateColorCodes(
                    plugin.getConfig().getString("prefix", "") +
                            plugin.getConfig().getString("messages.spawn.error", "&cError while teleporting!")
            );
            player.sendMessage(errorMessage);
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