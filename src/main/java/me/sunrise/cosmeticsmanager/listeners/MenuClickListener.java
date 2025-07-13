package me.sunrise.cosmeticsmanager.listeners;

import me.sunrise.cosmeticsmanager.CosmeticsManager;
import me.sunrise.cosmeticsmanager.menus.BrowseMenu;
import me.sunrise.cosmeticsmanager.menus.Menu;
import me.sunrise.cosmeticsmanager.menus.MenuConfig;
import me.sunrise.cosmeticsmanager.menus.MenuItem;
import me.sunrise.cosmeticsmanager.storage.Cache;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class MenuClickListener implements Listener {

    private final CosmeticsManager plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public MenuClickListener(CosmeticsManager plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null) return;

        // Click em Menu principal
        if (event.getClickedInventory().getHolder() instanceof Menu menu) {
            event.setCancelled(true);
            handleMainMenuClick(player, menu, event.getCurrentItem(), event.getRawSlot());
        }
        // Click em BrowseMenu
        else if (event.getClickedInventory().getHolder() instanceof BrowseMenu browseMenu) {
            event.setCancelled(true);
            handleBrowseMenuClick(player, browseMenu, event.getCurrentItem(), event.getRawSlot());
        }
    }

    private void handleMainMenuClick(Player player, Menu menu, ItemStack clicked, int slot) {
        if (clicked == null || clicked.getType().isAir()) return;

        MenuConfig config;
        switch (menu.getMenuType().toLowerCase()) {
            case "color" -> config = plugin.getChatColorMenuConfig();
            case "tag" -> config = plugin.getTagsMenuConfig();
            case "badge" -> config = plugin.getBadgesMenuConfig();
            case "cosmetics" -> config = plugin.getCosmeticsMenuConfig();
            default -> config = null;
        }
        if (config == null) return;

        MenuItem clickedItem = config.getItems().stream()
                .filter(i -> i.getSlot() == slot)
                .findFirst()
                .orElse(null);
        if (clickedItem == null) return;

        if (!hasPermission(player, clickedItem.getPermission())) return;

        runMainMenuAction(player, clickedItem);
    }

    private void handleBrowseMenuClick(Player player, BrowseMenu browseMenu, ItemStack clicked, int slot) {
        if (clicked == null || clicked.getType().isAir()) return;

        MenuConfig itemManager = null;
        switch (browseMenu.getMenuType().toLowerCase()) {
            case "tag" -> itemManager = plugin.getTagManager();
            case "badge" -> itemManager = plugin.getBadgesManager();
        }
        if (itemManager == null) return;

        // Encontrar item cosmético pelo display name
        String rawName = PlainTextComponentSerializer.plainText().serialize(clicked.displayName()).trim();
        String displayName = rawName.length() > 2 ? rawName.substring(1, rawName.length() - 1) : rawName;

        MenuItem cosmeticItem = itemManager.getAllCosmetics().stream()
                .filter(i -> PlainTextComponentSerializer.plainText()
                        .serialize(miniMessage.deserialize(i.getName()))
                        .trim()
                        .equalsIgnoreCase(displayName))
                .findFirst()
                .orElse(null);

        // Encontrar botão pelo slot
        MenuItem buttonItem = itemManager.getAllButtons().stream()
                .filter(i -> i.getSlot() == slot)
                .findFirst()
                .orElse(null);

        // Prioridade: cosmético
        if (cosmeticItem != null) {
            if (!hasPermission(player, cosmeticItem.getPermission())) return;
            runBrowseMenuAction(player, browseMenu.getMenuType(), cosmeticItem.getOnClick(), displayName);
        } else if (buttonItem != null) {
            if (!hasPermission(player, buttonItem.getPermission())) return;
            runBrowseMenuAction(player, browseMenu.getMenuType(), buttonItem.getOnClick(), "");
        }
    }

    private boolean hasPermission(Player player, String permission) {
        if (permission == null || permission.isEmpty()) return true;
        if (!player.hasPermission(permission)) {
            player.sendMessage(miniMessage.deserialize(plugin.getConfig().getString("settings.listeners.no-permission", "")));
            return false;
        }
        return true;
    }

    private void runMainMenuAction(Player player, MenuItem item) {
        String onClick = item.getOnClick();
        player.closeInventory();

        switch (onClick.toLowerCase()) {
            case "changecolor" -> player.performCommand("chatcolor set " + item.getColor());
            case "changegradient" -> player.performCommand("chatcolor gradient");
            case "cosmeticsmenu" -> player.performCommand("cosmetics");
            case "tagsmenu" -> player.performCommand("tags");
            case "badgesmenu" -> player.performCommand("badges");
            case "chatcolorsmenu" -> player.performCommand("chatcolor");
            case "mytags" -> openBrowseMenu(player, "tags owned", "my");
            case "alltags" -> openBrowseMenu(player, "tags all", "all");
            case "blockedtags" -> openBrowseMenu(player, "tags blocked", "blocked");
            case "mybadges" -> player.performCommand("badges owned");
            case "allbadges" -> player.performCommand("badges all");
            case "blockedbadges" -> player.performCommand("badges blocked");
        }
    }

    private void runBrowseMenuAction(Player player, String menuType, String onClick, String cosmeticName) {
        player.closeInventory();
        Cache cache = plugin.getCache();
        String type = cache.get(player.getUniqueId()).getMenuType();
        int page = cache.get(player.getUniqueId()).getPage();

        if (type == null || type.isEmpty()) {
            player.sendMessage("§cSua sessão expirou. Abra o menu novamente.");
            return;
        }

        switch (onClick.toLowerCase()) {
            case "tagsmenu" -> player.performCommand("tags");
            case "badgesmenu" -> player.performCommand("badges");
            case "backpage" -> openBrowsePage(player, menuType, type, page - 1);
            case "nextpage" -> openBrowsePage(player, menuType, type, page + 1);
            case "settag" -> player.performCommand("tags set " + cosmeticName);
            case "setbadge" -> player.performCommand("badges set " + cosmeticName);
        }
    }

    private void openBrowseMenu(Player player, String command, String menuType) {
        plugin.getCache().setMenuType(player.getUniqueId(), menuType);
        player.performCommand(command);
    }

    private void openBrowsePage(Player player, String cosmeticMenu, String type, int page) {
        MenuConfig itemManager = null;
        switch (cosmeticMenu.toLowerCase()) {
            case "tag" -> itemManager = plugin.getTagManager();
            case "badge" -> itemManager = plugin.getBadgesManager();
        }

        YamlConfiguration menuConfig = null;
        switch (cosmeticMenu.toLowerCase()) {
            case "tag" -> menuConfig = plugin.getBrowseTagsYml();
            case "badge" -> menuConfig = plugin.getBrowseBadgesYml();
        }

        BrowseMenu menu = new BrowseMenu(plugin, player, type, page, itemManager, menuConfig);
        plugin.getCache().setMenuType(player.getUniqueId(), type);
        plugin.getCache().setPage(player.getUniqueId(), page);
        menu.open();
    }

}