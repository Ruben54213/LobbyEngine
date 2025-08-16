package net.Ruben54213.lobbyEngine.Commands;

import net.Ruben54213.lobbyEngine.Utility.LobbyManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Command für die Lobby-Verwaltung mit BungeeCord/Velocity Support
 */
public class LobbiesCommand implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final LobbyManager lobbyManager;

    public LobbiesCommand(JavaPlugin plugin, LobbyManager lobbyManager) {
        this.plugin = plugin;
        this.lobbyManager = lobbyManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Permission Check
        if (!sender.hasPermission("lobbyengine.lobbies")) {
            String message = plugin.getConfig().getString("lobby.no-permission", "&cDu hast keine Berechtigung für diesen Befehl!");
            sender.sendMessage(translateColorCodes(message));
            return true;
        }

        // Keine Argumente - zeige Help
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                handleCreateCommand(sender, args);
                break;
            case "remove":
            case "delete":
                handleRemoveCommand(sender, args);
                break;
            case "list":
                handleListCommand(sender);
                break;
            case "gui":
                handleGUICommand(sender);
                break;
            case "reload":
                handleReloadCommand(sender);
                break;
            default:
                sendHelpMessage(sender);
                break;
        }

        return true;
    }

    /**
     * Behandelt /lobbies create <server-name>
     */
    private void handleCreateCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            String message = plugin.getConfig().getString("lobby.create-usage", "&cUsage: /lobbies create <server-name>");
            sender.sendMessage(translateColorCodes(message));
            return;
        }

        String serverName = args[1];

        // Validierung des Server-Namens
        if (!isValidServerName(serverName)) {
            String message = plugin.getConfig().getString("lobby.invalid-name", "&cInvalid server name! Only letters, numbers, - and _ are allowed.");
            sender.sendMessage(translateColorCodes(message));
            return;
        }

        // Lobby erstellen
        if (lobbyManager.addLobby(serverName)) {
            String lobbySlot = lobbyManager.getLastAddedLobbyName();
            String message = plugin.getConfig().getString("lobby.created", "&aLobby &e%lobby%&a was successfully created and assigned to &e%slot%&a!")
                    .replace("%lobby%", serverName)
                    .replace("%slot%", lobbySlot);
            sender.sendMessage(translateColorCodes(message));
            plugin.getLogger().info(sender.getName() + " created lobby: " + lobbySlot + " -> " + serverName);
        } else {
            // Prüfen ob Maximum erreicht
            if (lobbyManager.getRegisteredLobbies().size() >= lobbyManager.getMaxLobbies()) {
                String message = plugin.getConfig().getString("lobby.max-reached", "&cMaximum number of lobbies reached! (%max%)")
                        .replace("%max%", String.valueOf(lobbyManager.getMaxLobbies()));
                sender.sendMessage(translateColorCodes(message));
            } else {
                String message = plugin.getConfig().getString("lobby.creation-failed", "&cFailed to create lobby for server &e%server%&c!")
                        .replace("%server%", serverName);
                sender.sendMessage(translateColorCodes(message));
            }
        }
    }

    /**
     * Behandelt /lobbies remove <Lobby-X>
     */
    private void handleRemoveCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            String message = plugin.getConfig().getString("lobby.remove-usage", "&cUsage: /lobbies remove <Lobby-1 to Lobby-7>");
            sender.sendMessage(translateColorCodes(message));
            return;
        }

        String lobbyName = args[1];

        // Validierung des Lobby-Namens
        if (!isValidLobbyName(lobbyName)) {
            String message = plugin.getConfig().getString("lobby.invalid-name", "&cInvalid lobby name! Use format: Lobby-1, Lobby-2, ... Lobby-7");
            sender.sendMessage(translateColorCodes(message));
            return;
        }

        if (lobbyManager.removeLobby(lobbyName)) {
            String message = plugin.getConfig().getString("lobby.removed", "&aLobby &e%lobby%&a was successfully removed!")
                    .replace("%lobby%", lobbyName);
            sender.sendMessage(translateColorCodes(message));
            plugin.getLogger().info(sender.getName() + " removed lobby: " + lobbyName);
        } else {
            String message = plugin.getConfig().getString("lobby.not-found", "&cLobby &e%lobby%&c was not found!")
                    .replace("%lobby%", lobbyName);
            sender.sendMessage(translateColorCodes(message));
        }
    }

    /**
     * Behandelt /lobbies list
     */
    private void handleListCommand(CommandSender sender) {
        if (lobbyManager.getRegisteredLobbies().isEmpty()) {
            String message = plugin.getConfig().getString("lobby.no-lobbies", "&eNo lobbies configured.");
            sender.sendMessage(translateColorCodes(message));
            return;
        }

        String header = plugin.getConfig().getString("lobby.list-header", "&6=== Registered Lobbies ===");
        sender.sendMessage(translateColorCodes(header));

        // Sortierte Liste (Lobby-1, Lobby-2, etc.) mit Server-Namen
        for (int i = 1; i <= 7; i++) {
            String lobbyName = "Lobby-" + i;
            if (lobbyManager.getRegisteredLobbies().contains(lobbyName)) {
                String serverName = plugin.getConfig().getString("lobby.servers.slot" + i + ".server", "unknown");
                String status = "&a(Enabled) &7-> &e" + serverName;
                String listEntry = plugin.getConfig().getString("lobby.list-entry", "&e- %lobby% %status%")
                        .replace("%lobby%", lobbyName)
                        .replace("%status%", status);
                sender.sendMessage(translateColorCodes(listEntry));
            }
        }

        String footer = plugin.getConfig().getString("lobby.list-footer", "&7Count: &e%count%&7/&e%max%")
                .replace("%count%", String.valueOf(lobbyManager.getRegisteredLobbies().size()))
                .replace("%max%", String.valueOf(lobbyManager.getMaxLobbies()));
        sender.sendMessage(translateColorCodes(footer));
    }

    /**
     * Behandelt /lobbies gui
     */
    private void handleGUICommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            String message = plugin.getConfig().getString("lobby.players-only", "&cThis command can only be used by players!");
            sender.sendMessage(translateColorCodes(message));
            return;
        }

        Player player = (Player) sender;
        lobbyManager.openLobbyGUI(player);
    }

    /**
     * Behandelt /lobbies reload
     */
    private void handleReloadCommand(CommandSender sender) {
        if (!sender.hasPermission("lobbyengine.lobbies.reload")) {
            String message = plugin.getConfig().getString("lobby.no-permission", "&cYou don't have permission for this command!");
            sender.sendMessage(translateColorCodes(message));
            return;
        }

        plugin.reloadConfig();
        String message = plugin.getConfig().getString("lobby.reloaded", "&aLobby configuration has been reloaded!");
        sender.sendMessage(translateColorCodes(message));
    }

    /**
     * Sendet Help-Nachricht
     */
    private void sendHelpMessage(CommandSender sender) {
        List<String> helpMessages = plugin.getConfig().getStringList("lobby.help-messages");

        if (helpMessages.isEmpty()) {
            // Standard Help-Nachrichten
            helpMessages = Arrays.asList(
                    "&6=== Lobbies Commands ===",
                    "&e/lobbies create <server-name> &7- Creates a new lobby",
                    "&e/lobbies remove <Lobby-X> &7- Removes a lobby slot",
                    "&e/lobbies list &7- Shows all lobbies with server mappings",
                    "&e/lobbies gui &7- Opens the lobby GUI",
                    "&e/lobbies reload &7- Reloads the config"
            );
        }

        for (String message : helpMessages) {
            sender.sendMessage(translateColorCodes(message));
        }
    }

    /**
     * Validiert einen Server-Namen
     */
    private boolean isValidServerName(String name) {
        // Nur Buchstaben, Zahlen, Bindestriche und Unterstriche erlauben
        return name.matches("^[a-zA-Z0-9-_]+$") && name.length() >= 1 && name.length() <= 32;
    }

    /**
     * Validiert einen Lobby-Namen (für remove command)
     */
    private boolean isValidLobbyName(String name) {
        return name.matches("^Lobby-[1-7]$");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("lobbyengine.lobbies")) {
            return completions;
        }

        if (args.length == 1) {
            // Erste Ebene: Subcommands
            List<String> subCommands = Arrays.asList("create", "remove", "list", "gui", "reload");
            String input = args[0].toLowerCase();

            for (String subCommand : subCommands) {
                if (subCommand.startsWith(input)) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            String input = args[1];

            if (subCommand.equals("create")) {
                // Für create: keine Tab-Completion, da Server-Name frei wählbar ist
                // Optionale Beispiele:
                if (input.isEmpty()) {
                    completions.addAll(Arrays.asList("lobby2", "hub", "main", "survival"));
                }
            } else if (subCommand.equals("remove") || subCommand.equals("delete")) {
                // Für remove: existierende Lobbies vorschlagen
                for (String lobby : lobbyManager.getRegisteredLobbies()) {
                    if (lobby.startsWith(input)) {
                        completions.add(lobby);
                    }
                }
            }
        }

        return completions;
    }

    /**
     * Wandelt & Farbcodes in § Farbcodes um
     */
    private String translateColorCodes(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}