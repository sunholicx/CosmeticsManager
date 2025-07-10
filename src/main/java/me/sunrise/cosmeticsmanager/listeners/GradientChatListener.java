package me.sunrise.cosmeticsmanager.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.sunrise.cosmeticsmanager.CosmeticsManager;
import me.sunrise.cosmeticsmanager.chatcolor.ChatColorConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;


public class GradientChatListener implements Listener {

    private final CosmeticsManager plugin;


    public GradientChatListener(CosmeticsManager plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        var player = event.getPlayer();
        var manager = plugin.getGradientInputManager();

        if (!manager.isAwaiting(player)) {
            return;
        }

        event.setCancelled(true); // não mostrar a mensagem no chat público
        event.viewers().clear();

        // Pega e formata mensagem do player
        String message = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();
        message = message.toLowerCase();

        // Checa se foi cancelado
        if (message.equalsIgnoreCase(plugin.getConfig().getString("settings.listeners.gradient-setting.cancelArg").toLowerCase())) {
            player.sendMessage(
                    MiniMessage.miniMessage().deserialize(
                            plugin.getConfig().getString("settings.listeners.gradient-setting.cancel")
                    )
            );
            manager.remove(player);
            return;
        }

        String[] parts = message.split("\\s+");

        // Checa se a quantidade de argumentos está dentro dos limites
        if (parts.length > 4) {
            player.sendMessage(
                    MiniMessage.miniMessage().deserialize(
                            plugin.getConfig().getString("settings.listeners.gradient-setting.invalid-length")
                    )
            );
            return;
        }

        // Verifica se apenas uma cor foi escolhida
        if (parts.length == 1) {
            String finalMessage = message;
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.performCommand("chatcolor set " + finalMessage);
            });
            return;
        }

        // Verifica os argumentos e monta o valor final
        ChatColorConfig config = plugin.getChatColorConfig();
        String path = "settings.commands.chatcolor.subcommand.set.";
        StringBuilder gradient = new StringBuilder("<gradient:");
        for (int i = 0; i < parts.length; i++) {


            // Verifica cores HEX e se tem permissão
            if (parts[i].startsWith("#")) {
                String hex = parts[i].toLowerCase();

                if (!hex.matches("^#([A-Fa-f0-9]{6})$")) {
                    player.sendMessage(
                            MiniMessage.miniMessage().deserialize(
                                    plugin.getConfig().getString(path + "invalid-hex")
                            )
                    );
                    manager.remove(player);
                    return;
                }

                // Verifica permissão
                String hexPermission = config.getPermission("hex");
                if (hexPermission != null && !player.hasPermission(hexPermission)) {
                    player.sendMessage(
                            MiniMessage.miniMessage().deserialize(
                                    plugin.getConfig().getString(path + "no-hex-permission")
                            )
                    );
                    manager.remove(player);
                    return;
                }

                // Adiciona cor ao gradiente
                if (i > 0) gradient.append(":");
                gradient.append(parts[i]);

            } else {
                // Verifica se a cor existe e se tem permissão
                if (!config.isValidColor(parts[i])) {
                    player.sendMessage(
                            MiniMessage.miniMessage().deserialize(
                                    plugin.getConfig().getString(path + "invalid-color")
                            )
                    );
                    manager.remove(player);
                    return;
                }

                String permission = config.getPermission(parts[i]);
                if (permission != null && !player.hasPermission(permission)) {
                    player.sendMessage(
                            MiniMessage.miniMessage().deserialize(
                                    plugin.getConfig().getString(path + "no-basics-permission")
                            )
                    );
                    manager.remove(player);
                    return;
                }

                // Adiciona cor ao gradiente
                if (i > 0) gradient.append(":");
                gradient.append(config.getColorValue(config.getKey(parts[i])));
            }


        }

        gradient.append(">");

        // Salva no banco
        plugin.getDatabaseManager().savePlayerChatColor(player.getUniqueId().toString(), gradient.toString());

        // Atualiza cache
        plugin.getCache().setChatColor(player.getUniqueId(), gradient.toString());

        player.sendMessage(
                MiniMessage.miniMessage().deserialize(
                        plugin.getConfig().getString("settings.listeners.gradient-setting.success")
                                .replace("[openGrad]", gradient)
                                .replace("[closeGrad]", "</gradient>")
                )
        );

        // Sai do modo input
        manager.remove(player);
    }
}
