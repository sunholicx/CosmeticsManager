package me.sunrise.cosmeticsmanager.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.sunrise.cosmeticsmanager.CosmeticsManager;
import me.sunrise.cosmeticsmanager.menus.BrowseMenu;
import me.sunrise.cosmeticsmanager.menus.Menu;
import me.sunrise.cosmeticsmanager.menus.MenuConfig;
import me.sunrise.cosmeticsmanager.menus.MenuItem;
import me.sunrise.cosmeticsmanager.storage.Cache;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@CommandAlias("%badges")
@Description("%bdgdescription")
public class BadgesCommand extends BaseCommand {

    private final CosmeticsManager plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final PlainTextComponentSerializer plainSerializer = PlainTextComponentSerializer.plainText();

    public BadgesCommand(CosmeticsManager plugin) {
        this.plugin = plugin;
    }

    @Default
    public void onCommand(Player player) {
        new Menu(plugin, player, plugin.getBadgesMenuConfig()).open();
    }

    @Subcommand("%ownedbadges")
    @CommandCompletion("@nothing")
    @Description("%ownedbdgsdescription")
    public void onOwned(Player player) {
        openBrowseMenu(player, "my");
    }

    @Subcommand("%allbadges")
    @CommandCompletion("@nothing")
    @Description("%allbdgsdescription")
    public void onAll(Player player) {
        openBrowseMenu(player, "all");
    }

    @Subcommand("%blkbadges")
    @CommandCompletion("@nothing")
    @Description("%blkbdgsdescription")
    public void onBlocked(Player player) {
        openBrowseMenu(player, "blocked");
    }

    private void openBrowseMenu(Player player, String type) {
        BrowseMenu menu = new BrowseMenu(
                plugin,
                player,
                type,
                1,
                plugin.getBadgesManager(),
                plugin.getBrowseBadgesYml()
        );
        menu.open();

        Cache cache = plugin.getCache();
        cache.setMenuType(player.getUniqueId(), type);
        cache.setPage(player.getUniqueId(), 1);
    }

    @Subcommand("%setbadge")
    @Syntax("<badge>")
    @CommandCompletion("@nothing")
    @Description("%setbadgedescription")
    public void onSet(Player player, String inputBadge) {
        MenuConfig manager = plugin.getBadgesManager();
        String configPath = "settings.commands.badges.subcommand.set.";

        // Normaliza o input
        String sanitizedInput = ChatColor.stripColor(inputBadge).trim().toLowerCase();

        // Tenta localizar o badge
        MenuItem badge = manager.getAllCosmetics().stream()
                .filter(i -> {
                    String deserializedName = plainSerializer.serialize(
                            miniMessage.deserialize(i.getName())
                    ).trim();
                    return deserializedName.equalsIgnoreCase(sanitizedInput);
                })
                .findFirst()
                .orElse(null);

        if (badge == null) {
            sendConfigMessage(player, configPath + "invalid-badge");
            return;
        }

        // Checa permissão se existir
        if (badge.getPermission() != null && !badge.getPermission().isEmpty()) {
            if (!player.hasPermission(badge.getPermission())) {
                sendConfigMessage(player, configPath + "no-permission");
                return;
            }
        }

        // Salva badge
        plugin.getDatabaseManager().savePlayerBadge(player.getUniqueId().toString(), badge.getEmoji());
        plugin.getCache().setBadge(player.getUniqueId(), badge.getEmoji());

        String successMessage = plugin.getConfig()
                .getString(configPath + "success")
                .replace("[Badge]", badge.getEmoji());

        player.sendMessage(miniMessage.deserialize(successMessage));
    }

    @Subcommand("%removebadge")
    @CommandCompletion("@nothing")
    @Description("%removebadgedescription")
    public void onRemove(Player player) {
        plugin.getDatabaseManager().savePlayerBadge(player.getUniqueId().toString(), "");
        plugin.getCache().setBadge(player.getUniqueId(), "");
    }

    /**
     * Envia uma mensagem configurada para o jogador.
     */
    private void sendConfigMessage(Player player, String path) {
        String msg = plugin.getConfig().getString(path);
        if (msg != null && !msg.isEmpty()) {
            player.sendMessage(miniMessage.deserialize(msg));
        }
    }
}