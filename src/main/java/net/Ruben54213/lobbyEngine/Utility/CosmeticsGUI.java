package net.Ruben54213.lobbyEngine.Utility;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Verwaltet das Cosmetics GUI-System
 */
public class CosmeticsGUI implements Listener {

    private final JavaPlugin plugin;
    private final CosmeticsFeatures cosmeticsFeatures;

    public CosmeticsGUI(JavaPlugin plugin, CosmeticsFeatures cosmeticsFeatures) {
        this.plugin = plugin;
        this.cosmeticsFeatures = cosmeticsFeatures;
    }

    /**
     * Öffnet das Haupt-Cosmetics GUI
     */
    public void openMainCosmeticsGUI(Player player) {
        // 3 Zeilen GUI erstellen
        Inventory gui = Bukkit.createInventory(null, 27, translateColorCodes(plugin.getConfig().getString("shortprefix", "") + plugin.getConfig().getString("messages.cosmetics.gui.cosmetics", "")));

        // Glass Borders erstellen
        fillBorders(gui);

        // Kategorie-Items hinzufügen
        gui.setItem(11, createParticlesItem());
        gui.setItem(13, createEffectsItem());
        gui.setItem(15, createGadgetsItem());

        // Sound abspielen
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);

        // GUI öffnen
        player.openInventory(gui);
    }

    /**
     * Öffnet das Partikel-GUI
     */
    public void openParticlesGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, translateColorCodes(plugin.getConfig().getString("shortprefix", "") + plugin.getConfig().getString("messages.cosmetics.gui.particles", "")));
        fillBorders(gui);

        // Partikel-Items hinzufügen
        gui.setItem(10, createHeartParticleItem(player));
        gui.setItem(11, createEnderParticleItem(player));
        gui.setItem(12, createLavaParticleItem(player));
        gui.setItem(14, createWaterParticleItem(player));
        gui.setItem(15, createFireParticleItem(player));
        gui.setItem(16, createRainCloudItem(player));

        // Reset-Button hinzufügen
        gui.setItem(13, createParticleResetButton());

        // Zurück-Button
        gui.setItem(22, createBackButton());

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
        player.openInventory(gui);
    }

    /**
     * Öffnet das Effekte-GUI
     */
    public void openEffectsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, translateColorCodes(plugin.getConfig().getString("shortprefix", "") + plugin.getConfig().getString("messages.cosmetics.gui.effects", "")));
        fillBorders(gui);

        // Effekt-Items hinzufügen
        gui.setItem(10, createFlyItem(player));
        gui.setItem(11, createDoubleJumpItem(player));
        gui.setItem(12, createJumpBoostItem(player));
        gui.setItem(14, createNightVisionItem(player));
        gui.setItem(15, createSpeedItem(player));

        // Reset-Button hinzufügen
        gui.setItem(13, createEffectResetButton());

        // Zurück-Button
        gui.setItem(22, createBackButton());

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
        player.openInventory(gui);
    }

    /**
     * Öffnet das Gadgets-GUI
     */
    public void openGadgetsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, translateColorCodes(plugin.getConfig().getString("shortprefix", "") + plugin.getConfig().getString("messages.cosmetics.gui.gadgets", "")));
        fillBorders(gui);

        // Gadget-Items hinzufügen
        gui.setItem(11, createTeleporterItem(player));
        gui.setItem(15, createGrappleHookItem(player));

        // Reset-Button hinzufügen
        gui.setItem(13, createGadgetResetButton()); // Position geändert da Slot 13 jetzt Love Bow ist

        // Zurück-Button
        gui.setItem(22, createBackButton());

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
        player.openInventory(gui);
    }

    /**
     * Füllt die Ränder mit Glass Panes
     */
    private void fillBorders(Inventory gui) {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(translateColorCodes("&7"));
            glass.setItemMeta(glassMeta);
        }

        // Erste und letzte Reihe
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, glass);
            gui.setItem(i + 18, glass);
        }

        // Seitenränder
        gui.setItem(9, glass);
        gui.setItem(17, glass);
    }

    // ==================== KATEGORIE ITEMS ====================

    private ItemStack createParticlesItem() {
        ItemStack item = new ItemStack(Material.SUGAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.gui.particles", "")));
            meta.setLore(List.of(
                    translateColorCodes(plugin.getConfig().getString("messages.cosmetics.gui.particles-info", ""))
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createEffectsItem() {
        ItemStack item = new ItemStack(Material.GLASS_BOTTLE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.gui.effects", "")));
            meta.setLore(List.of(
                    translateColorCodes(plugin.getConfig().getString("messages.cosmetics.gui.effects-info", ""))
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createGadgetsItem() {
        ItemStack item = new ItemStack(Material.PISTON);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.gui.gadgets", "")));
            meta.setLore(List.of(
                    translateColorCodes(plugin.getConfig().getString("messages.cosmetics.gui.gadgets-info", ""))
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    // ==================== PARTIKEL ITEMS ====================

    private ItemStack createHeartParticleItem(Player player) {
        ItemStack item = new ItemStack(Material.RED_DYE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.particles.heart", "")));
            boolean hasPermission = player.hasPermission("lobbyengine.cosmetics.particles.heart");
            meta.setLore(hasPermission ?
                    List.of(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.unlocked", ""))) :
                    List.of(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.locked", "")))
            );
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createEnderParticleItem(Player player) {
        ItemStack item = new ItemStack(Material.ENDER_PEARL);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.particles.ender", "")));
            boolean hasPermission = player.hasPermission("lobbyengine.cosmetics.particles.ender");
            meta.setLore(hasPermission ?
                    List.of(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.unlocked", ""))) :
                    List.of(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.locked", "")))
            );
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createLavaParticleItem(Player player) {
        ItemStack item = new ItemStack(Material.LAVA_BUCKET);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.particles.lava", "")));
            boolean hasPermission = player.hasPermission("lobbyengine.cosmetics.particles.lava");
            meta.setLore(hasPermission ?
                    List.of(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.unlocked", ""))) :
                    List.of(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.locked", "")))
            );
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createWaterParticleItem(Player player) {
        ItemStack item = new ItemStack(Material.WATER_BUCKET);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.particles.water", "")));
            boolean hasPermission = player.hasPermission("lobbyengine.cosmetics.particles.water");
            meta.setLore(hasPermission ?
                    List.of(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.unlocked", ""))) :
                    List.of(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.locked", "")))
            );
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createFireParticleItem(Player player) {
        ItemStack item = new ItemStack(Material.FIRE_CHARGE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.particles.fire", "")));
            boolean hasPermission = player.hasPermission("lobbyengine.cosmetics.particles.fire");
            meta.setLore(hasPermission ?
                    List.of(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.unlocked", ""))) :
                    List.of(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.locked", "")))
            );
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createRainCloudItem(Player player) {
        ItemStack item = new ItemStack(Material.GRAY_WOOL);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.particles.raincloud", "")));
            boolean hasPermission = player.hasPermission("lobbyengine.cosmetics.particles.raincloud");
            meta.setLore(hasPermission ?
                    List.of(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.unlocked", ""))) :
                    List.of(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.locked", "")))
            );
            item.setItemMeta(meta);
        }
        return item;
    }

    // ==================== EFFEKT ITEMS ====================

    private ItemStack createFlyItem(Player player) {
        ItemStack item = new ItemStack(Material.FEATHER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.effects.fly", "")));
            boolean hasPermission = player.hasPermission("lobbyengine.cosmetics.effects.fly");
            meta.setLore(hasPermission ?
                    List.of(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.unlocked", ""))) :
                    List.of(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.locked", "")))
            );
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createDoubleJumpItem(Player player) {
        ItemStack item = new ItemStack(Material.RABBIT_FOOT);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.effects.doublejump", "")));
            boolean hasPermission = player.hasPermission("lobbyengine.cosmetics.effects.doublejump");
            meta.setLore(hasPermission ?
                    List.of(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.unlocked", ""))) :
                    List.of(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.locked", "")))
            );
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createJumpBoostItem(Player player) {
        ItemStack item = new ItemStack(Material.SLIME_BALL);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.effects.jumpboost", "")));
            boolean hasPermission = player.hasPermission("lobbyengine.cosmetics.effects.jumpboost");
            meta.setLore(hasPermission ?
                    List.of(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.unlocked", ""))) :
                    List.of(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.locked", "")))
            );
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createNightVisionItem(Player player) {
        ItemStack item = new ItemStack(Material.GOLDEN_CARROT);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.effects.nightvision", "")));
            boolean hasPermission = player.hasPermission("lobbyengine.cosmetics.effects.nightvision");
            meta.setLore(hasPermission ?
                    List.of(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.unlocked", ""))) :
                    List.of(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.locked", "")))
            );
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createSpeedItem(Player player) {
        ItemStack item = new ItemStack(Material.SUGAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.effects.speed", "")));
            boolean hasPermission = player.hasPermission("lobbyengine.cosmetics.effects.speed");
            meta.setLore(hasPermission ?
                    List.of(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.unlocked", ""))) :
                    List.of(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.locked", "")))
            );
            item.setItemMeta(meta);
        }
        return item;
    }

    // ==================== GADGET ITEMS ====================

    private ItemStack createTeleporterItem(Player player) {
        ItemStack item = new ItemStack(Material.ENDER_EYE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.gadgets.teleporter-name", "&5&lTeleporter")));
            boolean hasPermission = player.hasPermission("lobbyengine.cosmetics.gadgets.teleporter");
            meta.setLore(hasPermission ?
                    List.of(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.unlocked", ""))) :
                    List.of(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.locked", "")))
            );
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createGrappleHookItem(Player player) {
        ItemStack item = new ItemStack(Material.FISHING_ROD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.gadgets.grapplehook-name", "&2&lEnterhaken")));
            boolean hasPermission = player.hasPermission("lobbyengine.cosmetics.gadgets.grapplehook");
            meta.setLore(hasPermission ?
                    List.of(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.unlocked", ""))) :
                    List.of(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.locked", "")))
            );
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createLoveBowItem(Player player) {
        ItemStack item = new ItemStack(Material.BOW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.gadgets.lovebow-name", "&d&lLiebesbogen")));
            boolean hasPermission = player.hasPermission("lobbyengine.cosmetics.gadgets.lovebow");
            meta.setLore(hasPermission ?
                    List.of(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.unlocked", ""))) :
                    List.of(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.locked", "")))
            );
            item.setItemMeta(meta);
        }
        return item;
    }

    // ==================== UTILITY ====================

    private ItemStack createBackButton() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.back", "")));
            meta.setLore(List.of(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.return", ""))));
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Erstellt Reset-Button für Partikel
     */
    private ItemStack createParticleResetButton() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.remove-buttons.reset", "")));
            meta.setLore(List.of(
                    translateColorCodes(plugin.getConfig().getString("messages.cosmetics.remove-buttons.particle", ""))
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Erstellt Reset-Button für Effekte
     */
    private ItemStack createEffectResetButton() {
        ItemStack item = new ItemStack(Material.MILK_BUCKET);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.remove-buttons.reset", "")));
            meta.setLore(List.of(
                    translateColorCodes(plugin.getConfig().getString("messages.cosmetics.remove-buttons.effect", ""))
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Erstellt Reset-Button für Gadgets
     */
    private ItemStack createGadgetResetButton() {
        ItemStack item = new ItemStack(Material.RED_CONCRETE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.remove-buttons.reset", "")));
            meta.setLore(List.of(
                    translateColorCodes(plugin.getConfig().getString("messages.cosmetics.remove-buttons.gadget", ""))
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Behandelt Klicks in allen Cosmetics GUIs
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        // Prüfen ob es ein Cosmetics GUI ist - verbesserte Erkennung
        String cosmeticsTitle = translateColorCodes(plugin.getConfig().getString("shortprefix", "") + plugin.getConfig().getString("messages.cosmetics.gui.cosmetics", ""));
        String particlesTitle = translateColorCodes(plugin.getConfig().getString("shortprefix", "") + plugin.getConfig().getString("messages.cosmetics.gui.particles", ""));
        String effectsTitle = translateColorCodes(plugin.getConfig().getString("shortprefix", "") + plugin.getConfig().getString("messages.cosmetics.gui.effects", ""));
        String gadgetsTitle = translateColorCodes(plugin.getConfig().getString("shortprefix", "") + plugin.getConfig().getString("messages.cosmetics.gui.gadgets", ""));

        if (!title.equals(cosmeticsTitle) && !title.equals(particlesTitle) &&
                !title.equals(effectsTitle) && !title.equals(gadgetsTitle)) {
            return;
        }

        // Alle Klicks verhindern
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        // Navigation zwischen GUIs
        handleGUINavigation(player, title, clickedItem, event.getSlot());
    }

    /**
     * Behandelt Navigation zwischen verschiedenen GUIs
     */
    private void handleGUINavigation(Player player, String currentTitle, ItemStack clickedItem, int slot) {
        // Erkenne Haupt-Cosmetics GUI
        String cosmeticsTitle = translateColorCodes(plugin.getConfig().getString("shortprefix", "") + plugin.getConfig().getString("messages.cosmetics.gui.cosmetics", ""));

        if (currentTitle.equals(cosmeticsTitle)) {
            // Haupt-Menu Navigation
            if (slot == 11) openParticlesGUI(player);
            else if (slot == 13) openEffectsGUI(player);
            else if (slot == 15) openGadgetsGUI(player);
        } else if (slot == 22 && clickedItem.getType() == Material.ARROW) {
            // Zurück-Button
            openMainCosmeticsGUI(player);
        } else {
            // Item-Aktivierung
            handleItemActivation(player, currentTitle, clickedItem, slot);
        }
    }

    /**
     * Behandelt Aktivierung von Cosmetic-Items
     */
    private void handleItemActivation(Player player, String guiTitle, ItemStack clickedItem, int slot) {
        if (!clickedItem.hasItemMeta()) return;

        String displayName = clickedItem.getItemMeta().getDisplayName();

        // Reset-Buttons behandeln
        String resetName = translateColorCodes(plugin.getConfig().getString("messages.cosmetics.remove-buttons.reset", ""));
        if (displayName.equals(resetName)) {
            String particlesTitle = translateColorCodes(plugin.getConfig().getString("shortprefix", "") + plugin.getConfig().getString("messages.cosmetics.gui.particles", ""));
            String effectsTitle = translateColorCodes(plugin.getConfig().getString("shortprefix", "") + plugin.getConfig().getString("messages.cosmetics.gui.effects", ""));
            String gadgetsTitle = translateColorCodes(plugin.getConfig().getString("shortprefix", "") + plugin.getConfig().getString("messages.cosmetics.gui.gadgets", ""));

            if (guiTitle.equals(particlesTitle)) {
                cosmeticsFeatures.stopParticles(player);
                player.sendMessage(translateColorCodes(plugin.getConfig().getString("prefix", "") + plugin.getConfig().getString("messages.cosmetics.particles-disabled", "")));
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            } else if (guiTitle.equals(effectsTitle)) {
                cosmeticsFeatures.stopEffects(player);
                player.sendMessage(translateColorCodes(plugin.getConfig().getString("prefix", "") + plugin.getConfig().getString("messages.cosmetics.effects-disabled", "")));
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            } else if (guiTitle.equals(gadgetsTitle)) {
                cosmeticsFeatures.removeGadget(player);
                player.sendMessage(translateColorCodes(plugin.getConfig().getString("prefix", "") + plugin.getConfig().getString("messages.cosmetics.gadget-removed", "")));
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            }
            return;
        }

        // Feature aktivieren basierend auf Item-Typ durch Slot-Position
        String particlesTitle = translateColorCodes(plugin.getConfig().getString("shortprefix", "") + plugin.getConfig().getString("messages.cosmetics.gui.particles", ""));
        String effectsTitle = translateColorCodes(plugin.getConfig().getString("shortprefix", "") + plugin.getConfig().getString("messages.cosmetics.gui.effects", ""));
        String gadgetsTitle = translateColorCodes(plugin.getConfig().getString("shortprefix", "") + plugin.getConfig().getString("messages.cosmetics.gui.gadgets", ""));

        if (guiTitle.equals(particlesTitle)) {
            handleParticleActivation(player, slot);
        } else if (guiTitle.equals(effectsTitle)) {
            handleEffectActivation(player, slot);
        } else if (guiTitle.equals(gadgetsTitle)) {
            handleGadgetActivation(player, slot);
        }
    }

    /**
     * Behandelt Partikel-Aktivierung basierend auf Slot
     */
    private void handleParticleActivation(Player player, int slot) {
        String particleType = null;
        String permission = null;

        switch (slot) {
            case 10: // Heart
                particleType = "heart";
                permission = "lobbyengine.cosmetics.particles.heart";
                break;
            case 11: // Ender
                particleType = "ender";
                permission = "lobbyengine.cosmetics.particles.ender";
                break;
            case 12: // Lava
                particleType = "lava";
                permission = "lobbyengine.cosmetics.particles.lava";
                break;
            case 14: // Water
                particleType = "water";
                permission = "lobbyengine.cosmetics.particles.water";
                break;
            case 15: // Fire
                particleType = "fire";
                permission = "lobbyengine.cosmetics.particles.fire";
                break;
            case 16: // Rain Cloud
                particleType = "raincloud";
                permission = "lobbyengine.cosmetics.particles.raincloud";
                break;
        }

        if (particleType != null && permission != null) {
            if (player.hasPermission(permission)) {
                cosmeticsFeatures.activateParticles(player, particleType);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            } else {
                player.sendMessage(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.locked-message", "")));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
        }
    }

    /**
     * Behandelt Effekt-Aktivierung basierend auf Slot
     */
    private void handleEffectActivation(Player player, int slot) {
        String effectType = null;
        String permission = null;

        switch (slot) {
            case 10: // Fly
                effectType = "fly";
                permission = "lobbyengine.cosmetics.effects.fly";
                break;
            case 11: // Double Jump
                effectType = "doublejump";
                permission = "lobbyengine.cosmetics.effects.doublejump";
                break;
            case 12: // Jump Boost
                effectType = "jumpboost";
                permission = "lobbyengine.cosmetics.effects.jumpboost";
                break;
            case 14: // Night Vision
                effectType = "nightvision";
                permission = "lobbyengine.cosmetics.effects.nightvision";
                break;
            case 15: // Speed
                effectType = "speed";
                permission = "lobbyengine.cosmetics.effects.speed";
                break;
        }

        if (effectType != null && permission != null) {
            if (player.hasPermission(permission)) {
                cosmeticsFeatures.activateEffect(player, effectType);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            } else {
                player.sendMessage(translateColorCodes(plugin.getConfig().getString("messages.cosmetics.locked-message", "")));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
        }
    }

    /**
     * Behandelt Gadget-Aktivierung basierend auf Slot
     */
    private void handleGadgetActivation(Player player, int slot) {
        String gadgetType = null;
        String permission = null;

        switch (slot) {
            case 11: // Teleporter
                gadgetType = "teleporter";
                permission = "lobbyengine.cosmetics.gadgets.teleporter";
                break;
            case 13: // Love Bow
                gadgetType = "lovebow";
                permission = "lobbyengine.cosmetics.gadgets.lovebow";
                break;
            case 15: // Grapple Hook
                gadgetType = "grapplehook";
                permission = "lobbyengine.cosmetics.gadgets.grapplehook";
                break;
        }

        if (gadgetType != null && permission != null) {
            if (player.hasPermission(permission)) {
                cosmeticsFeatures.giveGadget(player, gadgetType);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
            } else {
                player.sendMessage(translateColorCodes(plugin.getConfig().getString("prefix", "") + plugin.getConfig().getString("messages.cosmetics.locked-message", "")));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
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