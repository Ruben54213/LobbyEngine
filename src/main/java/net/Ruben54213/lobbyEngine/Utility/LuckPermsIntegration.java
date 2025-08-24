package net.Ruben54213.lobbyEngine.Utility;


import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;

public class LuckPermsIntegration {

    /**
     * @param player Der Spieler, dessen Prefix abgefragt wird
     * @return Das Prefix des Spielers oder ein leerer String, wenn kein Prefix vorhanden ist
     */
    public static String getPlayerPrefix(Player player) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);

        if (user != null) {
            CachedMetaData metaData = user.getCachedData().getMetaData();
            return metaData.getPrefix() != null ? metaData.getPrefix() : "";
        }
        return "";
    }

    /**
     * @param player Der Spieler, dessen Suffix abgefragt wird
     * @return Das Suffix des Spielers oder ein leerer String, wenn kein Suffix vorhanden ist
     */
    public static String getPlayerSuffix(Player player) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);

        if (user != null) {
            CachedMetaData metaData = user.getCachedData().getMetaData();
            return metaData.getSuffix() != null ? metaData.getSuffix() : "";
        }
        return "";
    }

    /**
     * Fügt eine Permission für einen Spieler hinzu.
     *
     * @param player     Der Spieler, dem die Permission hinzugefügt wird
     * @param permission Die Permission, die hinzugefügt werden soll
     */
    public static void addPermission(Player player, String permission) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);

        if (user != null) {
            Node node = Node.builder(permission).value(true).build();
            user.data().add(node);
            luckPerms.getUserManager().saveUser(user);
        }
    }

    /**
     * Entfernt eine Permission von einem Spieler.
     *
     * @param player     Der Spieler, von dem die Permission entfernt wird
     * @param permission Die Permission, die entfernt werden soll
     */
    public static void removePermission(Player player, String permission) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);

        if (user != null) {
            Node node = Node.builder(permission).value(true).build();
            user.data().remove(node);
            luckPerms.getUserManager().saveUser(user);
        }
    }

    /**
     * Überprüft, ob ein Spieler eine bestimmte Permission besitzt.
     *
     * @param player     Der Spieler, dessen Permissions geprüft werden
     * @param permission Die zu prüfende Permission
     * @return true, wenn der Spieler die Permission besitzt, ansonsten false
     */
    public static boolean hasPermission(Player player, String permission) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);

        if (user != null) {
            return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
        }
        return false;
    }

    /**
     * Fügt einen Spieler einer LuckPerms-Gruppe hinzu.
     *
     * @param player Der Spieler, der der Gruppe hinzugefügt werden soll
     * @param group  Die Gruppe, zu der der Spieler hinzugefügt wird
     */
    public static void addToGroup(Player player, String group) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);

        if (user != null) {
            Node groupNode = InheritanceNode.builder(group).value(true).build();
            user.data().add(groupNode);
            luckPerms.getUserManager().saveUser(user);
        }
    }

    /**
     * Entfernt einen Spieler aus einer LuckPerms-Gruppe.
     *
     * @param player Der Spieler, der aus der Gruppe entfernt werden soll
     * @param group  Die Gruppe, aus der der Spieler entfernt wird
     */
    public static void removeFromGroup(Player player, String group) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);

        if (user != null) {
            Node groupNode = InheritanceNode.builder(group).value(true).build();
            user.data().remove(groupNode);
            luckPerms.getUserManager().saveUser(user);
        }
    }

    /**
     * Überprüft, ob ein Spieler einer LuckPerms-Gruppe angehört.
     *
     * @param player Der Spieler, dessen Gruppenmitgliedschaft geprüft wird
     * @param group  Die zu prüfende Gruppe
     * @return true, wenn der Spieler in der Gruppe ist, ansonsten false
     */
    public static boolean isInGroup(Player player, String group) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);

        if (user != null) {
            return user.data().toCollection().stream()
                    .anyMatch(node -> node.getKey().equals("group." + group));
        }
        return false;
    }
}