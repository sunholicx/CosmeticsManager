package me.sunrise.cosmeticsmanager.menus;

import java.util.List;

public class MenuItem {

    private final String id;
    private final Object color; // Pode ser String ou List<String>
    private final String name;
    private final int data;
    private final String material;
    private final List<String> lore;
    private final int slot;
    private final String permission;
    private final String noPermission;
    private final String onClick;
    private final String emoji;

    /**
     * Construtor para itens do menu e cores
     */
    public MenuItem(String id, Object color, String name, int data, String material, String permission,
                    String noPermission, List<String> lore, int slot, String onClick) {
        this.id = id;
        this.color = color;
        this.name = name;
        this.data = data;
        this.material = material;
        this.permission = permission;
        this.noPermission = noPermission;
        this.lore = lore;
        this.slot = slot;
        this.onClick = onClick;
        this.emoji = null;
    }

    /**
     * Construtor para badge e tag
     */
    public MenuItem(String id, String name, int data, String material, List<String> lore,
                    String permission, String onClick, String emoji) {
        this.id = id;
        this.name = name;
        this.data = data;
        this.material = material;
        this.lore = lore;
        this.permission = permission;
        this.onClick = onClick;
        this.emoji = emoji;
        this.color = null;
        this.slot = -1;
        this.noPermission = null;
    }

    /**
     * Construtor para botões dos menus de visualização
     */
    public MenuItem(String id, Object color, String name, int data, String material,
                    List<String> lore, int slot, String onClick) {
        this.id = id;
        this.color = color;
        this.name = name;
        this.data = data;
        this.material = material;
        this.lore = lore;
        this.slot = slot;
        this.onClick = onClick;
        this.permission = null;
        this.noPermission = null;
        this.emoji = null;
    }

    // Getters
    public String getId() { return id; }
    public Object getColor() { return color; }
    public String getName() { return name; }
    public int getData() { return data; }
    public String getMaterial() { return material; }
    public List<String> getLore() { return lore; }
    public int getSlot() { return slot; }
    public String getPermission() { return permission; }
    public String getNoPermission() { return noPermission; }
    public String getOnClick() { return onClick; }
    public String getEmoji() { return emoji; }
}