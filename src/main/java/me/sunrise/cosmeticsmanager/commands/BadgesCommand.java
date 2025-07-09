package me.sunrise.cosmeticsmanager.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.sunrise.cosmeticsmanager.CosmeticsManager;
import me.sunrise.cosmeticsmanager.menus.browse.BrowseMenu;
import me.sunrise.cosmeticsmanager.menus.browse.CosmeticData;
import me.sunrise.cosmeticsmanager.menus.browse.ItemManager;
import me.sunrise.cosmeticsmanager.menus.main.Menu;
import me.sunrise.cosmeticsmanager.storage.Cache;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@CommandAlias("%badges")
@Description("%bdgdescription")
public class BadgesCommand extends BaseCommand {

    private final CosmeticsManager plugin;

    public BadgesCommand(CosmeticsManager plugin) {
        this.plugin = plugin;
    }

    @Default
    public void onCommand(Player player) {
        Menu menu = new Menu(plugin, player, plugin.getBadgesMenuConfig());
        menu.open();
    }

    @Subcommand("%ownedbadges")
    @CommandCompletion("@nothing")
    @Description("%ownedbdgsdescription")
    public void onOwned(Player player) {
        BrowseMenu menu = new BrowseMenu(plugin, player, "my", 1, plugin.getBadgesManager(), plugin.getBrowseBadgesYml());
        menu.open();
        Cache cache = plugin.getCache();
        cache.setMenuType(player.getUniqueId(), "my");
        cache.setPage(player.getUniqueId(),1);
    }

    @Subcommand("%allbadges")
    @CommandCompletion("@nothing")
    @Description("%allbdgsdescription")
    public void onAll(Player player) {
        BrowseMenu menu = new BrowseMenu(plugin, player, "all", 1, plugin.getBadgesManager(), plugin.getBrowseBadgesYml());
        menu.open();
        Cache cache = plugin.getCache();
        cache.setMenuType(player.getUniqueId(), "all");
        cache.setPage(player.getUniqueId(),1);
    }

    @Subcommand("%blkbadges")
    @CommandCompletion("@nothing")
    @Description("%blkbdgsdescription")
    public void onblocked(Player player) {
        Cache cache = plugin.getCache();
        BrowseMenu menu = new BrowseMenu(plugin, player, "blocked", 1, plugin.getBadgesManager(), plugin.getBrowseBadgesYml());
        menu.open();
        cache.setMenuType(player.getUniqueId(), "blocked");
        cache.setPage(player.getUniqueId(),1);
    }

    @Subcommand("%setbadge")
    @Syntax("<[badge]>")
    @CommandCompletion("@nothing")
    @Description("%setbadgedescription")
    public void onSet(Player player, String arg) {
        ItemManager manager = plugin.getBadgesManager();

        String path = "settings.commands.badges.subcommand.set.";

        // Verifica se a badge informada existe
        String displayName0 = arg.toLowerCase();
        String displayName = ChatColor.stripColor(displayName0).trim();
        CosmeticData badge = manager.getAllCosmetics().stream()
                .filter(i -> {
                    String name = PlainTextComponentSerializer.plainText().serialize(
                            MiniMessage.miniMessage().deserialize(i.getName())
                    ).trim();
                    return name.equalsIgnoreCase(displayName);
                })
                .findFirst()
                .orElse(null);

        if (badge == null) {
            player.sendMessage(
                    MiniMessage.miniMessage().deserialize(
                            plugin.getConfig().getString(path + "invalid-badge")
                    )
            );
            return;
        }

        // Verifica permissão
        if (badge.getPermission() != null && !badge.getPermission().isEmpty()) {

            if (!(player.hasPermission(badge.getPermission()))) {
                player.sendMessage(
                        MiniMessage.miniMessage().deserialize(
                                plugin.getConfig().getString(path + "no-permission")
                        )
                );
                return;
            }
        }

        // Salva mudanças
        plugin.getDatabaseManager().savePlayerBadge(player.getUniqueId().toString(), badge.getEmoji());
        plugin.getCache().setBadge(player.getUniqueId(), badge.getEmoji());
        player.sendMessage(
                MiniMessage.miniMessage().deserialize(
                        plugin.getConfig().getString(path + "success").replace("[Badge]", badge.getEmoji())
                )
        );
    }

    @Subcommand("%removebadge")
    @CommandCompletion("@nothing")
    @Description("%removebadgedescription")
    public void onRemove(Player player) {
        plugin.getDatabaseManager().savePlayerBadge(player.getUniqueId().toString(), "");
        plugin.getCache().setBadge(player.getUniqueId(), "");
    }

}
