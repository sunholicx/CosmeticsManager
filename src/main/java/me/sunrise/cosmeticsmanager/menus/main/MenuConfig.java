package me.sunrise.cosmeticsmanager.menus.chat_colors;


import me.sunrise.cosmeticsmanager.CosmeticsManager;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class ChatColorsMenuConfig {

    private final String title;
    private final String texture;
    private final int size;
    private final List<Integer> emptySlots;
    private final Map<String, ChatColorItem> items = new LinkedHashMap<>();

    public ChatColorsMenuConfig(CosmeticsManager plugin) {
        File file = new File(plugin.getDataFolder(), "guis/chat-colors-menu.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        File file1 = new File(plugin.getDataFolder(), "cosmetics/chat-colors.yml");
        YamlConfiguration chatColors = YamlConfiguration.loadConfiguration(file1);

        title = config.getString("settings.title", "<green>Chat Colors");
        texture = config.getString("settings.texture", "");
        size = config.getInt("settings.size", 54);
        emptySlots = config.getIntegerList("settings.empty-slots");
        String noPermission = config.getString("settings.no-permission", null);



        var section = config.getConfigurationSection("settings.colors-items");
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
                String permission = chatColors.getString("colors." + key + ".permission", null);
                String  onClick = sub.getString("on-click");


                items.put(key, new ChatColorItem(
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

    public String getTitle() { return title; }
    public String getTexture() { return texture; }
    public int getSize() { return size; }
    public List<Integer> getEmptySlots() { return emptySlots; }
    public Collection<ChatColorItem> getItems() { return items.values(); }
}
