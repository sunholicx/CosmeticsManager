package me.sunrise.cosmeticsmanager.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.sunrise.cosmeticsmanager.CosmeticsManager;
import me.sunrise.cosmeticsmanager.chatcolor.ChatColorConfig;
import me.sunrise.cosmeticsmanager.menus.main.Menu;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

@CommandAlias("%chatcolor")
@Description("%ccdescription")
public class ChatColorCommand extends BaseCommand {

    private final CosmeticsManager plugin;

    public ChatColorCommand(CosmeticsManager plugin) {
        this.plugin = plugin;
    }

    @Default
    public void onCommand(Player player) {
        Menu menu = new Menu(plugin, player, plugin.getChatColorMenuConfig());
        menu.open();

    }

    @Subcommand("%setcolor")
    @Syntax("<cor>")
    @CommandCompletion("@nothing")
    @Description("%setcolordescription")
    public void onSet(Player player, String arg) {
        ChatColorConfig config = plugin.getChatColorConfig();

        String path = "settings.commands.chatcolor.subcommand.set.";

        if (arg.startsWith("#")) {
            // Cor HEX
            String hex = arg.toLowerCase();

            if (!hex.matches("^#([A-Fa-f0-9]{6})$")) {
                player.sendMessage(
                        MiniMessage.miniMessage().deserialize(
                            plugin.getConfig().getString(path + "invalid-hex")
                        )
                );
                return;
            }

            // Verifica permissão
            boolean isHexBasicColor = config.isValidColor(hex);

            if (!isHexBasicColor) {
                String hexPermission = config.getPermission("hex");
                if (hexPermission != null && !player.hasPermission(hexPermission)) {
                    player.sendMessage(
                            MiniMessage.miniMessage().deserialize(
                                    plugin.getConfig().getString(path + "no-hex-permission")
                            )
                    );
                    return;
                }
            }


            String finalValue = "<" + hex + ">";
            plugin.getDatabaseManager().savePlayerChatColor(player.getUniqueId().toString(), finalValue);
            plugin.getCache().setChatColor(player.getUniqueId(), finalValue);
            player.sendMessage(
                    MiniMessage.miniMessage().deserialize(
                            plugin.getConfig().getString(path + "success").replace("[Cor]", finalValue+hex)
                    )
            );
            return;
        }

        // Cor é válida?
        if (!config.isValidColor(arg)) {
            player.sendMessage(
                    MiniMessage.miniMessage().deserialize(
                            plugin.getConfig().getString(path + "invalid-color")
                    )
            );
            return;
        }
        // player tem permissão?
        String permission = config.getPermission(arg);
        if (permission != null && !player.hasPermission(permission)) {
            player.sendMessage(
                    MiniMessage.miniMessage().deserialize(
                            plugin.getConfig().getString(path + "no-basics-permission")
                    )
            );
            return;
        }

        String value = config.getDisplayName(config.getKey(arg));
        if (value == null || value.isEmpty()) {
            return;
        }

        String finalValue = "<" + config.getColorValue(config.getKey(arg)) + ">";
        plugin.getDatabaseManager().savePlayerChatColor(player.getUniqueId().toString(), finalValue);
        plugin.getCache().setChatColor(player.getUniqueId(), finalValue);
        player.sendMessage(
                MiniMessage.miniMessage().deserialize(
                        plugin.getConfig().getString(path + "success").replace("[Cor]", finalValue+value)
                )
        );
    }


    @Subcommand("%gradient")
    @CommandCompletion("@nothing")
    @Description("%graddescription")
    public void onGradient(Player player) {
        String permission = plugin.getChatColorConfig().getPermission("gradient");
        if (permission != null && !player.hasPermission(permission)) {
            player.sendMessage(
                    MiniMessage.miniMessage().deserialize(
                            plugin.getConfig().getString("settings.commands.chatcolor.subcommand.gradient.no-gradient-permission")
                    )
            );
            return;
        }
        player.sendMessage(
                MiniMessage.miniMessage().deserialize(
                        plugin.getConfig().getString("settings.commands.chatcolor.subcommand.gradient.msg1")
                )
        );
        player.sendMessage(
                MiniMessage.miniMessage().deserialize(
                        plugin.getConfig().getString("settings.commands.chatcolor.subcommand.gradient.msg2")
                )
        );
        // Marca o jogador como aguardando input
        plugin.getGradientInputManager().add(player);
    }


    @Subcommand("%colors")
    @Description("%colorsdescription")
    public void onColors(Player player) {
        YamlConfiguration config = plugin.getChatColorsYml();

        if (config == null) {
            player.sendMessage("§cConfiguração de cores não carregada.");
            return;
        }

        player.sendMessage(
                MiniMessage.miniMessage().deserialize(
                        plugin.getConfig().getString("settings.commands.chatcolor.subcommand.colors.msg")
                )
        );


        // Percorre todas as cores
        for (String key : config.getConfigurationSection("colors").getKeys(false)) {
            if (!key.equalsIgnoreCase("gradient")) {
                String colorValue = config.getString("colors." + key + ".value");
                String displayName = plugin.getChatColorConfig().getDisplayName(key);

                // Formata <cor>Nome
                String formatted = "     <" + colorValue + ">- " + displayName;
                player.sendMessage(MiniMessage.miniMessage().deserialize(formatted));
            }
        }

    }


    @Subcommand("%removecolor")
    @CommandCompletion("@nothing")
    @Description("%removecolordescription")
    public void onRemove(Player player) {
        plugin.getDatabaseManager().savePlayerChatColor(player.getUniqueId().toString(), "");
        plugin.getCache().setChatColor(player.getUniqueId(), "");
    }


}
