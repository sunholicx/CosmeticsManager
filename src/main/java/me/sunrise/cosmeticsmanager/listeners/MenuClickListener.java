package me.sunrise.cosmeticsmanager.listeners;

import me.sunrise.cosmeticsmanager.CosmeticsManager;
import me.sunrise.cosmeticsmanager.menus.main.MenuItem;
import me.sunrise.cosmeticsmanager.menus.main.Menu;
import me.sunrise.cosmeticsmanager.menus.main.MenuConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ChatColorMenuClickListener implements Listener {

    private final CosmeticsManager plugin;

    public ChatColorMenuClickListener(CosmeticsManager plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null) return;
        if (!(event.getClickedInventory().getHolder() instanceof Menu)) return;


        event.setCancelled(true); // bloqueia pegar os itens

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        MenuConfig config = plugin.getChatColorMenuConfig();

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
                            plugin.getConfigYML().getString("settings.listeners.color-menu.no-permission")
                    )
            );
            return;
        }

        // Executa o comando se existir
        String onClick = clickedItem.getOnClick();

        if (onClick.equalsIgnoreCase("changeColor")) {
            player.performCommand("chatcolor set " + clickedItem.getColor());
            player.closeInventory();
            return;
        } else if (onClick.equalsIgnoreCase("changeGradient")) {
            player.performCommand("chatcolor gradient");
            player.closeInventory();
            return;
        }



    }
}
