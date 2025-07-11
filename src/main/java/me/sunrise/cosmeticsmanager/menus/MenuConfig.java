package me.sunrise.cosmeticsmanager.menus;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;

public class MenuConfig {

    private final Map<String, MenuItem> items = new LinkedHashMap<>();
    private final Map<String, MenuItem> cosmetics = new LinkedHashMap<>();
    private final Map<String, MenuItem> buttons = new LinkedHashMap<>();
    private final YamlConfiguration menuConfig;
    private final YamlConfiguration cosmeticsConfig;

    /**
     * Construtor para menus principais
     */
    public MenuConfig(YamlConfiguration menuConfig) {
        this.menuConfig = menuConfig;
        this.cosmeticsConfig = null;

        String noPermission = menuConfig.getString("settings.no-permission", "");

        ConfigurationSection itemsSection = menuConfig.getConfigurationSection("settings.items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                ConfigurationSection subSection = itemsSection.getConfigurationSection(key);
                if (subSection == null) continue;

                String color = subSection.getString("color");
                String name = subSection.getString("name");
                int data = subSection.getInt("data");
                String material = subSection.getString("material");
                List<String> lore = subSection.getStringList("lore");
                int slot = subSection.getInt("slot");
                // Corrigido para pegar a permissão do subSection, e não do menuConfig raiz
                String permission = subSection.getString("permission", null);
                String onClick = subSection.getString("on-click");

                items.put(key, new MenuItem(
                        key,
                        color,
                        name,
                        data,
                        material,
                        permission,
                        noPermission,
                        lore,
                        slot,
                        onClick
                ));
            }
        }
    }

    /**
     * Construtor para menus de visualização de cosméticos
     */
    public MenuConfig(YamlConfiguration cosmeticsConfig, YamlConfiguration menuConfig) {
        this.menuConfig = menuConfig;
        this.cosmeticsConfig = cosmeticsConfig;

        String onClickCommand = cosmeticsConfig.getString("on-click", "");

        // Carrega os cosméticos
        ConfigurationSection cosmeticsSection = cosmeticsConfig.getConfigurationSection("cosmetics");
        if (cosmeticsSection != null) {
            for (String id : cosmeticsSection.getKeys(false)) {
                ConfigurationSection cosmeticSec = cosmeticsSection.getConfigurationSection(id);
                if (cosmeticSec == null) continue;

                String name = cosmeticSec.getString("name", "");
                int data = cosmeticSec.getInt("data", 0);
                String material = cosmeticSec.getString("material", "BARRIER");
                List<String> lore = cosmeticSec.getStringList("lore");
                String permission = cosmeticSec.getString("permission", "");
                String emoji = cosmeticSec.getString("emoji", "");

                MenuItem cosmeticItem = new MenuItem(
                        id,
                        name,
                        data,
                        material,
                        lore,
                        permission,
                        onClickCommand,
                        emoji
                );

                cosmetics.put(id, cosmeticItem);
            }
        }

        // Carrega os botões
        ConfigurationSection buttonsSection = menuConfig.getConfigurationSection("settings.items");
        if (buttonsSection != null) {
            for (String key : buttonsSection.getKeys(false)) {
                ConfigurationSection subSection = buttonsSection.getConfigurationSection(key);
                if (subSection == null) continue;

                String color = subSection.getString("color");
                String name = subSection.getString("name");
                int data = subSection.getInt("data");
                String material = subSection.getString("material");
                List<String> lore = subSection.getStringList("lore");
                int slot = subSection.getInt("slot");
                String onClick = subSection.getString("on-click");

                buttons.put(key, new MenuItem(
                        key,
                        color,
                        name,
                        data,
                        material,
                        lore,
                        slot,
                        onClick
                ));
            }
        }
    }

    // Getters
    public YamlConfiguration getConfig() {
        return menuConfig;
    }

    public YamlConfiguration getCosmeticsConfig() {
        return cosmeticsConfig;
    }

    public Collection<MenuItem> getItems() {
        return items.values();
    }

    public List<MenuItem> getAllCosmetics() {
        return new ArrayList<>(cosmetics.values());
    }

    public Collection<MenuItem> getAllButtons() {
        return buttons.values();
    }
}