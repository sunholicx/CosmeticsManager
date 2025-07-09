package me.sunrise.cosmeticsmanager.menus.chat_colors;

import dev.lone.itemsadder.api.CustomStack;
import me.sunrise.cosmeticsmanager.CosmeticsManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ChatColorMenu implements InventoryHolder {

    private final Player player;
    private final Inventory inventory;
    private final String menuTitle;
    private final String tex;

    public ChatColorMenu(CosmeticsManager plugin, Player player) {
        ChatColorsMenuConfig chatColorMenuConfig = plugin.getChatColorMenuConfig();

        this.player = player;
        tex = chatColorMenuConfig.getTexture();

        Component component = MiniMessage.miniMessage().deserialize(chatColorMenuConfig.getTitle());
        String title = LegacyComponentSerializer.legacySection().serialize(component);
        menuTitle = title;

        this.inventory = Bukkit.createInventory(this, chatColorMenuConfig.getSize(), title);

        loadItems(chatColorMenuConfig);
    }


    private void loadItems(ChatColorsMenuConfig chatColorsMenuConfig) {

        for (ChatColorItem item : chatColorsMenuConfig.getItems()) {
            // Verifica se o jogador tem permissão
            boolean hasPermission = item.getPermission() == null || player.hasPermission(item.getPermission());

            ItemStack stack;

            // Decide se é ItemsAdder ou item vanilla
            if (item.getData() == 1) {
                // ItemsAdder
                CustomStack cs = CustomStack.getInstance(item.getMaterial());
                if (cs != null) {
                    stack = cs.getItemStack().clone();
                } else {
                    // Item não encontrado no ItemsAdder
                    stack = new ItemStack(Material.BARRIER);
                }
            } else {
                // Item vanilla
                Material mat = Material.matchMaterial(item.getMaterial());
                stack = new ItemStack(mat != null ? mat : Material.BARRIER);
            }

            var meta = stack.getItemMeta();

            // Nome
            String miniMessageString = "<" + item.getColor() + ">" + item.getName();
            Component displayName = MiniMessage.miniMessage().deserialize(miniMessageString);
            meta.displayName(displayName);

            // Lore
            List<Component> loreComponents;
            if (hasPermission) {
                loreComponents = item.getLore().stream()
                        .map(MiniMessage.miniMessage()::deserialize)
                        .toList();
            } else {
                loreComponents = List.of(
                        MiniMessage.miniMessage().deserialize(item.getNoPermission())
                );
            }

            meta.lore(loreComponents);
            stack.setItemMeta(meta);

            // Define no slot correto
            inventory.setItem(item.getSlot(), stack);
        }
    }

    public void open() {
        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public  String getMenuTitle() {return menuTitle;}

    public  String getTexture() {return tex;}

}
