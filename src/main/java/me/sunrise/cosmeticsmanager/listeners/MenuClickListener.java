package me.sunrise.cosmeticsmanager.listeners;

import me.sunrise.cosmeticsmanager.CosmeticsManager;
import me.sunrise.cosmeticsmanager.menus.browse.BrowseMenu;
import me.sunrise.cosmeticsmanager.menus.browse.CosmeticData;
import me.sunrise.cosmeticsmanager.menus.browse.ItemManager;
import me.sunrise.cosmeticsmanager.menus.main.MenuItem;
import me.sunrise.cosmeticsmanager.menus.main.Menu;
import me.sunrise.cosmeticsmanager.menus.main.MenuConfig;
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

    public MenuClickListener(CosmeticsManager plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null) return;


        String onClick;

        if ((event.getClickedInventory().getHolder() instanceof Menu menu)) {
            event.setCancelled(true); // bloqueia pegar os itens

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType().isAir()) return;

            MenuConfig config = getMenuConfig(menu.getMenuType());
            if (config == null) return;

            // Descobre qual item foi clicado pelo slot
            int slot = event.getRawSlot();
            MenuItem clickedItem = config.getItems().stream()
                    .filter(i -> i.getSlot() == slot)
                    .findFirst()
                    .orElse(null);

            if (clickedItem == null) return;

            // Verifica permissão
            if (clickedItem.getPermission() != null && !player.hasPermission(clickedItem.getPermission())) {
                player.sendMessage(
                        MiniMessage.miniMessage().deserialize(
                                plugin.getConfig().getString("settings.listeners.no-permission")
                        )
                );
                return;
            }

            onClick = clickedItem.getOnClick();

            mainMenuCommands(onClick, clickedItem, player);

        } else if ((event.getClickedInventory().getHolder() instanceof BrowseMenu browseMenu)) {
            event.setCancelled(true); // bloqueia pegar os itens

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType().isAir()) return;

            ItemManager itemManager = getItemManager(browseMenu.getMenuType());
            if (itemManager == null) return;

            // Descobre qual item foi clicado pelo nome
            String displayName0 = PlainTextComponentSerializer.plainText().serialize(clicked.displayName()).trim();
            String displayName = displayName0.substring(1, displayName0.length() - 1);

            CosmeticData clickedItem1 = itemManager.getAllCosmetics().stream()
                    .filter(i -> {
                        String name = PlainTextComponentSerializer.plainText().serialize(
                                MiniMessage.miniMessage().deserialize(i.getName())
                        ).trim();
                        return name.equalsIgnoreCase(displayName);
                    })
                    .findFirst()
                    .orElse(null);


            int slot = event.getRawSlot();
            MenuItem clickedItem2 = itemManager.getAllButtons().stream()
                    .filter(i -> i.getSlot() == slot)
                    .findFirst()
                    .orElse(null);

            // Verifica se o item clicado é um cosmético
            if (clickedItem1 != null){
                // Verifica permissão
                if (clickedItem1.getPermission() != null && !clickedItem1.getPermission().isEmpty()) {

                    if (!(player.hasPermission(clickedItem1.getPermission()))) {
                        player.sendMessage(
                                MiniMessage.miniMessage().deserialize(
                                        plugin.getConfig().getString("settings.listeners.no-permission")
                                )
                        );
                        return;
                    }
                }

                onClick = clickedItem1.getOnClick();
                browseMenuCommands(onClick, player, browseMenu.getMenuType(), displayName);

            } else if (clickedItem2 != null){
                // Verifica permissão
                if (clickedItem2.getPermission() != null && !player.hasPermission(clickedItem2.getPermission())) {
                    player.sendMessage(
                            MiniMessage.miniMessage().deserialize(
                                    plugin.getConfig().getString("settings.listeners.no-permission")
                            )
                    );
                    return;
                }

                onClick = clickedItem2.getOnClick();
                browseMenuCommands(onClick, player, browseMenu.getMenuType(), "");

            } else return;

        } else return;


    }

    private MenuConfig getMenuConfig(String type) {
        if (type.equalsIgnoreCase("color")) {
            return plugin.getChatColorMenuConfig();
        } else if (type.equalsIgnoreCase("tag")) {
            return plugin.getTagsMenuConfig();
        } else if (type.equalsIgnoreCase("badge")) {
            return plugin.getBadgesMenuConfig();
        } else if (type.equalsIgnoreCase("cosmetics")) {
            return plugin.getCosmeticsMenuConfig();
        }
        return null;
    }

    private ItemManager getItemManager(String type) {
        if (type.equalsIgnoreCase("tag")) {
            return plugin.getTagManager();
        } else if (type.equalsIgnoreCase("badge")) {
            return plugin.getBadgesManager();
        }
        return null;
    }

    private YamlConfiguration getBrowseMenuConfig(String type) {
        if (type.equalsIgnoreCase("tag")) {
            return plugin.getBrowseTagsYml();
        } else if (type.equalsIgnoreCase("badge")) {
            return plugin.getBrowseBadgesYml();
        }
        return null;
    }

    private void mainMenuCommands(String onClick, MenuItem clickedItem, Player player) {
        if (onClick.equalsIgnoreCase("changeColor")) {
            player.performCommand("chatcolor set " + clickedItem.getColor());
            player.closeInventory();
        } else if (onClick.equalsIgnoreCase("changeGradient")) {
            player.performCommand("chatcolor gradient");
            player.closeInventory();
        } else if (onClick.equalsIgnoreCase("cosmeticsMenu")) {
            player.closeInventory();
            player.performCommand("cosmetics");
        } else if (onClick.equalsIgnoreCase("tagsMenu")) {
            player.closeInventory();
            player.performCommand("tags");
        } else if (onClick.equalsIgnoreCase("badgesMenu")) {
            player.closeInventory();
            player.performCommand("badges");
        } else if (onClick.equalsIgnoreCase("chatColorsMenu")) {
            player.closeInventory();
            player.performCommand("chatcolor");
        } else if (onClick.equalsIgnoreCase("myTags")) {
            Cache cache = plugin.getCache();
            cache.setMenuType(player.getUniqueId(), "my");
            player.closeInventory();
            player.performCommand("tags owned");
        } else if (onClick.equalsIgnoreCase("allTags")) {
            Cache cache = plugin.getCache();
            cache.setMenuType(player.getUniqueId(), "all");
            player.closeInventory();
            player.performCommand("tags all");
        } else if (onClick.equalsIgnoreCase("blockedTags")) {
            Cache cache = plugin.getCache();
            cache.setMenuType(player.getUniqueId(), "blocked");
            player.closeInventory();
            player.performCommand("tags blocked");
        } else if (onClick.equalsIgnoreCase("myBadges")) {
            player.closeInventory();
            player.performCommand("badges owned");
        } else if (onClick.equalsIgnoreCase("allBadges")) {
            player.closeInventory();
            player.performCommand("badges all");
        } else if (onClick.equalsIgnoreCase("blockedBadges")) {
            player.closeInventory();
            player.performCommand("badges blocked");
        }

    }

    private void browseMenuCommands(String onClick, Player player, String cosmeticMenu, String cosmeticName) {
        if (onClick.equalsIgnoreCase("tagsMenu")) {
            player.closeInventory();
            player.performCommand("tags");
        } else if (onClick.equalsIgnoreCase("badgesMenu")) {
            player.closeInventory();
            player.performCommand("badges");
        } else if (onClick.equalsIgnoreCase("backPage")) {
            player.closeInventory();

            Cache cache = plugin.getCache();
            String type = cache.get(player.getUniqueId()).getMenuType();
            int page = cache.get(player.getUniqueId()).getPage();
            page = page - 1;
            ItemManager itemManager = getItemManager(cosmeticMenu);
            YamlConfiguration menuConfig = getBrowseMenuConfig(cosmeticMenu);
            BrowseMenu menu = new BrowseMenu(plugin, player, type, page, itemManager, menuConfig);
            cache.setPage(player.getUniqueId(), page);
            menu.open();


        } else if (onClick.equalsIgnoreCase("nextPage")) {
            player.closeInventory();
            Cache cache = plugin.getCache();
            String type = cache.get(player.getUniqueId()).getMenuType();
            int page = cache.get(player.getUniqueId()).getPage();
            page = page + 1;
            ItemManager itemManager = getItemManager(cosmeticMenu);
            YamlConfiguration menuConfig = getBrowseMenuConfig(cosmeticMenu);
            BrowseMenu menu = new BrowseMenu(plugin, player, type, page, itemManager, menuConfig);
            cache.setPage(player.getUniqueId(), page);
            menu.open();

        } else if (onClick.equalsIgnoreCase("setTag")) {
            player.closeInventory();
            player.performCommand("tags set " + cosmeticName);
        } else if (onClick.equalsIgnoreCase("setBadge")) {
            player.closeInventory();
            player.performCommand("badges set " + cosmeticName);
        }


    }

}
