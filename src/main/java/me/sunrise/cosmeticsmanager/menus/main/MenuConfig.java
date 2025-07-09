package me.sunrise.cosmeticsmanager.menus.main;



import org.bukkit.configuration.file.YamlConfiguration;


import java.util.*;

public class MenuConfig {

    private final String title;
    private final String texture;
    private final int size;
    private final Map<String, MenuItem> items = new LinkedHashMap<>();
    private final YamlConfiguration config;

    public MenuConfig(YamlConfiguration menuConfig) {
        config = menuConfig;

        title = menuConfig.getString("settings.title", "Menu");
        texture = menuConfig.getString("settings.texture", "");
        size = menuConfig.getInt("settings.size", 27);
        String noPermission = menuConfig.getString("settings.no-permission", null);



        var section = menuConfig.getConfigurationSection("settings.items");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                var sub = section.getConfigurationSection(key);
                if (sub == null) continue;

                String color = sub.getString("color");

                String name = sub.getString("name");
                int data = sub.getInt("data");
                String material = sub.getString("material");
                List<String> lore = sub.getStringList("lore");
                int slot = sub.getInt("slot");
                String permission = menuConfig.getString("permission", null);
                String  onClick = sub.getString("on-click");


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

    public YamlConfiguration getConfig() { return config; }
    public String getTitle() { return title; }
    public String getTexture() { return texture; }
    public int getSize() { return size; }
    public Collection<MenuItem> getItems() { return items.values(); }
}
