package me.sunrise.cosmeticsmanager.listeners;

import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.sunrise.cosmeticsmanager.CosmeticsManager;
import me.sunrise.cosmeticsmanager.menus.browse.CosmeticData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Objects;

public class ChatListener implements Listener {

    private final CosmeticsManager plugin;

    public ChatListener(CosmeticsManager plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        var player = event.getPlayer();
        var cache = plugin.getCache();
        if (cache.get(player.getUniqueId()) == null) return;
        String color = cache.get(player.getUniqueId()).getChatColor();
        String tag = cache.get(player.getUniqueId()).getTag();
        String badgeName = cache.get(player.getUniqueId()).getBadge();
        CosmeticData badge = plugin.getBadgesManager().getAllCosmetics().stream()
                .filter(i -> Objects.equals(getPlainName(i).toLowerCase(), badgeName))
                .findFirst()
                .orElse(null);


        if (plugin.getGradientInputManager().isAwaiting(player)) {
            return;
        }

        // Coloca cor na mensagem
        String message = PlainTextComponentSerializer.plainText().serialize(event.originalMessage());
        String formattedMessage;
        if (color != null) {
            if (color.startsWith("<gradient")) {
                formattedMessage = color + message + "</gradient>";
                if (formattedMessage.contains("<reset>")) {
                    formattedMessage = formattedMessage.replace("</gradient>", "");
                }
            } else {
                formattedMessage = color + message;
            }
        } else {
            formattedMessage = message;
        }

        // Componentes
        Component messageComponent = MiniMessage.miniMessage().deserialize(formattedMessage);
        String badgeTex;

        Component badgeComponent = Component.empty();
        if (badge != null) {
            FontImageWrapper badgeIcon = new FontImageWrapper(badge.getMaterial());
            if (badgeIcon.exists()) {
                badgeTex = badgeIcon.getString();
                badgeComponent = Component.text(badgeTex).append(Component.space());
            } else {
                // Opcional fallback: texto no lugar
                badgeComponent = Component.text("???").append(Component.space());
            }
        }

        Component tagComponent = Component.empty();
        if (tag != null && !tag.isEmpty()) { // Coloca tag se existir
            tagComponent = MiniMessage.miniMessage().deserialize(tag)
                    .append(Component.space());
        }

        Component nameComponent = Component.text(player.getName())
                .append(Component.text(": "));

        // Monta a linha completa
        Component full = badgeComponent
                .append(tagComponent)
                .append(nameComponent)
                .append(messageComponent);

        // Renderiza
        event.renderer((source, displayName, msg, viewer) -> full);
    }

    private String getPlainName(CosmeticData cosmetic) {
        return PlainTextComponentSerializer.plainText().serialize(
                MiniMessage.miniMessage().deserialize(
                        cosmetic.getName()
                )
        ).toLowerCase();
    }

}
