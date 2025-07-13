package me.sunrise.cosmeticsmanager;

import co.aikar.commands.PaperCommandManager;
import me.sunrise.cosmeticsmanager.commands.BadgesCommand;
import me.sunrise.cosmeticsmanager.commands.ChatColorCommand;
import me.sunrise.cosmeticsmanager.commands.CosmeticsCommand;
import me.sunrise.cosmeticsmanager.commands.TagsCommand;
import me.sunrise.cosmeticsmanager.listeners.GradientChatListener;
import me.sunrise.cosmeticsmanager.listeners.MenuClickListener;
import me.sunrise.cosmeticsmanager.listeners.PlayerJoinListener;
import me.sunrise.cosmeticsmanager.menus.MenuConfig;
import me.sunrise.cosmeticsmanager.storage.Cache;
import me.sunrise.cosmeticsmanager.storage.DatabaseManager;
import me.sunrise.cosmeticsmanager.utils.ChatColorConfig;
import me.sunrise.cosmeticsmanager.utils.GradientInputManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

public final class CosmeticsManager extends JavaPlugin {

    private static CosmeticsManager instance;

    private FileConfiguration config;

    private DatabaseManager databaseManager;
    private Cache cache;
    private GradientInputManager gradientInputManager;

    private ChatColorConfig chatColorsConfig;

    private MenuConfig cosmeticsMenuConfig;
    private MenuConfig chatColorMenuConfig;
    private MenuConfig tagMenuConfig;
    private MenuConfig badgesMenuConfig;
    private MenuConfig tagManager;
    private MenuConfig badgesManager;

    private YamlConfiguration badgesYml;
    private YamlConfiguration chatColorsYml;
    private YamlConfiguration tagsYml;
    private YamlConfiguration cosmeticsMenuYml;
    private YamlConfiguration badgesMenuYml;
    private YamlConfiguration chatColorsMenuYml;
    private YamlConfiguration tagsMenuYml;
    private YamlConfiguration browseTagsYml;
    private YamlConfiguration browseBadgesYml;

