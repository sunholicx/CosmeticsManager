package me.sunrise.cosmeticsmanager.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.sunrise.cosmeticsmanager.CosmeticsManager;
import me.sunrise.cosmeticsmanager.utils.ChatColorConfig;
import me.sunrise.cosmeticsmanager.menus.Menu;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

@CommandAlias("%chatcolor")
@Description("%ccdescription")
public class ChatColorCommand extends BaseCommand {

    private final CosmeticsManager plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ChatColorCommand(CosmeticsManager plugin) {
        this.plugin = plugin;
    }

    @Default
    public void onCommand(Player player) {
        new Menu(plugin, player, plugin.getChatColorMenuConfig()).open();
    }

    @Subcommand("%setcolor")
    @Syntax("<cor>")
    @CommandCompletion("@nothing")
    @Description("%setcolordescription")
    public void onSet(Player player, String arg) {
        ChatColorConfig config = plugin.getChatColorConfig();
        String path = "settings.commands.chatcolor.subcommand.set.";

        if (arg.startsWith("#")) {
            // HEX color
            String hex = arg.toLowerCase();

            if (!hex.matches("^#([a-f0-9]{6})$")) {
                sendConfigMessage(player, path + "invalid-hex");
                return;
            }

            boolean isBasicHex = config.isValidColor(hex);
            if (!isBasicHex) {
                String hexPerm = config.getPermission("hex");
                if (hexPerm != null && !player.hasPermission(hexPerm)) {
                    sendConfigMessage(player, path + "no-hex-permission");
                    return;
                }
            }

            String finalValue = "<" + hex + ">";
            savePlayerChatColor(player, finalValue);
            sendSuccessMessage(player, path + "success", finalValue + hex);
            return;
        }

        // Named color
        if (!config.isValidColor(arg)) {
            sendConfigMessage(player, path + "invalid-color");
            return;
        }

        String permission = config.getPermission(arg);
        if (permission != null && !player.hasPermission(permission)) {
            sendConfigMessage(player, path + "no-basics-permission");
            return;
        }

        String key = config.getKey(arg);
        String value = config.getDisplayName(key);
        if (value == null || value.isEmpty()) {
            sendConfigMessage(player, path + "invalid-color");
            return;
        }

        String finalValue = "<" + config.getColorValue(key) + ">";
        savePlayerChatColor(player, finalValue);
        sendSuccessMessage(player, path + "success", finalValue + value);
    }

    @Subcommand("%gradient")
    @CommandCompletion("@nothing")
    @Description("%graddescription")
    public void onGradient(Player player) {
        String perm = plugin.getChatColorConfig().getPermission("gradient");
        String basePath = "settings.commands.chatcolor.subcommand.gradient.";

        if (perm != null && !player.hasPermission(perm)) {
            sendConfigMessage(player, basePath + "no-gradient-permission");
            return;
        }

        sendConfigMessage(player, basePath + "msg1");
        sendConfigMessage(player, basePath + "msg2");

        // Marca como aguardando input
        plugin.getGradientInputManager().add(player);
    }

    @Subcommand("%colors")
    @Description("%colorsdescription")
    public void onColors(Player player) {
        YamlConfiguration yml = plugin.getChatColorsYml();

        if (yml == null) {
            player.sendMessage("§cConfiguração de cores não carregada.");
            return;
        }

        sendConfigMessage(player, "settings.commands.chatcolor.subcommand.colors.msg");

        yml.getConfigurationSection("colors").getKeys(false).stream()
                .filter(key -> !key.equalsIgnoreCase("gradient"))
                .forEach(key -> {
                    String colorValue = yml.getString("colors." + key + ".value");
                    String displayName = plugin.getChatColorConfig().getDisplayName(key);
                    String formatted = "     <" + colorValue + ">- " + displayName;
                    player.sendMessage(miniMessage.deserialize(formatted));
                });
    }

    @Subcommand("%removecolor")
    @CommandCompletion("@nothing")
    @Description("%removecolordescription")
    public void onRemove(Player player) {
        savePlayerChatColor(player, "");
    }

    /**
     * Salva a cor escolhida no banco e no cache.
     */
    private void savePlayerChatColor(Player player, String color) {
        plugin.getDatabaseManager().savePlayerChatColor(player.getUniqueId().toString(), color);
        plugin.getCache().setChatColor(player.getUniqueId(), color);
    }

    /**
     * Envia mensagem configurada do plugin.yml.
     */
    private void sendConfigMessage(Player player, String path) {
        String raw = plugin.getConfig().getString(path);
        if (raw != null && !raw.isEmpty()) {
            player.sendMessage(miniMessage.deserialize(raw));
        }
    }

    /**
     * Envia mensagem de sucesso com placeholder da cor.
     */
    private void sendSuccessMessage(Player player, String path, String replacement) {
        String raw = plugin.getConfig().getString(path);
        if (raw != null && !raw.isEmpty()) {
            player.sendMessage(miniMessage.deserialize(raw.replace("[Cor]", replacement)));
        }
    }

}