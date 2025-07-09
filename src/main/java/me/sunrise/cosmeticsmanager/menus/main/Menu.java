package me.sunrise.cosmeticsmanager.menus.main;

import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import dev.lone.itemsadder.api.FontImages.TexturedInventoryWrapper;
import me.sunrise.cosmeticsmanager.CosmeticsManager;

import me.sunrise.cosmeticsmanager.menus.browse.CosmeticData;
import me.sunrise.cosmeticsmanager.storage.PlayerCosmetics;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class Menu implements InventoryHolder {

    private final Player player;
    private final Inventory inventory;
    private final String menuTitle;
    private final String tex;
    private final String menuType;

    public Menu(CosmeticsManager plugin, Player player, MenuConfig config) {

        this.player = player;
        this.menuType = config.getConfig().getString("settings.type");
        tex = config.getTexture();

        Component component = MiniMessage.miniMessage().deserialize(config.getTitle());
        String title = LegacyComponentSerializer.legacySection().serialize(component);
        menuTitle = title;

        this.inventory = Bukkit.createInventory(this, config.getSize(), title);

        loadItems(plugin, config);
    }


    private void loadItems(CosmeticsManager plugin, MenuConfig menuConfig) {

        for (MenuItem item : menuConfig.getItems()) {

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


            // Verifica se o item é uma player head
            if (item.getData() == 2) {
                SkullMeta meta;
                meta = (SkullMeta) stack.getItemMeta();
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()));

                // Nome
                meta.displayName(formatItemName(item));

                // Lore
                List<Component>  loreComponents = formatItemLore(plugin, menuConfig, item);
                meta.lore(loreComponents);
                stack.setItemMeta(meta);

                // Define no slot correto
                inventory.setItem(item.getSlot(), stack);
            } else {
                var meta = stack.getItemMeta();

                // Nome
                meta.displayName(formatItemName(item));

                // Lore
                List<Component>  loreComponents = formatItemLore(plugin, menuConfig, item);
                meta.lore(loreComponents);
                stack.setItemMeta(meta);

                // Define no slot correto
                inventory.setItem(item.getSlot(), stack);
            }

        }
    }

    public void open() {
        player.openInventory(inventory);

        if (getTexture() == null || getTexture().isEmpty()) {
            return;
        }

        FontImageWrapper texture = new FontImageWrapper(getTexture());
        if(!texture.exists())
        {
            Bukkit.getConsoleSender().sendMessage("Textura " + getTexture() + " não encontrada.");
            return;
        }
        TexturedInventoryWrapper.setPlayerInventoryTexture(player, texture, getMenuTitle());
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public Component formatItemName(MenuItem item) {
        String miniMessageString;
        Component displayName;

        if (item.getColor().toString().startsWith("<gradient")) {
            miniMessageString = item.getColor() + item.getName() + "</gradient>";
        } else if (item.getColor().toString() == null || item.getColor().toString().isEmpty()) {
            miniMessageString = item.getName();
        } else {
            miniMessageString = "<" + item.getColor() + ">" + item.getName();
        }

        displayName = MiniMessage.miniMessage().deserialize(miniMessageString);

        return  displayName;
    }

    public List<Component> formatItemLore(CosmeticsManager plugin, MenuConfig menuConfig, MenuItem item) {

        // Verifica se o jogador tem permissão
        boolean hasPermission = item.getPermission() == null || player.hasPermission(item.getPermission());

        String path = "settings.items.actual.no-" ;

        List<Component> loreComponents = List.of();

        if (hasPermission) {
            PlayerCosmetics cosmetics = plugin.getCache().get(player.getUniqueId());
            if (cosmetics == null) {
                // Define valores padrão, ex:
                cosmetics = new PlayerCosmetics(null, null, null, "", 0);
            }

            String tagValue = cosmetics.getTag();
            if (tagValue == null || tagValue.isEmpty()) {
                tagValue = menuConfig.getConfig().getString(path + "tag");
            }
            String badgeValue = cosmetics.getBadge();
            String badge1 = badgeValue;
            // Se a textura existir aparece o emoji
            if (badgeValue == null || badgeValue.isEmpty()) {
                badgeValue = menuConfig.getConfig().getString(path + "badge");
            } else {
                CosmeticData badge = plugin.getBadgesManager().getAllCosmetics().stream()
                        .filter(i -> Objects.equals(getPlainName(i).toLowerCase(), badge1))
                        .findFirst()
                        .orElse(null);

                if (badge != null) {
                    FontImageWrapper badgeIcon = new FontImageWrapper(badge.getMaterial());
                    if (badgeIcon.exists()) {
                        badgeValue = badgeIcon.getString();
                    }
                }
            }

            String colorValue = cosmetics.getChatColor();
            if (colorValue == null || colorValue.isEmpty()) {
                colorValue = menuConfig.getConfig().getString(path + "color");
            }

            String finalTagValue = safe(tagValue);
            String finalBadgeValue = safe(badgeValue);
            String finalColorValue = safe(colorValue);

            if (finalColorValue.startsWith("<gradient")){
                loreComponents = item.getLore().stream()
                        .map(s -> s.replace("[Tag]", finalTagValue))
                        .map(s -> s.replace("[Badge]", finalBadgeValue))
                        .map(s -> s.replace("[Color]", finalColorValue))
                        .map(s -> s + "</gradient>")
                        .map(MiniMessage.miniMessage()::deserialize)
                        .toList();
            } else {
                loreComponents = item.getLore().stream()
                        .map(s -> s.replace("[Tag]", finalTagValue))
                        .map(s -> s.replace("[Badge]", finalBadgeValue))
                        .map(s -> s.replace("[Color]", finalColorValue))
                        .map(MiniMessage.miniMessage()::deserialize)
                        .toList();
            }
        } else {
            loreComponents.add(MiniMessage.miniMessage().deserialize(safe(menuConfig.getConfig().getString("settings.no-permission"))));
        }

        return loreComponents;
    }

    public  String getMenuTitle() {return menuTitle;}

    public  String getTexture() {return tex;}

    public String getMenuType() {return menuType;}

    private String safe(String input) {
        return input == null ? "" : input;
    }

    private String getPlainName(CosmeticData cosmetic) {
        return PlainTextComponentSerializer.plainText().serialize(
                MiniMessage.miniMessage().deserialize(
                        cosmetic.getName()
                )
        ).toLowerCase();
    }



}
