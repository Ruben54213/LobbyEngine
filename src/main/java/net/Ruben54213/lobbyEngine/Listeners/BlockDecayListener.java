package net.Ruben54213.lobbyEngine.Listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Verhindert das Verschwinden/Verfall von Blöcken (Leaves, Ice, Snow, etc.)
 */
public class BlockDecayListener implements Listener {

    private final JavaPlugin plugin;
    private boolean blockDecayBlocked;

    public BlockDecayListener(JavaPlugin plugin) {
        this.plugin = plugin;
        // Standard: Block-Decay ist blockiert
        this.blockDecayBlocked = plugin.getConfig().getBoolean("blockdecay.blocked", true);
    }

    /**
     * Verhindert das Verschwinden von Leaves (Blättern)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onLeavesDecay(LeavesDecayEvent event) {
        if (blockDecayBlocked) {
            event.setCancelled(true);
        }
    }

    /**
     * Verhindert das Verblassen/Schmelzen von Blöcken (Ice, Snow, etc.)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockFade(BlockFadeEvent event) {
        if (!blockDecayBlocked) {
            return;
        }

        Material blockType = event.getBlock().getType();

        // Verhindere das Verblassen von verschiedenen Blöcken
        switch (blockType) {
            case ICE:
            case PACKED_ICE:
            case BLUE_ICE:
            case FROSTED_ICE:
            case SNOW:
            case SNOW_BLOCK:
            case POWDER_SNOW:
            case FARMLAND:
            case GRASS_BLOCK:
            case MYCELIUM:
            case PODZOL:
            case FIRE:
            case SOUL_FIRE:
                event.setCancelled(true);
                break;
            default:
                // Andere Blöcke können verblassen
                break;
        }
    }

    /**
     * Togglet den Block-Decay Status
     *
     * @return true wenn jetzt blockiert, false wenn erlaubt
     */
    public boolean toggleBlockDecay() {
        blockDecayBlocked = !blockDecayBlocked;

        // In Config speichern
        plugin.getConfig().set("blockdecay.blocked", blockDecayBlocked);
        plugin.saveConfig();

        return blockDecayBlocked;
    }

    /**
     * Prüft ob Block-Decay blockiert ist
     *
     * @return true wenn blockiert
     */
    public boolean isBlockDecayBlocked() {
        return blockDecayBlocked;
    }

    /**
     * Setzt den Block-Decay Status
     *
     * @param blocked true um zu blockieren, false um zu erlauben
     */
    public void setBlockDecayBlocked(boolean blocked) {
        this.blockDecayBlocked = blocked;
        plugin.getConfig().set("blockdecay.blocked", blocked);
        plugin.saveConfig();
    }

    /**
     * Registriert diesen Listener beim Plugin
     */
    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
}