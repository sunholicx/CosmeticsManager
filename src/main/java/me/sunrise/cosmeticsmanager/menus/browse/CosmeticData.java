package me.sunrise.cosmeticsmanager.menus.browse;

import java.util.List;

public class CosmeticData {

    private String id;
    private String name;
    private int data;
    private String material;
    private List<String> lore;
    private String permission;
    private String onClick;

    public CosmeticData(String id, String name, int data, String material, List<String> lore, String permission, String onClick) {
        this.id = id;
        this.name = name;
        this.data = data;
        this.material = material;
        this.lore = lore;
        this.permission = permission;
        this.onClick = onClick;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
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

    public String getOnClick() {
        return onClick;
    }
}
