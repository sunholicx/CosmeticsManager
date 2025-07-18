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
        String uuid = player.getUniqueId().toString();

        // Puxa do banco e coloca no cache
        String savedColor = plugin.getDatabaseManager().getPlayerChatColor(uuid);
        if (savedColor != null) {
            plugin.getCache().setChatColor(player.getUniqueId(), savedColor);
        }

        String savedTag = plugin.getDatabaseManager().getPlayerTag(uuid);
        if (savedTag != null) {
            plugin.getCache().setTag(player.getUniqueId(), savedTag);
        }

        String savedBadge = plugin.getDatabaseManager().getPlayerBadge(uuid);
        if (savedBadge != null) {
            plugin.getCache().setBadge(player.getUniqueId(), savedBadge);
        }
    }
}