    @Override
    public void onEnable() {
        instance = this;

        // Carrega configurações e dados
        saveConfigs();
        loadConfigs();
        loadDB();

        // Inicializa cache e gradient
        this.cache = new Cache();
        this.gradientInputManager = new GradientInputManager();
        reloadCache();

        registerCommands();
        registerListeners();

        new CosmeticsPlaceholders(this).register();

        getLogger().info("Cosmetics Manager inicializado!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    public void registerCommands() {
        PaperCommandManager manager = new PaperCommandManager(this);

        manager.enableUnstableAPI("brigadier");
        manager.enableUnstableAPI("help");

        // Aliases e descrições
        manager.getCommandReplacements().addReplacements(
                "cosmetics", getAliases("settings.commands.cosmetics"),
                "badges", getAliases("settings.commands.badges"),
                "chatcolor", getAliases("settings.commands.chatcolor"),
                "tags", getAliases("settings.commands.tags"),
                "gradient", getAliases("settings.commands.chatcolor.subcommand.gradient"),
                "ccdescription", config.getString("settings.commands.chatcolor.description", "Sem descrição"),
                "setcolor", getAliases("settings.commands.chatcolor.subcommand.set"),
                "setcolordescription", config.getString("settings.commands.chatcolor.subcommand.set.description", "Sem descrição"),
                "graddescription", config.getString("settings.commands.chatcolor.subcommand.gradient.description", "Sem descrição"),
                "colors", getAliases("settings.commands.chatcolor.subcommand.colors"),
                "colorsdescription", config.getString("settings.commands.chatcolor.subcommand.colors.description", "Sem descrição"),
                "tagsdescription", config.getString("settings.commands.tags.description", "Sem descrição"),
                "cmtcsdescription", config.getString("settings.commands.cosmetics.description", "Sem descrição"),
                "bdgdescription", config.getString("settings.commands.badges.description", "Sem descrição"),
                "ownedtags", getAliases("settings.commands.tags.subcommand.owned"),
                "ownedtagsdescription", config.getString("settings.commands.tags.subcommand.owned.description", "Sem descrição"),
                "alltags", getAliases("settings.commands.tags.subcommand.all"),
                "alltagsdescription", config.getString("settings.commands.tags.subcommand.all.description", "Sem descrição"),
                "blktags", getAliases("settings.commands.tags.subcommand.blocked"),
                "blktagsdescription", config.getString("settings.commands.tags.subcommand.blocked.description", "Sem descrição"),
                "ownedbadges", getAliases("settings.commands.badges.subcommand.owned"),
                "ownedbdgsdescription", config.getString("settings.commands.badges.subcommand.owned.description", "Sem descrição"),
                "allbadges", getAliases("settings.commands.badges.subcommand.all"),
                "allbdgsdescription", config.getString("settings.commands.badges.subcommand.all.description", "Sem descrição"),
                "blkbadges", getAliases("settings.commands.badges.subcommand.blocked"),
                "blkbdgsdescription", config.getString("settings.commands.badges.subcommand.blocked.description", "Sem descrição"),
                "settag", getAliases("settings.commands.tags.subcommand.set"),
                "settagdescription", config.getString("settings.commands.tags.subcommand.set.description", "Sem descrição"),
                "setbadge", getAliases("settings.commands.badges.subcommand.set"),
                "setbadgedescription", config.getString("settings.commands.badges.subcommand.set.description", "Sem descrição"),
                "removetag", getAliases("settings.commands.tags.subcommand.remove"),
                "removetagdescription", config.getString("settings.commands.tags.subcommand.remove.description", "Sem descrição"),
                "removebadge", getAliases("settings.commands.badges.subcommand.remove"),
                "removebadgedescription", config.getString("settings.commands.badges.subcommand.remove.description", "Sem descrição"),
                "removecolor", getAliases("settings.commands.chatcolor.subcommand.remove"),
                "removecolordescription", config.getString("settings.commands.chatcolor.subcommand.remove.description", "Sem descrição"),
                "admin", config.getString("settings.admin-permission")
        );

        // Registra comandos
        manager.registerCommand(new CosmeticsCommand(this));
        manager.registerCommand(new BadgesCommand(this));
        manager.registerCommand(new ChatColorCommand(this));
        manager.registerCommand(new TagsCommand(this));

        // Exception Handler global
        manager.setDefaultExceptionHandler((command, registeredCommand, sender, args, t) -> {
            getLogger().warning("Error occurred while executing command " + command.getName());
            return false;
        });
    }

    public void registerListeners() {
        getServer().getPluginManager().registerEvents(new MenuClickListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new GradientChatListener(this), this);
    }

    public void saveConfigs() {
        saveDefaultConfig();

        String[] resources = {
                "cosmetics/badges.yml",
                "cosmetics/chat-colors.yml",
                "cosmetics/tags.yml",
                "guis/cosmetics-menu.yml",
                "guis/badges-menu.yml",
                "guis/chat-colors-menu.yml",
                "guis/tags-menu.yml",
                "guis/browse-tags.yml",
                "guis/browse-badges.yml"
        };

        for (String resource : resources) {
            File outFile = new File(getDataFolder(), resource);
            if (!outFile.exists()) {
                outFile.getParentFile().mkdirs();
                saveResource(resource, false);
                getLogger().info("Arquivo criado: " + resource);
            }
        }
    }

    public void loadDB() {
        File dataFolder = new File(getDataFolder(), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        databaseManager = new DatabaseManager();
        databaseManager.init();
    }

    public void loadConfigs() {
        reloadConfig();
        this.config = getConfig();

        badgesYml = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "cosmetics/badges.yml"));
        chatColorsYml = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "cosmetics/chat-colors.yml"));
        tagsYml = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "cosmetics/tags.yml"));
        cosmeticsMenuYml = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "guis/cosmetics-menu.yml"));
        badgesMenuYml = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "guis/badges-menu.yml"));
        chatColorsMenuYml = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "guis/chat-colors-menu.yml"));
        tagsMenuYml = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "guis/tags-menu.yml"));
        browseTagsYml = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "guis/browse-tags.yml"));
        browseBadgesYml = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "guis/browse-badges.yml"));

        cosmeticsConfig();
    }

    public void cosmeticsConfig() {
        this.cosmeticsMenuConfig = new MenuConfig(getCosmeticsMenuYml());
        this.chatColorMenuConfig = new MenuConfig(getChatColorsMenuYml());
        this.chatColorsConfig = new ChatColorConfig(this);
        this.tagMenuConfig = new MenuConfig(getTagsMenuYml());
        this.badgesMenuConfig = new MenuConfig(getBadgesMenuYml());
        this.tagManager = new MenuConfig(getTagsYml(), getBrowseTagsYml());
        this.badgesManager = new MenuConfig(getBadgesYml(), getBrowseBadgesYml());
    }

    public static CosmeticsManager getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public MenuConfig getChatColorMenuConfig() {
        return chatColorMenuConfig;
    }

    public MenuConfig getTagsMenuConfig() {
        return tagMenuConfig;
    }

    public MenuConfig getCosmeticsMenuConfig() {
        return cosmeticsMenuConfig;
    }

    public MenuConfig getBadgesMenuConfig() {
        return badgesMenuConfig;
    }

    public MenuConfig getTagManager() {
        return tagManager;
    }

    public MenuConfig getBadgesManager() {
        return badgesManager;
    }

    public ChatColorConfig getChatColorConfig() {
        return chatColorsConfig;
    }

    public Cache getCache() {
        return cache;
    }

    public GradientInputManager getGradientInputManager() {
        return gradientInputManager;
    }

    public String getAliases(String path) {
        String[] split = path.split("\\.");
        String keyName = split[split.length - 1];
        List<String> aliasesNames = config.getStringList(path + ".aliases");

        StringBuilder aliases = new StringBuilder(keyName);
        for (String alias : aliasesNames) {
            aliases.append("|").append(alias);
        }
        return aliases.toString();
    }

    public YamlConfiguration getBadgesYml() { return badgesYml; }
    public YamlConfiguration getChatColorsYml() { return chatColorsYml; }
    public YamlConfiguration getTagsYml() { return tagsYml; }
    public YamlConfiguration getCosmeticsMenuYml() { return cosmeticsMenuYml; }
    public YamlConfiguration getBadgesMenuYml() { return badgesMenuYml; }
    public YamlConfiguration getChatColorsMenuYml() { return chatColorsMenuYml; }
    public YamlConfiguration getTagsMenuYml() { return tagsMenuYml; }
    public YamlConfiguration getBrowseTagsYml() { return browseTagsYml; }
    public YamlConfiguration getBrowseBadgesYml() { return browseBadgesYml; }

    public void reloadCache() {
        cache.clear();

        for (Player player : Bukkit.getOnlinePlayers()) {
            String savedColor = databaseManager.getPlayerChatColor(player.getUniqueId().toString());
            if (savedColor != null) {
                cache.setChatColor(player.getUniqueId(), savedColor);
            }

            String savedTag = databaseManager.getPlayerTag(player.getUniqueId().toString());
            if (savedTag != null) {
                cache.setTag(player.getUniqueId(), savedTag);
            }

            String savedBadge = databaseManager.getPlayerBadge(player.getUniqueId().toString());
            if (savedBadge != null) {
                cache.setBadge(player.getUniqueId(), savedBadge);
            }
        }

        getLogger().info("[CosmeticsManager] Cache recarregado para " + Bukkit.getOnlinePlayers().size() + " players online.");
    }
}