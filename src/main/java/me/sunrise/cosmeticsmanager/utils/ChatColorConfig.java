package me.sunrise.cosmeticsmanager.utils;

import me.sunrise.cosmeticsmanager.CosmeticsManager;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;

public class ChatColorConfig {

    private final YamlConfiguration config;
    private final YamlConfiguration menuConfig;
    private final CosmeticsManager plugin;

    public ChatColorConfig(CosmeticsManager plugin) {
        this.plugin = plugin;
        this.config = plugin.getChatColorsYml();
        this.menuConfig = plugin.getChatColorsMenuYml();
    }

    /**
     * Verifica se a cor existe no config (por nome, alias ou valor)
     */
    public boolean isValidColor(String name) {
        if (name == null) return false;
        String input = name.toLowerCase();

        if (config.getConfigurationSection("colors") == null) return false;

        for (String key : config.getConfigurationSection("colors").getKeys(false)) {
            String basePath = "colors." + key + ".";

            // Checa displayName
            String displayName = config.getString(basePath + "displayName", "").toLowerCase();
            if (input.equals(displayName)) {
                return true;
            }

            // Checa aliases
            List<String> aliases = config.getStringList(basePath + "aliases");
            for (String alias : aliases) {
                if (input.equals(alias.toLowerCase())) {
                    return true;
                }
            }

            // Checa valor da cor
            String colorValue = config.getString(basePath + "value", "").toLowerCase();
            if (input.equals(colorValue)) {
                return true;
            }

            // Checa no menuConfig (se disponível)
            String menuColorValue = menuConfig.getString("settings.items." + key + ".color", "").toLowerCase();
            if (input.equals(menuColorValue)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Retorna a chave da cor com base no nome ou alias, ou null se não achar
     */
    public String getKey(String name) {
        if (name == null) return null;
        String input = name.toLowerCase();

        if (config.getConfigurationSection("colors") == null) return null;

        for (String key : config.getConfigurationSection("colors").getKeys(false)) {
            String basePath = "colors." + key + ".";
            String displayName = config.getString(basePath + "displayName", "").toLowerCase();

            if (input.equals(displayName)) {
                return key;
            }

            List<String> aliases = config.getStringList(basePath + "aliases");
            for (String alias : aliases) {
                if (input.equals(alias.toLowerCase())) {
                    return key;
                }
            }
        }

        return null;
    }

    /**
     * Retorna o valor da cor (ex: #FFFFFF)
     */
    public String getColorValue(String key) {
        if (key == null) return null;
        return config.getString("colors." + key.toLowerCase() + ".value");
    }

    /**
     * Retorna o nome para display
     */
    public String getDisplayName(String key) {
        if (key == null) return null;
        return config.getString("colors." + key.toLowerCase() + ".displayName");
    }

    /**
     * Retorna a permissão associada a uma cor, nome ou tipo especial (gradient, hex)
     */
    public String getPermission(String name) {
        if (name == null) return config.getString("permission");

        String input = name.toLowerCase();

        String gradientDisplayName = config.getString("colors.gradient.displayName", "").toLowerCase();
        if (input.equals(gradientDisplayName)) {
            return config.getString("gradient-permission");
        }

        List<String> gradientAliases = config.getStringList("colors.gradient.aliases");
        for (String alias : gradientAliases) {
            if (input.equals(alias.toLowerCase())) {
                return config.getString("gradient-permission");
            }
        }

        if ("hex".equalsIgnoreCase(name)) {
            return config.getString("hex-permission");
        }

        return config.getString("permission");
    }
}