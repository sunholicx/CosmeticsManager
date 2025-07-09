package me.sunrise.cosmeticsmanager.storage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Cache {

    private final Map<UUID, String> cache = new ConcurrentHashMap<>();

    public void set(UUID uuid, String color) {
        cache.put(uuid, color);
    }

    public String get(UUID uuid) {
        return cache.get(uuid);
    }

    public void remove(UUID uuid) {
        cache.remove(uuid);
    }

    public void clear() {
        cache.clear();
    }
}
