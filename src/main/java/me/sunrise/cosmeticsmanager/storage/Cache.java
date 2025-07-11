package me.sunrise.cosmeticsmanager.storage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Cache {

    private final Map<UUID, PlayerCosmetics> cache = new ConcurrentHashMap<>();

    /**
     * Salva ou substitui os dados de cosméticos do jogador
     */
    public void set(UUID uuid, PlayerCosmetics cosmetics) {
        cache.put(uuid, cosmetics);
    }

    /**
     * Recupera os dados de cosméticos do jogador pelo UUID
     */
    public PlayerCosmetics get(UUID uuid) {
        return cache.get(uuid);
    }

    /**
     * Atualiza apenas a cor do chat do jogador
     */
    public void setChatColor(UUID uuid, String color) {
        cache.computeIfAbsent(uuid, u -> new PlayerCosmetics(null, null, null, "", 1))
                .setChatColor(color);
    }

    /**
     * Atualiza apenas a tag do jogador
     */
    public void setTag(UUID uuid, String tag) {
        cache.computeIfAbsent(uuid, u -> new PlayerCosmetics(null, null, null, "", 1))
                .setTag(tag);
    }

    /**
     * Atualiza apenas a badge do jogador
     */
    public void setBadge(UUID uuid, String badge) {
        cache.computeIfAbsent(uuid, u -> new PlayerCosmetics(null, null, null, "", 1))
                .setBadge(badge);
    }

    /**
     * Atualiza o tipo do menu para o jogador
     */
    public void setMenuType(UUID uuid, String menuType) {
        cache.computeIfAbsent(uuid, u -> new PlayerCosmetics(null, null, null, "", 1))
                .setMenuType(menuType);
    }

    /**
     * Atualiza a página atual do menu para o jogador
     */
    public void setPage(UUID uuid, int page) {
        cache.computeIfAbsent(uuid, u -> new PlayerCosmetics(null, null, null, "", 1))
                .setPage(page);
    }

    /**
     * Remove os dados de cosméticos do jogador
     */
    public void remove(UUID uuid) {
        cache.remove(uuid);
    }

    /**
     * Limpa o cache inteiro
     */
    public void clear() {
        cache.clear();
    }
}