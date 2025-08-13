package net.Ruben54213.lobbyEngine.Commands;

import net.Ruben54213.lobbyEngine.Utility.ServerNavigatorGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command zum Verwalten des Server-Navigators
 */
public class LobbyCommand implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final ServerNavigatorGUI navigatorGUI;

    public LobbyCommand(JavaPlugin plugin, ServerNavigatorGUI navigatorGUI) {
        this.plugin = plugin;
        this.navigatorGUI = navigatorGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Permission Check
        if (!sender.hasPermission("lobbyengine.lobby")) {
            String noPermMessage = translateColorCodes(
                    plugin.getConfig().getString("prefix", "&e&lLobby&6&lEngine &8» ") +
                            plugin.getConfig().getString("messages.noperm", "&cYou don't have permission!")
            );
            sender.sendMessage(noPermMessage);
            return true;
        }

        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                return handleCreateCommand(sender, args);
            case "remove":
                return handleRemoveCommand(sender, args);
            case "list":
                return handleListCommand(sender);
            case "reload":
                return handleReloadCommand(sender);
            default:
                sendHelpMessage(sender);
                return true;
        }
    }

    /**
     * Behandelt /lobby create <server> <slot> <name> <material>
     */
    private boolean handleCreateCommand(CommandSender sender, String[] args) {
        if (args.length < 5) {
            sender.sendMessage(translateColorCodes(
                    plugin.getConfig().getString("prefix", "") +
                            "&cUsage: /lobby create <server> <slot> <name> <material>"
            ));
            return true;
        }

        String serverName = args[1];
        int slot;
        String itemName = args[3];
        String materialName = args[4].toUpperCase();

        // Slot validieren
        try {
            slot = Integer.parseInt(args[2]);
            if (slot < 0 || slot >= 45) {
                sender.sendMessage(translateColorCodes(
                        plugin.getConfig().getString("prefix", "") +
                                "&cSlot must be between 0 and 44!"
                ));
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(translateColorCodes(
                    plugin.getConfig().getString("prefix", "") +
                            "&cInvalid slot number!"
            ));
            return true;
        }

        // Material validieren
        Material material;
        try {
            material = Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(translateColorCodes(
                    plugin.getConfig().getString("prefix", "") +
                            "&cInvalid material: " + materialName
            ));
            return true;
        }

        // Prüfen ob Slot bereits belegt
        if (plugin.getConfig().contains("navigator.servers." + slot)) {
            sender.sendMessage(translateColorCodes(
                    plugin.getConfig().getString("prefix", "") +
                            plugin.getConfig().getString("messages.navigator.slot-occupied",
                                            "&cSlot %slot% is already occupied!")
                                    .replace("%slot%", String.valueOf(slot))
            ));
            return true;
        }

        // Server erstellen
        plugin.getConfig().set("navigator.servers." + slot + ".server", serverName);
        plugin.getConfig().set("navigator.servers." + slot + ".name", itemName);
        plugin.getConfig().set("navigator.servers." + slot + ".material", materialName);
        plugin.saveConfig();

        // Navigator neu laden
        navigatorGUI.reloadServers();

        sender.sendMessage(translateColorCodes(
                plugin.getConfig().getString("prefix", "") +
                        plugin.getConfig().getString("messages.navigator.created",
                                        "&aServer &b%server%&a created in slot &b%slot%&a!")
                                .replace("%server%", serverName)
                                .replace("%slot%", String.valueOf(slot))
        ));

        return true;
    }

    /**
     * Behandelt /lobby remove <slot>
     */
    private boolean handleRemoveCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(translateColorCodes(
                    plugin.getConfig().getString("prefix", "") +
                            "&cUsage: /lobby remove <slot>"
            ));
            return true;
        }

        int slot;
        try {
            slot = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(translateColorCodes(
                    plugin.getConfig().getString("prefix", "") +
                            "&cInvalid slot number!"
            ));
            return true;
        }

        // Prüfen ob Server existiert
        if (!plugin.getConfig().contains("navigator.servers." + slot)) {
            sender.sendMessage(translateColorCodes(
                    plugin.getConfig().getString("prefix", "") +
                            plugin.getConfig().getString("messages.navigator.slot-empty",
                                            "&cSlot &b%slot%&c is empty!")
                                    .replace("%slot%", String.valueOf(slot))
            ));
            return true;
        }

        // Server entfernen
        plugin.getConfig().set("navigator.servers." + slot, null);
        plugin.saveConfig();

        // Navigator neu laden
        navigatorGUI.reloadServers();

        sender.sendMessage(translateColorCodes(
                plugin.getConfig().getString("prefix", "") +
                        plugin.getConfig().getString("messages.navigator.removed",
                                        "&aServer removed from slot &b%slot%&a!")
                                .replace("%slot%", String.valueOf(slot))
        ));

        return true;
    }

    /**
     * Behandelt /lobby list
     */
    private boolean handleListCommand(CommandSender sender) {
        sender.sendMessage(translateColorCodes("&d&lServer Navigator - Server List:"));

        boolean hasServers = false;
        for (int i = 0; i < 45; i++) {
            if (plugin.getConfig().contains("navigator.servers." + i)) {
                String serverName = plugin.getConfig().getString("navigator.servers." + i + ".server");
                String itemName = plugin.getConfig().getString("navigator.servers." + i + ".name");
                String material = plugin.getConfig().getString("navigator.servers." + i + ".material");

                sender.sendMessage(translateColorCodes(
                        "&7Slot &b" + i + "&7: &f" + itemName + " &7(&b" + serverName + "&7) [&e" + material + "&7]"
                ));
                hasServers = true;
            }
        }

        if (!hasServers) {
            sender.sendMessage(translateColorCodes("&7No servers configured."));
        }

        return true;
    }

    /**
     * Behandelt /lobby reload
     */
    private boolean handleReloadCommand(CommandSender sender) {
        plugin.reloadConfig();
        navigatorGUI.reloadServers();

        sender.sendMessage(translateColorCodes(
                plugin.getConfig().getString("prefix", "") +
                        "&aNavigator configuration reloaded!"
        ));

        return true;
    }

    /**
     * Sendet Hilfe-Nachricht
     */
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(translateColorCodes("&d&lLobbyEngine Navigator Commands:"));
        sender.sendMessage(translateColorCodes("&7/lobby create <server> <slot> <name> <material> &8- Create server"));
        sender.sendMessage(translateColorCodes("&7/lobby remove <slot> &8- Remove server"));
        sender.sendMessage(translateColorCodes("&7/lobby list &8- List all servers"));
        sender.sendMessage(translateColorCodes("&7/lobby reload &8- Reload configuration"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("lobbyengine.lobby")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            return Arrays.asList("create", "remove", "list", "reload").stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            List<String> slots = new ArrayList<>();
            for (int i = 0; i < 45; i++) {
                if (plugin.getConfig().contains("navigator.servers." + i)) {
                    slots.add(String.valueOf(i));
                }
            }
            return slots.stream()
                    .filter(s -> s.startsWith(args[1]))
                    .collect(Collectors.toList());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            List<String> slots = new ArrayList<>();
            for (int i = 0; i < 45; i++) {
                slots.add(String.valueOf(i));
            }
            return slots.stream()
                    .filter(s -> s.startsWith(args[2]))
                    .collect(Collectors.toList());
        }

        if (args.length == 5 && args[0].equalsIgnoreCase("create")) {
            return Arrays.stream(Material.values())
                    .filter(m -> m.isItem() && !m.isAir())
                    .map(Material::name)
                    .filter(s -> s.toLowerCase().startsWith(args[4].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    /**
     * Wandelt & Farbcodes in § Farbcodes um
     */
    private String translateColorCodes(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}