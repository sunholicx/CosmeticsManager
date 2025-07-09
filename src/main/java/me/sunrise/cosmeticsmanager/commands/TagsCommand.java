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

import java.util.Objects;

@CommandAlias("%tags")
@Description("%tagdescription")
public class TagsCommand extends BaseCommand {

    private final CosmeticsManager plugin;

    public TagsCommand(CosmeticsManager plugin) {
        this.plugin = plugin;
    }

    @Default
    public void onCommand(Player player) {
        Menu menu = new Menu(plugin, player, plugin.getTagsMenuConfig());
        menu.open();
    }


    @Subcommand("%ownedtags")
    @CommandCompletion("@nothing")
    @Description("%ownedtagsdescription")
    public void onOwned(Player player) {
        BrowseMenu menu = new BrowseMenu(plugin, player, "my", 1, plugin.getTagManager(), plugin.getBrowseTagsYml());
        menu.open();
        Cache cache = plugin.getCache();
        cache.setMenuType(player.getUniqueId(), "my");
        cache.setPage(player.getUniqueId(),1);
    }

    @Subcommand("%alltags")
    @CommandCompletion("@nothing")
    @Description("%alltagsdescription")
    public void onAll(Player player) {
        BrowseMenu menu = new BrowseMenu(plugin, player, "all", 1, plugin.getTagManager(), plugin.getBrowseTagsYml());
        menu.open();
        Cache cache = plugin.getCache();
        cache.setMenuType(player.getUniqueId(), "all");
        cache.setPage(player.getUniqueId(),1);
    }

    @Subcommand("%blktags")
    @CommandCompletion("@nothing")
    @Description("%blktagsdescription")
    public void onblocked(Player player) {
        Cache cache = plugin.getCache();
        BrowseMenu menu = new BrowseMenu(plugin, player, "blocked", 1, plugin.getTagManager(), plugin.getBrowseTagsYml());
        menu.open();
        cache.setMenuType(player.getUniqueId(), "blocked");
        cache.setPage(player.getUniqueId(),1);
    }

    @Subcommand("%settag")
    @Syntax("<[tag]>")
    @CommandCompletion("@nothing")
    @Description("%settagsdescription")
    public void onSet(Player player, String arg) {
        ItemManager manager = plugin.getTagManager();

        String path = "settings.commands.tags.subcommand.set.";

        // Verifica se a tag informada existe
        String displayName0 = arg.toLowerCase();
        String displayName = ChatColor.stripColor(displayName0).trim();
        CosmeticData tag = manager.getAllCosmetics().stream()
                .filter(i -> {
                    String name = PlainTextComponentSerializer.plainText().serialize(
                            MiniMessage.miniMessage().deserialize(i.getName())
                    ).trim();
                    return name.equalsIgnoreCase(displayName);
                })
                .findFirst()
                .orElse(null);

        if (tag == null) {
            player.sendMessage(
                    MiniMessage.miniMessage().deserialize(
                            plugin.getConfig().getString(path + "invalid-tag")
                    )
            );
            return;
        }

        // Verifica permissão
        if (tag.getPermission() != null && !tag.getPermission().isEmpty()) {

            if (!(player.hasPermission(tag.getPermission()))) {
                player.sendMessage(
                        MiniMessage.miniMessage().deserialize(
                                plugin.getConfig().getString(path + "no-permission")
                        )
                );
                return;
            }
        }

        // Salva mudanças
        plugin.getDatabaseManager().savePlayerTag(player.getUniqueId().toString(), tag.getName());
        plugin.getCache().setTag(player.getUniqueId(), tag.getName());
        player.sendMessage(
                MiniMessage.miniMessage().deserialize(
                        plugin.getConfig().getString(path + "success").replace("[Tag]", tag.getName())
                )
        );
    }

    @Subcommand("%removetag")
    @CommandCompletion("@nothing")
    @Description("%removetagsdescription")
    public void onRemove(Player player) {
        plugin.getDatabaseManager().savePlayerTag(player.getUniqueId().toString(), "");
        plugin.getCache().setTag(player.getUniqueId(), "");
    }

}
