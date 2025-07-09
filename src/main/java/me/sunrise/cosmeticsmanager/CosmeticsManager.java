package me.sunrise.cosmeticsmanager;

import co.aikar.commands.PaperCommandManager;
import me.sunrise.cosmeticsmanager.chatcolor.GradientInputManager;
import me.sunrise.cosmeticsmanager.commands.BadgesCommand;
import me.sunrise.cosmeticsmanager.commands.ChatColorCommand;
import me.sunrise.cosmeticsmanager.commands.CosmeticsCommand;
import me.sunrise.cosmeticsmanager.commands.TagsCommand;
import me.sunrise.cosmeticsmanager.chatcolor.ChatColorConfig;
import me.sunrise.cosmeticsmanager.listeners.MenuClickListener;
import me.sunrise.cosmeticsmanager.listeners.ChatListener;
import me.sunrise.cosmeticsmanager.listeners.GradientChatListener;
import me.sunrise.cosmeticsmanager.listeners.PlayerJoinListener;
import me.sunrise.cosmeticsmanager.menus.browse.ItemManager;
import me.sunrise.cosmeticsmanager.menus.main.MenuConfig;
import me.sunrise.cosmeticsmanager.storage.Cache;
import me.sunrise.cosmeticsmanager.storage.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

public final class CosmeticsManager extends JavaPlugin {

    private static CosmeticsManager instance;
    FileConfiguration config;

    private DatabaseManager databaseManager;
    private MenuConfig chatColorMenuConfig;
    private MenuConfig tagMenuConfig;
    private MenuConfig cosmeticsMenuConfig;
    private MenuConfig badgesMenuConfig;
    private ChatColorConfig  chatColorsConfig;
    private Cache cache;
    private GradientInputManager gradientInputManager;
    private ItemManager tagManager;
    private ItemManager badgesManager;
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

        // Cria um cache dos dados dos players online
        this.cache = new Cache();
        this.gradientInputManager = new GradientInputManager();
        reloadCache();

        registerCommands();
        registerListeners();

        getLogger().info("Cosmetics Manager inicializado!");

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    public void registerCommands() {
        PaperCommandManager manager = new PaperCommandManager(this);

        manager.enableUnstableAPI("brigadier");
        manager.enableUnstableAPI("help");


        // Aliases
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

        // Comandos
        manager.registerCommand(new CosmeticsCommand(this));
        manager.registerCommand(new BadgesCommand(this));
        manager.registerCommand(new ChatColorCommand(this));
        manager.registerCommand(new TagsCommand(this));

        // Exception Handler Global
        manager.setDefaultExceptionHandler((command, registeredCommand, sender, args, t) -> {
            getLogger().warning("Error occurred while executing command " + command.getName());
            return false; // Se retornar false, ACF mostra mensagem padrão ao player
        });

    }

    public void registerListeners() {
        getServer().getPluginManager().registerEvents(new MenuClickListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
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

            // Se não existir, cria as pastas e salva
            if (!outFile.exists()) {
                outFile.getParentFile().mkdirs();
                saveResource(resource, false);
                getLogger().info("Arquivo criado: " + resource);
            }
        }
    }

    public void loadDB() {
        File dataFolder = new File(this.getDataFolder(), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        databaseManager = new DatabaseManager();
        databaseManager.init();
    }

    public void loadConfigs() {
        // Recarrega o config.yml padrão
        reloadConfig();
        this.config = this.getConfig();

        // Recarrega os YML customizados
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
        // Cosmetics
        this.cosmeticsMenuConfig = new MenuConfig(getCosmeticsMenuYml());

        // Chat Color
        this.chatColorMenuConfig = new MenuConfig(getChatColorsMenuYml());
        this.chatColorsConfig = new ChatColorConfig(this);


        // Tags
        this.tagMenuConfig = new MenuConfig(getTagsMenuYml());

        // Badges
        this.badgesMenuConfig = new MenuConfig(getBadgesMenuYml());

        // Browse Menus
        this.tagManager = new ItemManager(getTagsYml(), getBrowseTagsYml());
        this.badgesManager = new ItemManager(getBadgesYml(), getBrowseBadgesYml());

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

    public MenuConfig getTagsMenuConfig() { return tagMenuConfig; }

    public MenuConfig getCosmeticsMenuConfig() { return cosmeticsMenuConfig; }

    public MenuConfig getBadgesMenuConfig() { return badgesMenuConfig; }

    public String getAliases(String path) {
        // Extrai o último segmento do path como o nome da key
        String[] split = path.split("\\.");
        String keyName = split[split.length - 1];

        // Carrega a lista de aliases
        List<String> aliasesNames = config.getStringList(path + ".aliases");

        // Constrói a string
        StringBuilder aliases = new StringBuilder();
        aliases.append(keyName);

        for (String alias : aliasesNames) {
            aliases.append("|").append(alias);
        }

        return aliases.toString();
    }

    public ChatColorConfig getChatColorConfig() {
        return chatColorsConfig;
    }

    public Cache getCache() { return cache; }

    public GradientInputManager getGradientInputManager() { return gradientInputManager; }

    public ItemManager getTagManager() { return tagManager; }

    public ItemManager getBadgesManager() { return badgesManager; }

    public YamlConfiguration getBadgesYml() {
        return badgesYml;
    }

    public YamlConfiguration getChatColorsYml() {
        return chatColorsYml;
    }

    public YamlConfiguration getTagsYml() {
        return tagsYml;
    }

    public YamlConfiguration getCosmeticsMenuYml() {
        return cosmeticsMenuYml;
    }

    public YamlConfiguration getBadgesMenuYml() {
        return badgesMenuYml;
    }

    public YamlConfiguration getChatColorsMenuYml() {
        return chatColorsMenuYml;
    }

    public YamlConfiguration getTagsMenuYml() {
        return tagsMenuYml;
    }

    public YamlConfiguration getBrowseTagsYml() {
        return browseTagsYml;
    }

    public YamlConfiguration getBrowseBadgesYml() {
        return browseBadgesYml;
    }

    public void reloadCache() {
        // Limpa o cache
        cache.clear();

        // Para cada player online, recarrega os dados
        for (Player player : Bukkit.getOnlinePlayers()) {
            String savedColor = this.getDatabaseManager().getPlayerChatColor(player.getUniqueId().toString());
            if (savedColor != null) {
                this.getCache().setChatColor(player.getUniqueId(), savedColor);
            }

            String savedTag = this.getDatabaseManager().getPlayerTag(player.getUniqueId().toString());
            if (savedTag != null) {
                this.getCache().setTag(player.getUniqueId(), savedTag);
            }

            String savedBadge = this.getDatabaseManager().getPlayerBadge(player.getUniqueId().toString());
            if (savedBadge != null) {
                this.getCache().setBadge(player.getUniqueId(), savedBadge);
            }

        }

        // Log no console
        getLogger().info("[CosmeticsManager] Cache recarregado para "
                + Bukkit.getOnlinePlayers().size() + " players online.");
    }
}
