package me.sunrise.cosmeticsmanager.menus;

import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import dev.lone.itemsadder.api.FontImages.TexturedInventoryWrapper;
import me.sunrise.cosmeticsmanager.CosmeticsManager;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Menu implements InventoryHolder {

    private final Player player;
    private final Inventory inventory;
    private final String menuTitle;
    private final String texture;
    private final String menuType;

    public Menu(CosmeticsManager plugin, Player player, MenuConfig config) {
        this.player = player;
        this.menuType = config.getConfig().getString("settings.type");
        this.texture = config.getConfig().getString("settings.texture");

        Component component = MiniMessage.miniMessage().deserialize(config.getConfig().getString("settings.title"));
        this.menuTitle = LegacyComponentSerializer.legacySection().serialize(component);

        this.inventory = Bukkit.createInventory(this, config.getConfig().getInt("settings.size"), menuTitle);

        loadItems(plugin, config);
    }

    /**
     * Carrega os itens no inventário conforme a configuração
     */
    private void loadItems(CosmeticsManager plugin, MenuConfig menuConfig) {
        for (MenuItem item : menuConfig.getItems()) {
            ItemStack stack;

            // Define se é item vanilla ou ItemsAdder
            if (item.getData() == 1) {
                CustomStack cs = CustomStack.getInstance(item.getMaterial());
                stack = cs != null ? cs.getItemStack().clone() : new ItemStack(Material.BARRIER);
            } else {
                Material mat = Material.matchMaterial(item.getMaterial());
                stack = new ItemStack(mat != null ? mat : Material.BARRIER);
            }

            // Se for cabeça de jogador, configura meta especial
            if (item.getData() == 2) {
                SkullMeta meta = (SkullMeta) stack.getItemMeta();
                if (meta != null) {
                    meta.setOwningPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()));
                    meta.displayName(formatItemName(item));
                    meta.lore(formatItemLore(plugin, menuConfig, item));
                    stack.setItemMeta(meta);
                }
            } else {
                var meta = stack.getItemMeta();
                if (meta != null) {
                    meta.displayName(formatItemName(item));
                    meta.lore(formatItemLore(plugin, menuConfig, item));
                    stack.setItemMeta(meta);
                }
            }

            inventory.setItem(item.getSlot(), stack);
        }
    }

    /**
     * Abre o inventário para o jogador e aplica textura, se existir
     */
    public void open() {
        player.openInventory(inventory);

        if (texture == null || texture.isEmpty()) {
            return;
        }

        FontImageWrapper fontImage = new FontImageWrapper(texture);
        if (!fontImage.exists()) {
            Bukkit.getConsoleSender().sendMessage("Textura " + texture + " não encontrada.");
            return;
        }

        TexturedInventoryWrapper.setPlayerInventoryTexture(player, fontImage, menuTitle);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    /**
     * Formata o nome do item com cores e gradientes
     */
    public Component formatItemName(MenuItem item) {
        String color = item.getColor().toString();

        String miniMessageString;
        if (color.startsWith("<gradient")) {
            miniMessageString = color + item.getName() + "</gradient>";
        } else if (color == null || color.isEmpty() || color.equals("null")) {
            miniMessageString = item.getName();
        } else {
            miniMessageString = "<" + color + ">" + item.getName();
        }

        return MiniMessage.miniMessage().deserialize(miniMessageString);
    }

    /**
     * Formata a lore do item, considerando permissões e dados do jogador
     */
    public List<Component> formatItemLore(CosmeticsManager plugin, MenuConfig menuConfig, MenuItem item) {
        boolean hasPermission = item.getPermission() == null || player.hasPermission(item.getPermission());

        if (!hasPermission) {
            String noPermissionMsg = safe(menuConfig.getConfig().getString("settings.no-permission"));
            return List.of(MiniMessage.miniMessage().deserialize(noPermissionMsg));
        }

        PlayerCosmetics cosmetics = plugin.getCache().get(player.getUniqueId());
        if (cosmetics == null) {
            cosmetics = new PlayerCosmetics(null, null, null, "", 0);
        }

        String tagValue = cosmetics.getTag();
        if (tagValue == null || tagValue.isEmpty()) {
            tagValue = menuConfig.getConfig().getString("settings.items.actual.no-tag");
        }

        String badgeValue = cosmetics.getBadge();
        String badgeKey = badgeValue;
        if (badgeValue == null || badgeValue.isEmpty()) {
            badgeValue = menuConfig.getConfig().getString("settings.items.actual.no-badge");
        } else {
            MenuItem badge = plugin.getBadgesManager().getAllCosmetics().stream()
                    .filter(i -> Objects.equals(getPlainName(i).toLowerCase(), badgeKey))
                    .findFirst()
                    .orElse(null);
            if (badge != null) {
                badgeValue = badge.getName();
            }
        }

        String colorValue = cosmetics.getChatColor();
        if (colorValue == null || colorValue.isEmpty()) {
            colorValue = menuConfig.getConfig().getString("settings.items.actual.no-color");
        }

        String finalTag = safe(tagValue);
        String finalBadge = safe(badgeValue);
        String finalColor = safe(colorValue);

        List<String> loreTemplates = item.getLore();

        if (finalColor.startsWith("<gradient")) {
            return loreTemplates.stream()
                    .map(s -> s.replace("[Tag]", finalTag))
                    .map(s -> s.replace("[Badge]", finalBadge))
                    .map(s -> {
                        if (s.contains("[Color]")) {
                            return s.replace("[Color]", finalColor) + "</gradient>";
                        } else {
                            return s;
                        }
                    })
                    .map(MiniMessage.miniMessage()::deserialize)
                    .toList();
        } else {
            return loreTemplates.stream()
                    .map(s -> s.replace("[Tag]", finalTag))
                    .map(s -> s.replace("[Badge]", finalBadge))
                    .map(s -> s.replace("[Color]", finalColor))
                    .map(MiniMessage.miniMessage()::deserialize)
                    .toList();
        }
    }

    public String getMenuTitle() {
        return menuTitle;
    }

    public String getTexture() {
        return texture;
    }

    public String getMenuType() {
        return menuType;
    }

    private String safe(String input) {
        return input == null ? "" : input;
    }

    private String getPlainName(MenuItem cosmetic) {
        return PlainTextComponentSerializer.plainText().serialize(
                MiniMessage.miniMessage().deserialize(cosmetic.getEmoji())
        ).toLowerCase();
    }

}