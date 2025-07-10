package me.sunrise.cosmeticsmanager;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.sunrise.cosmeticsmanager.storage.PlayerCosmetics;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CosmeticsPlaceholders extends PlaceholderExpansion {

    private final CosmeticsManager plugin;

    public CosmeticsPlaceholders(CosmeticsManager plugin) {
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
        return true; // manter registrada após reload
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        PlayerCosmetics cosmetics = plugin.getCache().get(player.getUniqueId());
        if (cosmetics == null) {
            return "";
        }

        String badge = cosmetics.getBadge();
        String tag = cosmetics.getTag();
        String chatColor = cosmetics.getChatColor();

        switch (identifier.toLowerCase()) {
            case "badge":
                return badge != null ? badge : "";
            case "tag":
                if (tag != null && !tag.isEmpty()) {
                    tag = tag + "<reset>" ;
                    return tag;
                }
                return "";
            case "chatcolor":
                if (chatColor != null && !chatColor.isEmpty()) {
                    return chatColor;
                }
                return "";
            default:
                return null;
        }
    }
}
