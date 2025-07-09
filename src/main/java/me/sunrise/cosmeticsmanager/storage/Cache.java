package me.sunrise.cosmeticsmanager.storage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Cache {

    private final Map<UUID, PlayerCosmetics> cache = new ConcurrentHashMap<>();

    // Salvar todos os dados de uma vez
    public void set(UUID uuid, PlayerCosmetics cosmetics) {
        cache.put(uuid, cosmetics);
    }

    // Recuperar todos os dados
    public PlayerCosmetics get(UUID uuid) {
        return cache.get(uuid);
    }

    // Atualizar só a cor
    public void setChatColor(UUID uuid, String color) {
        cache.computeIfAbsent(uuid, u -> new PlayerCosmetics(null, null, null, "", 1))
                .setChatColor(color);
    }

    // Atualizar só a tag
    public void setTag(UUID uuid, String tag) {
        cache.computeIfAbsent(uuid, u -> new PlayerCosmetics(null, null, null, "", 1))
                .setTag(tag);
    }

    // Atualizar só a badge
    public void setBadge(UUID uuid, String badge) {
        cache.computeIfAbsent(uuid, u -> new PlayerCosmetics(null, null, null, "", 1))
                .setBadge(badge);
    }

    // Atualizar só o tipo do menu
    public void setMenuType(UUID uuid, String menuType) {
        cache.computeIfAbsent(uuid, u -> new PlayerCosmetics(null, null, null, "", 1))
                .setMenuType(menuType);
    }

    // Atualizar só a página
    public void setPage(UUID uuid, int page) {
        cache.computeIfAbsent(uuid, u -> new PlayerCosmetics(null, null, null, "", 1))
                .setPage(page);
    }

    public void remove(UUID uuid) {
        cache.remove(uuid);
    }

    public void clear() {
        cache.clear();
    }
}
