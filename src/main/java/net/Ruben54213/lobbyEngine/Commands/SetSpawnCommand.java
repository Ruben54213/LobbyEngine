package net.Ruben54213.lobbyEngine.Commands;

import net.Ruben54213.lobbyEngine.Utility.SpawnManager;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Command zum Setzen des Spawns
 */
public class SetSpawnCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final SpawnManager spawnManager;

    public SetSpawnCommand(JavaPlugin plugin, SpawnManager spawnManager) {
        this.plugin = plugin;
        this.spawnManager = spawnManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Nur Spieler können Spawn setzen
        if (!(sender instanceof Player)) {
            sender.sendMessage(translateColorCodes("&cOnly players can use this command!"));
            return true;
        }

        Player player = (Player) sender;

        // Permission Check
        if (!player.hasPermission("lobbyengine.setspawn")) {
            String noPermMessage = translateColorCodes(
                    plugin.getConfig().getString("prefix", "&e&lLobby&6&lEngine &8» ") +
                            plugin.getConfig().getString("messages.noperm", "&cYou don't have permission!")
            );
            player.sendMessage(noPermMessage);
            return true;
        }

        // Spawn an aktueller Position setzen
        spawnManager.setSpawn(player.getLocation());

        // Bestätigungs-Nachricht
        String successMessage = translateColorCodes(
                plugin.getConfig().getString("prefix", "&e&lLobby&6&lEngine &8» ") +
                        plugin.getConfig().getString("messages.spawn.set", "&aSpawn location has been set!")
        );
        player.sendMessage(successMessage);

        // Bestätigungs-Sound aus Config
        String confirmSound = plugin.getConfig().getString("spawn.sounds.confirm", "BLOCK_NOTE_BLOCK_PLING");
        float confirmVolume = (float) plugin.getConfig().getDouble("spawn.sounds.confirm-volume", 1.0);
        float confirmPitch = (float) plugin.getConfig().getDouble("spawn.sounds.confirm-pitch", 2.0);

        try {
            player.playSound(player.getLocation(), Sound.valueOf(confirmSound), confirmVolume, confirmPitch);
        } catch (IllegalArgumentException e) {
            // Fallback auf Standard-Sound
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
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