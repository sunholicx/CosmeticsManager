package me.sunrise.cosmeticsmanager.listeners;

import me.sunrise.cosmeticsmanager.CosmeticsManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final CosmeticsManager plugin;

    public PlayerJoinListener(CosmeticsManager plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();

        // Puxa do banco
        String savedColor = plugin.getDatabaseManager().getPlayerChatColor(player.getUniqueId().toString());
        if (savedColor != null) {
            plugin.getCache().setChatColor(player.getUniqueId(), savedColor);
        }

        String savedTag = plugin.getDatabaseManager().getPlayerTag(player.getUniqueId().toString());
        if (savedTag != null) {
            plugin.getCache().setTag(player.getUniqueId(), savedTag);
        }

        String savedBadge = plugin.getDatabaseManager().getPlayerBadge(player.getUniqueId().toString());
        if (savedBadge != null) {
            plugin.getCache().setBadge(player.getUniqueId(), savedBadge);
        }
    }
}
