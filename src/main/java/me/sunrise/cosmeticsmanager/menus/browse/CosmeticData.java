package me.sunrise.cosmeticsmanager.model;

import java.util.List;

public class TagData {

    private String id;
    private String tag;
    private int data;
    private String material;
    private List<String> lore;
    private String permission;

    public TagData(String id, String tag, int data, String material, List<String> lore, String permission) {
        this.id = id;
        this.tag = tag;
        this.data = data;
        this.material = material;
        this.lore = lore;
        this.permission = permission;
    }

    public String getId() {
        return id;
    }

    public String getTag() {
        return tag;
    }

    public int getData() {
        return data;
    }

    public String getMaterial() {
        return material;
    }

    public List<String> getLore() {
        return lore;
    }

    public String getPermission() {
        return permission;
    }
}
