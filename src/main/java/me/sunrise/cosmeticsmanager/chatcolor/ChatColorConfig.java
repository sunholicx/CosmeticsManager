package me.sunrise.cosmeticsmanager.cosmetics;

import me.sunrise.cosmeticsmanager.CosmeticsManager;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ChatColorConfig {

    private final YamlConfiguration config;

    public ChatColorConfig(CosmeticsManager plugin) {
        File file = new File(plugin.getDataFolder(), "cosmetics/chat-colors.yml");
        config = YamlConfiguration.loadConfiguration(file);

    }

    public boolean isValidColor(String name) {
        return config.getConfigurationSection("colors").getKeys(false).contains(name.toLowerCase());
    }

    public String getColorValue(String name) {
        return config.getString("colors." + name.toLowerCase() + ".value");
    }

    public String getPermission(String name) {
        return config.getString("colors." + name.toLowerCase() + ".permission");
    }
}
