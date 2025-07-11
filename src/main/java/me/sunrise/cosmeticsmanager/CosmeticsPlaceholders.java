package me.sunrise.cosmeticsmanager;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.sunrise.cosmeticsmanager.storage.PlayerCosmetics;
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

        final PlayerCosmetics cosmetics = plugin.getCache().get(player.getUniqueId());
        if (cosmetics == null) return "";

        final String id = identifier.toLowerCase();

        return switch (id) {
            case "badge" -> cosmetics.getBadge() != null ? cosmetics.getBadge() : "";
            case "tag" -> {
                final String tag = cosmetics.getTag();
                yield (tag != null && !tag.isEmpty()) ? tag + "<reset>" : "";
            }
            case "chatcolor" -> {
                final String color = cosmetics.getChatColor();
                yield (color != null && !color.isEmpty()) ? color : "";
            }
            default -> null;
        };
    }
}