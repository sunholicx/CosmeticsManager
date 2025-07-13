package me.sunrise.cosmeticsmanager.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.sunrise.cosmeticsmanager.CosmeticsManager;
import me.sunrise.cosmeticsmanager.utils.ChatColorConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class GradientChatListener implements Listener {

    private final CosmeticsManager plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public GradientChatListener(CosmeticsManager plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        var player = event.getPlayer();
        var manager = plugin.getGradientInputManager();
        var chatConfig = plugin.getChatColorConfig();

        if (!manager.isAwaiting(player)) {
            return;
        }

        event.setCancelled(true);
        event.viewers().clear();

        String message = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();

        // Cancelar
        String cancelArg = plugin.getConfig().getString("settings.listeners.gradient-setting.cancelArg");
        if (cancelArg != null && message.equalsIgnoreCase(cancelArg)) {
            send(player, "settings.listeners.gradient-setting.cancel");
            manager.remove(player);
            return;
        }

        String[] parts = message.split("\\s+");
        if (parts.length == 0 || parts.length > 4) {
            send(player, "settings.listeners.gradient-setting.invalid-length");
            return;
        }

        // Caso apenas 1 cor, delegar ao comando normal
        if (parts.length == 1) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.performCommand("chatcolor set " + parts[0]);
            });
            return;
        }

        // Modo gradiente
        StringBuilder gradient = new StringBuilder("<gradient:");
        String path = "settings.commands.chatcolor.subcommand.set.";

        for (int i = 0; i < parts.length; i++) {
            String color = parts[i];

            if (i > 0) gradient.append(":");

            if (color.startsWith("#")) {
                if (!isValidHex(color)) {
                    send(player, path + "invalid-hex");
                    manager.remove(player);
                    return;
                }
                if (!hasPermission(player, chatConfig.getPermission("hex"), path + "no-hex-permission")) {
                    manager.remove(player);
                    return;
                }
                gradient.append(color.toLowerCase());
            } else {
                if (!chatConfig.isValidColor(color)) {
                    send(player, path + "invalid-color");
                    manager.remove(player);
                    return;
                }
                if (!hasPermission(player, chatConfig.getPermission(color), path + "no-basics-permission")) {
                    manager.remove(player);
                    return;
                }
                gradient.append(chatConfig.getColorValue(chatConfig.getKey(color)));
            }
        }
        gradient.append(">");

        // Salva no banco e atualiza cache
        plugin.getDatabaseManager().savePlayerChatColor(player.getUniqueId().toString(), gradient.toString());
        plugin.getCache().setChatColor(player.getUniqueId(), gradient.toString());

        player.sendMessage(
                miniMessage.deserialize(
                        plugin.getConfig().getString("settings.listeners.gradient-setting.success")
                                .replace("[openGrad]", gradient)
                                .replace("[closeGrad]", "</gradient>")
                )
        );

        manager.remove(player);
    }

    /**
     * Verifica se uma string é um HEX válido.
     */
    private boolean isValidHex(String hex) {
        return hex.matches("^#([A-Fa-f0-9]{6})$");
    }

    /**
     * Envia mensagem de configuração.
     */
    private void send(org.bukkit.entity.Player player, String path) {
        String msg = plugin.getConfig().getString(path);
        if (msg != null && !msg.isEmpty()) {
            player.sendMessage(miniMessage.deserialize(msg));
        }
    }

    /**
     * Checa permissão e envia mensagem caso não tenha.
     */
    private boolean hasPermission(org.bukkit.entity.Player player, String permission, String denyMessagePath) {
        if (permission == null || permission.isEmpty()) return true;
        if (!player.hasPermission(permission)) {
            send(player, denyMessagePath);
            return false;
        }
        return true;
    }
}