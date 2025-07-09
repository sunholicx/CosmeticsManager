package me.sunrise.cosmeticsmanager.storage;

public class PlayerCosmetics {
    private String chatColor;
    private String tag;
    private String badge;
    private String menuType;
    private int page;

    public PlayerCosmetics(String chatColor, String tag, String badge, String menuType, int page) {
        this.chatColor = chatColor;
        this.tag = tag;
        this.badge = badge;
        this.menuType = menuType;
        this.page = page;
    }

    // Getters e Setters
    public String getChatColor() {
        return chatColor;
    }

    public void setChatColor(String chatColor) {
        this.chatColor = chatColor;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public String getMenuType() {
        return menuType;
    }

    public void setMenuType(String menuType) {
        this.menuType = menuType;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}
