package me.sunrise.cosmeticsmanager;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.sunrise.cosmeticsmanager.menus.Menu;
import me.sunrise.cosmeticsmanager.menus.MenuConfig;
import me.sunrise.cosmeticsmanager.menus.MenuItem;
import me.sunrise.cosmeticsmanager.storage.PlayerCosmetics;
import me.sunrise.cosmeticsmanager.utils.ChatColorConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CosmeticsPlaceholders extends PlaceholderExpansion {

    private final CosmeticsManager plugin;

    public CosmeticsPlaceholders(final CosmeticsManager plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "cm";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Sunrise";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true; // mantém registrada após reload
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(final Player player, @NotNull final String identifier) {
        if (player == null) return "";

        final String id = identifier.toLowerCase();

        return switch (id) {
            case "badge" -> getCosmetic(player, "badge");
            case "tag" -> {
                final String tag = getCosmetic(player, "tag");
                yield (tag != null && !tag.isEmpty()) ? tag + "<reset>" : "";
            }
            case "chatcolor" -> getCosmetic(player, "chatcolor");
            default -> null;
        };
    }

    public String getCosmetic(Player player, String type) {
        PlayerCosmetics cosmetics = plugin.getCache().get(player.getUniqueId());
        if (cosmetics == null) return "";

        String cosmetic;
        switch (type) {
            case "badge":
                cosmetic = cosmetics.getBadge() == null ? "" : cosmetics.getBadge();
                if (cosmetic.isEmpty()) return "";
                if (isCosmeticPermitted(player, plugin.getBadgesYml(), cosmetic, "emoji")) {
                    return cosmetic;
                }
                plugin.getDatabaseManager().savePlayerBadge(player.getUniqueId().toString(), "");
                plugin.getCache().setBadge(player.getUniqueId(), "");
                return "";
            case "tag":
                cosmetic = cosmetics.getTag() == null ? "" : cosmetics.getTag();
                if (cosmetic.isEmpty()) return "";
                if (isCosmeticPermitted(player, plugin.getTagsYml(), cosmetic, "name")) {
                    return cosmetic;
                }
                plugin.getDatabaseManager().savePlayerTag(player.getUniqueId().toString(), "");
                plugin.getCache().setTag(player.getUniqueId(), "");
                return "";
            case "chatcolor":
                cosmetic = cosmetics.getChatColor() == null ? "" : cosmetics.getChatColor();
                if (cosmetic.isEmpty()) return "";
                if (isColorPermitted(player, cosmetic)){
                    return cosmetic;
                }
                plugin.getDatabaseManager().savePlayerChatColor(player.getUniqueId().toString(), "");
                plugin.getCache().setChatColor(player.getUniqueId(), "");
                return "";
        }

        return "";

    }

    private boolean isCosmeticPermitted(Player player, YamlConfiguration config, String searched, String type) {
        ConfigurationSection cosmeticsSection = config.getConfigurationSection("cosmetics");
        if (cosmeticsSection == null) {return false;}

        boolean found = false;
        String permission = null;

        for (String key : cosmeticsSection.getKeys(false)) {
            ConfigurationSection cosmetic = cosmeticsSection.getConfigurationSection(key);
            if (cosmetic == null) continue;

            String emoji = cosmetic.getString(type);
            if (emoji != null && emoji.trim().equalsIgnoreCase(searched.trim())) {
                found = true;
                permission = cosmetic.getString("permission");
                break;
            }
        }

        if (!found) {return false;}

        if (permission != null && !permission.isEmpty()) {
            return player.hasPermission(permission);
        } else {
            return true;
        }
    }

    private boolean isColorPermitted (Player player, String cosmetic) {
        ChatColorConfig config = plugin.getChatColorConfig();
                cosmetic = cosmetic.substring(1, cosmetic.length() - 1);

        if (cosmetic.contains("#")) {
            boolean isBasicHex = config.isValidColor(cosmetic);
            if (!isBasicHex) {
                String hexPerm = config.getPermission("hex");
                if (hexPerm != null && !player.hasPermission(hexPerm)) {
                    return false;
                }
            }
        }

        if (cosmetic.contains("gradient")) {
            String perm = config.getPermission("gradient");
            return perm == null || player.hasPermission(perm);
        }

        String permission = config.getPermission(cosmetic);
        return permission == null || player.hasPermission(permission);
    }
}