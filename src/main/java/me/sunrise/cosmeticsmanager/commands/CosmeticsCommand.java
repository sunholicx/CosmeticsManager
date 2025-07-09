package me.sunrise.cosmeticsmanager.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.sunrise.cosmeticsmanager.CosmeticsManager;
import me.sunrise.cosmeticsmanager.menus.main.Menu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("%cosmetics")
@Description("%cmtcsdescription")
public class CosmeticsCommand extends BaseCommand {

    private final CosmeticsManager plugin;

    public CosmeticsCommand(CosmeticsManager plugin) {
        this.plugin = plugin;
    }

    @Default
    public void onCommand(Player player) {
        Menu menu = new Menu(plugin, player, plugin.getCosmeticsMenuConfig());
        menu.open();

    }

    @Subcommand("reloadplg")
    @Description("Recarrega o plugin inteiro")
    @CommandPermission("%admin")
    public void onReload(CommandSender sender) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            plugin.getServer().getPluginManager().enablePlugin(plugin);
            sender.sendMessage("[CosmeticsManger] O plugin foi recarregado com sucesso!");
        });
    }

    @Subcommand("reload")
    @Description("Recarrega os arquivos de configuração")
    @CommandPermission("%admin")
    public void onReloadConfig(CommandSender sender) {
        plugin.loadConfigs();
        sender.sendMessage("[CosmeticsManger] Os arquivos de configuração foram recarregados!");
    }

    @Subcommand("reloadcache")
    @Description("Recarrega o cache")
    @CommandPermission("%admin")
    public void onReloadCache(CommandSender sender) {
        plugin.reloadCache();
    }
}
