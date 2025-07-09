package me.sunrise.cosmeticsmanager.menus.browse;

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

    public static ItemBuilder of(CosmeticData cosmetic) {
        ItemStack stack;
        if (cosmetic.getData() == 0) {
            stack = new ItemStack(Material.valueOf(cosmetic.getMaterial()));
        } else if (cosmetic.getData() == 1) {
            // ItemsAdder
            CustomStack customStack = CustomStack.getInstance(cosmetic.getMaterial());
            stack = customStack != null ? customStack.getItemStack().clone() : new ItemStack(Material.BARRIER);
        } else if (cosmetic.getData() == 2) {
            stack = new ItemStack(Material.PLAYER_HEAD);
        } else {
            stack = new ItemStack(Material.BARRIER);
        }
        return new ItemBuilder(stack);

    }

    public static ItemBuilder of(int data, String material, String name, List<String> lore) {
        ItemStack stack;
        if (data == 0) {
            stack = new ItemStack(Material.valueOf(material));
        } else if (data == 1) {
            CustomStack customStack = CustomStack.getInstance(material);
            stack = customStack != null ? customStack.getItemStack().clone() : new ItemStack(Material.BARRIER);
        } else if (data == 2) {
            stack = new ItemStack(Material.PLAYER_HEAD);
        } else {
            stack = new ItemStack(Material.BARRIER);
        }
        ItemBuilder builder = new ItemBuilder(stack);
        builder.setName(name);
        builder.setLore(lore);
        return builder;
    }

    public ItemBuilder setName(String name) {
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MiniMessage.miniMessage().deserialize(name));
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setLore(List<String> loreLines) {
        ItemMeta meta = item.getItemMeta();
        meta.lore(loreLines.stream()
                .map(line -> MiniMessage.miniMessage().deserialize(line))
                .toList()
        );
        item.setItemMeta(meta);
        return this;
    }

    public ItemStack build() {
        return item;
    }

}
