package me.sunrise.cosmeticsmanager.menus;

import me.sunrise.cosmeticsmanager.CosmeticsManager;
import me.sunrise.cosmeticsmanager.utils.ItemBuilder;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class BrowseTagsMenu {

    private final CosmeticsManager plugin;
    private final Player player;
    private final String type; // "my", "all", "blocked"
    private final int page;
    private final List<String> emptySlots;
    private final int size;
    private final String menuTitle;

    private final Map<String, TagData> tags;

    public BrowseTagsMenu(CosmeticsManager plugin, Player player, String type, int page) {
        this.plugin = plugin;
        this.player = player;
        this.type = type;
        this.page = page;
        this.tags = plugin.getTagManager().getAllTags(); // Carrega as tags
        this.emptySlots = plugin.getBrowseTagsConfig().getStringList("settings.empty-spaces");
        this.size = plugin.getBrowseTagsConfig().getInt("settings.size");

        String titleTemplate = plugin.getBrowseTagsConfig().getString("settings." + type, "Tags");
        String pageSuffix = plugin.getBrowseTagsConfig().getString("settings.page", " - Página [n]");
        this.menuTitle = MiniMessage.miniMessage().deserialize(
                titleTemplate + pageSuffix.replace("[n]", String.valueOf(page))
        ).toString();
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, size, menuTitle);

        // Filtrar as tags de acordo com o tipo
        List<TagData> filteredTags = getFilteredTags();

        // Paginar
        int tagsPerPage = size - emptySlots.size() - 3; // Reservando slots de botões
        int startIndex = (page - 1) * tagsPerPage;
        int endIndex = Math.min(startIndex + tagsPerPage, filteredTags.size());
        List<TagData> pageTags = filteredTags.subList(startIndex, endIndex);

        // Preencher itens de tags
        int slot = 0;
        for (TagData tag : pageTags) {
            while (emptySlots.contains(String.valueOf(slot)) && slot < size) {
                slot++;
            }
            inv.setItem(slot, buildTagItem(tag));
            slot++;
        }

        // Botão de voltar página
        if (page > 1) {
            inv.setItem(
                    plugin.getBrowseTagsConfig().getInt("settings.items.back.slot"),
                    buildButton("back")
            );
        }
        // Botão de próxima página
        if (endIndex < filteredTags.size()) {
            inv.setItem(
                    plugin.getBrowseTagsConfig().getInt("settings.items.next.slot"),
                    buildButton("next")
            );
        }
        // Botão de menu
        inv.setItem(
                plugin.getBrowseTagsConfig().getInt("settings.items.menu.slot"),
                buildButton("menu")
        );

        player.openInventory(inv);
    }

    private List<TagData> getFilteredTags() {
        return tags.values().stream()
                .filter(tag -> {
                    boolean hasPermission = tag.permission == null || tag.permission.isEmpty() || player.hasPermission(tag.permission);
                    if (type.equalsIgnoreCase("my")) return hasPermission;
                    if (type.equalsIgnoreCase("blocked")) return !hasPermission;
                    return true; // "all"
                })
                .collect(Collectors.toList());
    }

    private ItemStack buildTagItem(TagData tag) {
        boolean hasPermission = tag.permission == null || tag.permission.isEmpty() || player.hasPermission(tag.permission);
        List<String> lore = new ArrayList<>();
        if (!hasPermission) {
            lore.add(plugin.getBrowseTagsConfig().getString("settings.no-permission"));
        } else {
            lore.addAll(tag.lore);
        }
        return ItemBuilder.of(tag)
                .setName(tag.tag)
                .setLore(lore)
                .build();
    }

    private ItemStack buildButton(String key) {
        String path = "settings.items." + key;
        return ItemBuilder.of(
                plugin.getBrowseTagsConfig().getInt(path + ".data"),
                plugin.getBrowseTagsConfig().getString(path + ".material"),
                plugin.getBrowseTagsConfig().getString(path + ".name"),
                plugin.getBrowseTagsConfig().getStringList(path + ".lore")
        ).build();
    }
}
