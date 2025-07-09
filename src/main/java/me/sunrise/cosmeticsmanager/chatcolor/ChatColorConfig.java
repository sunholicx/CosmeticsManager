package me.sunrise.cosmeticsmanager.chatcolor;

import me.sunrise.cosmeticsmanager.CosmeticsManager;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public class ChatColorConfig {

    private final YamlConfiguration config;
    private final CosmeticsManager plugin;

    public ChatColorConfig(CosmeticsManager plugin) {
        this.config = plugin.getChatColorsYml();
        this.plugin = plugin;

    }

    public boolean isValidColor(String name) {
        String input = name.toLowerCase();

        for (String key : config.getConfigurationSection("colors").getKeys(false)) {
            String displayName = config.getString("colors." + key + ".displayName", "").toLowerCase();
            if (input.equals(displayName)) {
                return true;
            }

            List<String> aliases = config.getStringList("colors." + key + ".aliases");
            for (String alias : aliases) {
                if (input.equals(alias.toLowerCase())) {
                    return true;
                }
            }

            String colorValue = config.getString("colors." + key + ".value", "").toLowerCase();
            if (input.equals(colorValue)) {
                return true;
            }

            colorValue = plugin.getChatColorsMenuYml().getString("settings.items" + key + ".color", "").toLowerCase();
            if (input.equals(colorValue)) {
                return true;
            }
        }

        return false;
    }

    public String getKey(String name) {
        String input = name.toLowerCase();

        for (String key : config.getConfigurationSection("colors").getKeys(false)) {
            String displayName = config.getString("colors." + key + ".displayName", "").toLowerCase();

            if (input.equals(displayName)) {
                return key;
            }

            List<String> aliases = config.getStringList("colors." + key + ".aliases");
            for (String alias : aliases) {
                if (input.equalsIgnoreCase(alias)) {
                    return key;
                }
            }
        }

        return null;
    }

    public String getColorValue(String key) {
        return config.getString("colors." + key.toLowerCase() + ".value");
    }

    public String getDisplayName(String key) {
        return config.getString("colors." + key.toLowerCase() + ".displayName");
    }

    public String getPermission(String name) {
        String input = name.toLowerCase();

        String displayName = config.getString("colors.gradient.displayName", "").toLowerCase();
        if (input.equals(displayName)) {
            return config.getString("gradient-permission");
        }

        List<String> aliases = config.getStringList("colors.gradient.aliases");
        for (String alias : aliases) {
            if (input.equalsIgnoreCase(alias)) {
                return config.getString("gradient-permission");
            }
        }

        if (name.equalsIgnoreCase("hex")) {
            return config.getString("hex-permission");
        }

        return config.getString("permission");
    }
}
