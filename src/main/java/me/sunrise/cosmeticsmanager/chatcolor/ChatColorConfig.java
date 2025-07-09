package me.sunrise.cosmeticsmanager.chatcolor;

import me.sunrise.cosmeticsmanager.CosmeticsManager;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public class ChatColorConfig {

    private final YamlConfiguration config;

    public ChatColorConfig(CosmeticsManager plugin) {
        config = plugin.getChatColorsYml();

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
