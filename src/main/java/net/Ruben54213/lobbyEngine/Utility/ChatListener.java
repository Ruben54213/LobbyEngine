package net.Ruben54213.lobbyEngine.Utility;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private static final String PERM_COLOR = "simplecore.chat.color";
    private static final String PERM_EXTRA_FORMAT = "simplecore.chat.extra";
    private static final String CHAT_PREFIX = "§7» ";

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        LuckPerms lp = LuckPermsProvider.get();
        CachedMetaData meta = lp.getPlayerAdapter(Player.class).getMetaData(player);

        String prefix = ChatFormatter.format(meta.getPrefix() == null ? "" : meta.getPrefix());
        String suffix = ChatFormatter.format(meta.getSuffix() == null ? "" : meta.getSuffix());

        String msg = event.getMessage();
        if (player.hasPermission(PERM_COLOR)) {
            msg = ChatFormatter.format(msg);
        }

        StringBuilder out = new StringBuilder();
        if (player.hasPermission(PERM_EXTRA_FORMAT)) out.append("\n");

        out.append(prefix)
                .append("§f").append(player.getName())
                .append(suffix)
                .append("§r ")                 // <— Reset, damit Format nicht weiterläuft
                .append(CHAT_PREFIX)
                .append(msg);

        if (player.hasPermission(PERM_EXTRA_FORMAT)) out.append("\n");

        event.setFormat(out.toString());  // keine % drin → safe
    }
}
