package me.sunrise.cosmeticsmanager.menus;

import dev.lone.itemsadder.api.CustomStack;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemBuilder {

    private final ItemStack item;

    private ItemBuilder(ItemStack item) {
        this.item = item;
    }

    /**
     * Constrói ItemStack para badges e tags com base no MenuItem
     */
    public static ItemBuilder of(MenuItem cosmetic) {
        ItemStack stack;

        switch (cosmetic.getData()) {
            case 0 -> stack = new ItemStack(Material.valueOf(cosmetic.getMaterial()));
            case 1 -> {
                // Suporte a ItemsAdder
                var customStack = CustomStack.getInstance(cosmetic.getMaterial());
                stack = customStack != null ? customStack.getItemStack().clone() : new ItemStack(Material.BARRIER);
            }
            default -> stack = new ItemStack(Material.BARRIER);
        }

        return new ItemBuilder(stack);
    }

    /**
     * Constrói ItemStack para botões, com nome e lore personalizados
     */
    public static ItemBuilder of(int data, String material, String name, List<String> lore) {
        ItemStack stack;

        switch (data) {
            case 0 -> stack = new ItemStack(Material.valueOf(material));
            case 1 -> {
                var customStack = CustomStack.getInstance(material);
                stack = customStack != null ? customStack.getItemStack().clone() : new ItemStack(Material.BARRIER);
            }
            case 2 -> stack = new ItemStack(Material.PLAYER_HEAD);
            default -> stack = new ItemStack(Material.BARRIER);
        }

        ItemBuilder builder = new ItemBuilder(stack);
        builder.setName(name);
        builder.setLore(lore);
        return builder;
    }

    /**
     * Define o nome do item usando MiniMessage para formatação
     */
    public ItemBuilder setName(String name) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MiniMessage.miniMessage().deserialize(name));
            item.setItemMeta(meta);
        }
        return this;
    }

    /**
     * Define a lore do item, convertendo cada linha com MiniMessage
     */
    public ItemBuilder setLore(List<String> loreLines) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.lore(loreLines.stream()
                    .map(line -> MiniMessage.miniMessage().deserialize(line))
                    .toList()
            );
            item.setItemMeta(meta);
        }
        return this;
    }

    /**
     * Retorna o ItemStack final construído
     */
    public ItemStack build() {
        return item;
    }
}