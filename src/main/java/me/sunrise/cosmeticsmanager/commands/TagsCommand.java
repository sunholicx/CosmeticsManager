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

@CommandAlias("%tags")
@Description("%tagdescription")
public class TagsCommand extends BaseCommand {

    private final CosmeticsManager plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public TagsCommand(CosmeticsManager plugin) {
        this.plugin = plugin;
    }

    @Default
    public void onCommand(Player player) {
        new Menu(plugin, player, plugin.getTagsMenuConfig()).open();
    }

    @Subcommand("%ownedtags")
    @CommandCompletion("@nothing")
    @Description("%ownedtagsdescription")
    public void onOwned(Player player) {
        openBrowseMenu(player, "my");
    }

    @Subcommand("%alltags")
    @CommandCompletion("@nothing")
    @Description("%alltagsdescription")
    public void onAll(Player player) {
        openBrowseMenu(player, "all");
    }

    @Subcommand("%blktags")
    @CommandCompletion("@nothing")
    @Description("%blktagsdescription")
    public void onBlocked(Player player) {
        openBrowseMenu(player, "blocked");
    }

    @Subcommand("%settag")
    @Syntax("<[tag]>")
    @CommandCompletion("@nothing")
    @Description("%settagsdescription")
    public void onSet(Player player, String arg) {
        String path = "settings.commands.tags.subcommand.set.";
        MenuConfig manager = plugin.getTagManager();

        MenuItem tag = findCosmeticByName(manager, arg);
        if (tag == null) {
            sendMessageFromConfig(player, path + "invalid-tag");
            return;
        }

        if (!hasPermissionOrNotify(player, tag.getPermission(), path + "no-permission")) return;

        // Salva
        saveTag(player, tag.getName());

        String successMsg = plugin.getConfig().getString(path + "success").replace("[Tag]", tag.getName());
        player.sendMessage(miniMessage.deserialize(successMsg));
    }

    @Subcommand("%removetag")
    @CommandCompletion("@nothing")
    @Description("%removetagsdescription")
    public void onRemove(Player player) {
        saveTag(player, "");
    }

    // -----------------------
    // Métodos auxiliares
    // -----------------------

    private void openBrowseMenu(Player player, String type) {
        BrowseMenu menu = new BrowseMenu(plugin, player, type, 1, plugin.getTagManager(), plugin.getBrowseTagsYml());
        menu.open();
        Cache cache = plugin.getCache();
        cache.setMenuType(player.getUniqueId(), type);
        cache.setPage(player.getUniqueId(), 1);
    }

    private void saveTag(Player player, String value) {
        plugin.getDatabaseManager().savePlayerTag(player.getUniqueId().toString(), value);
        plugin.getCache().setTag(player.getUniqueId(), value);
    }

    private MenuItem findCosmeticByName(MenuConfig manager, String arg) {
        String inputName = ChatColor.stripColor(arg).trim().toLowerCase();

        return manager.getAllCosmetics().stream()
                .filter(i -> {
                    String name = PlainTextComponentSerializer.plainText().serialize(
                            miniMessage.deserialize(i.getName())
                    ).trim().toLowerCase();
                    return name.equalsIgnoreCase(inputName);
                })
                .findFirst()
                .orElse(null);
    }

    private boolean hasPermissionOrNotify(Player player, String permission, String messagePath) {
        if (permission == null || permission.isEmpty()) return true;

        if (!player.hasPermission(permission)) {
            sendMessageFromConfig(player, messagePath);
            return false;
        }
        return true;
    }

    private void sendMessageFromConfig(Player player, String path) {
        String raw = plugin.getConfig().getString(path);
        if (raw != null && !raw.isEmpty()) {
            player.sendMessage(miniMessage.deserialize(raw));
        }
    }
}