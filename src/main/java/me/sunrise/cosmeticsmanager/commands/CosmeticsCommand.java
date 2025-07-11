package me.sunrise.cosmeticsmanager.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.sunrise.cosmeticsmanager.CosmeticsManager;
import me.sunrise.cosmeticsmanager.menus.Menu;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("%cosmetics")
@Description("%cmtcsdescription")
public class CosmeticsCommand extends BaseCommand {

    private final CosmeticsManager plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public CosmeticsCommand(CosmeticsManager plugin) {
        this.plugin = plugin;
    }

    @Default
    public void onCommand(Player player) {
        new Menu(plugin, player, plugin.getCosmeticsMenuConfig()).open();
    }

    @Subcommand("reloadplg")
    @Description("Recarrega o plugin inteiro")
    @CommandPermission("%admin")
    public void onReloadPlugin(CommandSender sender) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            plugin.getServer().getPluginManager().enablePlugin(plugin);
            sendInfo(sender, "<green>[CosmeticsManager]</green> O plugin foi recarregado com sucesso!");
        });
    }

    @Subcommand("reload")
    @Description("Recarrega os arquivos de configuração")
    @CommandPermission("%admin")
    public void onReloadConfigs(CommandSender sender) {
        plugin.loadConfigs();
        sendInfo(sender, "<green>[CosmeticsManager]</green> Os arquivos de configuração foram recarregados!");
    }

    @Subcommand("reloadcache")
    @Description("Recarrega o cache")
    @CommandPermission("%admin")
    public void onReloadCache(CommandSender sender) {
        plugin.reloadCache();
        sendInfo(sender, "<green>[CosmeticsManager]</green> O cache foi recarregado!");
    }

    /**
     * Envia mensagem formatada com MiniMessage para CommandSender.
     */
    private void sendInfo(CommandSender sender, String message) {
        if (sender instanceof Player player) {
            player.sendMessage(miniMessage.deserialize(message));
        } else {
            sender.sendMessage(miniMessage.stripTags(message));
        }
    }
}