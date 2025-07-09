package me.sunrise.cosmeticsmanager.menus.browse;


import me.sunrise.cosmeticsmanager.menus.main.MenuItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;

public class ItemManager {

    private final Map<String, CosmeticData> cosmeticMap = new HashMap<>();
    private final YamlConfiguration cosmeticsConfig;
    private final Map<String, MenuItem> items = new LinkedHashMap<>();

    public ItemManager(YamlConfiguration cosmeticsConfig, YamlConfiguration menuConfig) {
        // Carrega lores e comando globais
        String noPermissionLoreKey = "no-permission";
        List<String> noPermissionLore = cosmeticsConfig.getStringList(noPermissionLoreKey);
        String cosmeticSelectedLoreKey = "selected";
        List<String> selectedLore = cosmeticsConfig.getStringList(cosmeticSelectedLoreKey);
        String onClickKey = "on-click";
        String onClickCommand = cosmeticsConfig.getString(onClickKey, "");
        this.cosmeticsConfig = cosmeticsConfig;

        // Carrega todos os cosmetics
        ConfigurationSection section = cosmeticsConfig.getConfigurationSection("cosmetics");
        if (section != null) {
            for (String id : section.getKeys(false)) {
                ConfigurationSection cosmeticSec = section.getConfigurationSection(id);
                if (cosmeticSec != null) {
                    String name = cosmeticSec.getString("name", "");
                    int data = cosmeticSec.getInt("data", 0);
                    String material = cosmeticSec.getString("material", "BARRIER");
                    List<String> lore = cosmeticSec.getStringList("lore");
                    String permission = cosmeticSec.getString("permission", "");

                    CosmeticData cosmeticData = new CosmeticData(
                            id,
                            name,
                            data,
                            material,
                            lore,
                            permission,
                            onClickCommand
                    );

                    cosmeticMap.put(id, cosmeticData);
                }
            }
        }

        // Carrega todos os botões
        var section1 = menuConfig.getConfigurationSection("settings.items");
        if (section1 != null) {
            for (String key : section1.getKeys(false)) {
                var sub = section1.getConfigurationSection(key);
                if (sub == null) continue;

                String color = sub.getString("color");

                String name = sub.getString("name");
                int data = sub.getInt("data");
                String material = sub.getString("material");
                List<String> lore = sub.getStringList("lore");
                int slot = sub.getInt("slot");
                String  onClick = sub.getString("on-click");


                items.put(key, new MenuItem(
                        key,
                        color,
                        name,
                        data,
                        material,
                        null,
                        null,
                        lore,
                        slot,
                        onClick
                ));
            }
        }

    }

    // Obtém todas as cosmetics carregadas
    public List<CosmeticData> getAllCosmetics() {
        return new ArrayList<>(cosmeticMap.values());
    }
    // Obtém todos os botões
    public Collection<MenuItem> getAllButtons() { return items.values(); }

    public YamlConfiguration getCosmeticsConfig() {return this.cosmeticsConfig;}


}
