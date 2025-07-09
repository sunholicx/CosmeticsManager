package me.sunrise.cosmeticsmanager.menus.chat_colors;

import java.util.List;

public class ChatColorItem {
    private final String id;
    private final Object color; // String ou List<String>
    private final String name;
    private final int data;
    private final String material;
    private final List<String> lore;
    private final int slot;
    private final String permission;
    private final String noPermission;
    private final String onClick;

    public ChatColorItem(String id, Object color, String name, int data, String material, String permission, String noPermission, List<String> lore, int slot, String onClick) {
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
    }


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

}