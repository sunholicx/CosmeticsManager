package me.sunrise.cosmeticsmanager.menus;

import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import dev.lone.itemsadder.api.FontImages.TexturedInventoryWrapper;
import me.sunrise.cosmeticsmanager.CosmeticsManager;
import me.sunrise.cosmeticsmanager.storage.PlayerCosmetics;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BrowseMenu implements InventoryHolder {

    private Inventory inventory;

    private final CosmeticsManager plugin;
    private final Player player;
    private final String type;
    private final String menuType;
    private final int page;
    private final int size;
    private final String menuTitle;

    private final List<String> emptySlots;
    private final YamlConfiguration config;
    private final List<MenuItem> cosmetics;

    public BrowseMenu(
            CosmeticsManager plugin,
            Player player,
            String type,
            int page,
            MenuConfig itemManager,
            YamlConfiguration browseConfig
    ) {
        this.plugin = plugin;
        this.player = player;
        this.type = type;
        this.page = page;
        this.config = browseConfig;
        this.cosmetics = itemManager.getAllCosmetics();
        this.emptySlots = config.getStringList("settings.empty-spaces");
        this.size = config.getInt("settings.size", 27);
        this.menuType = config.getString("settings.type");

        String titleTemplate = config.getString("settings." + type, "");
        String pageSuffix = config.getString("settings.page", " - Página [n]");

        Component component = MiniMessage.miniMessage().deserialize(
                titleTemplate + pageSuffix.replace("[n]", String.valueOf(page))
        );
        this.menuTitle = LegacyComponentSerializer.legacySection().serialize(component);
    }

    /**
     * Abre o menu para o player
     */
    public void open() {
        inventory = Bukkit.createInventory(this, size, menuTitle);

        List<MenuItem> filteredCosmetics = getFilteredCosmetics();

        int cosmeticsPerPage = size - emptySlots.size() - 3;
        int startIndex = (page - 1) * cosmeticsPerPage;
        int endIndex = Math.min(startIndex + cosmeticsPerPage, filteredCosmetics.size());

        List<MenuItem> pageCosmetics = filteredCosmetics.subList(startIndex, endIndex);

        // Preenche slots com cosméticos
        int slot = 0;
        for (MenuItem cosmetic : pageCosmetics) {
            while (emptySlots.contains(String.valueOf(slot)) && slot < size) {
                slot++;
            }
            inventory.setItem(slot, buildCosmeticItem(cosmetic));
            slot++;
        }

        // Botões de navegação
        if (page > 1) {
            inventory.setItem(
                    config.getInt("settings.items.back.slot"),
                    buildButton("back")
            );
        }
        if (endIndex < filteredCosmetics.size()) {
            inventory.setItem(
                    config.getInt("settings.items.next.slot"),
                    buildButton("next")
            );
        }
        inventory.setItem(
                config.getInt("settings.items.menu.slot"),
                buildButton("menu")
        );

        player.openInventory(inventory);

        // Textura do ItemsAdder (opcional)
        String texture = config.getString("settings.texture");
        if (texture != null && !texture.isEmpty()) {
            FontImageWrapper tex = new FontImageWrapper(texture);
            if (tex.exists()) {
                TexturedInventoryWrapper.setPlayerInventoryTexture(player, tex, menuTitle);
            } else {
                Bukkit.getConsoleSender().sendMessage("Textura " + texture + " não encontrada.");
            }
        }
    }

    /**
     * Filtra os cosméticos de acordo com o tipo selecionado
     */
    private List<MenuItem> getFilteredCosmetics() {
        return cosmetics.stream()
                .filter(cosmetic -> {
                    String permission = cosmetic.getPermission();
                    boolean hasPerm = permission == null || permission.isEmpty() || player.hasPermission(permission);

                    switch (type.toLowerCase()) {
                        case "all":
                            return true;
                        case "my":
                            return hasPerm;
                        case "blocked":
                            return !hasPerm;
                        default:
                            return false;
                    }
                })
                .sorted(Comparator.comparing(this::getPlainName))
                .collect(Collectors.toList());
    }

    /**
     * Cria o item visual do cosmético
     */
    private ItemStack buildCosmeticItem(MenuItem cosmetic) {
        boolean hasPermission = cosmetic.getPermission() == null
                || cosmetic.getPermission().isEmpty()
                || player.hasPermission(cosmetic.getPermission());

        List<String> lore = new ArrayList<>();
        if (!hasPermission) {
            lore.add(config.getString("settings.no-permission"));
        } else {
            lore.addAll(cosmetic.getLore());
        }

        PlayerCosmetics playerCosmetics = plugin.getCache().get(player.getUniqueId());
        String playerTag = safe(playerCosmetics.getTag());
        String playerBadge = safe(playerCosmetics.getBadge());

        if (!playerTag.isEmpty() || !playerBadge.isEmpty()) {
            if (playerTag.equalsIgnoreCase(safe(cosmetic.getName()))
                    || playerBadge.equalsIgnoreCase(safe(cosmetic.getEmoji()))) {
                lore.clear();
                lore.addAll(plugin.getTagsYml().getStringList("selected"));
            }
        }

        return ItemBuilder.of(cosmetic)
                .setName(cosmetic.getName())
                .setLore(lore)
                .build();
    }

    /**
     * Cria um botão padrão do menu
     */
    private ItemStack buildButton(String key) {
        String path = "settings.items." + key;
        return ItemBuilder.of(
                config.getInt(path + ".data"),
                config.getString(path + ".material"),
                config.getString(path + ".name"),
                config.getStringList(path + ".lore")
        ).build();
    }

    /**
     * Retorna o tipo do menu (tag/badge)
     */
    public String getMenuType() {
        return menuType;
    }

    /**
     * Retorna string segura (nunca null)
     */
    private String safe(String input) {
        return input == null ? "" : input;
    }

    /**
     * Obtém o nome em texto puro para ordenação
     */
    private String getPlainName(MenuItem cosmetic) {
        return PlainTextComponentSerializer.plainText().serialize(
                MiniMessage.miniMessage().deserialize(
                        safe(cosmetic.getName()).replace("[", "").replace("]", "")
                )
        ).toLowerCase();
    }

    @Override
    @NotNull
    public Inventory getInventory() {
        return inventory;
    }
}