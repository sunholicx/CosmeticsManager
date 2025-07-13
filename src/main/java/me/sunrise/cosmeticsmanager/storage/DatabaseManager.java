package me.sunrise.cosmeticsmanager.storage;

import me.sunrise.cosmeticsmanager.CosmeticsManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.*;

public class DatabaseManager {

    private Connection connection;
    private String tablePrefix;

    public void init() {
        FileConfiguration config = CosmeticsManager.getInstance().getConfig();

        tablePrefix = config.getString("settings.database.table-prefix", "cosmeticsmanager_");
        boolean mysqlEnabled = config.getBoolean("settings.database.mysql.enabled", false);

        try {
            if (mysqlEnabled) {
                initMySQL(config);
            } else {
                initSQLite();
            }
            createTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initMySQL(FileConfiguration config) throws SQLException {
        String hostname = config.getString("settings.database.mysql.hostname");
        int port = config.getInt("settings.database.mysql.port");
        String database = config.getString("settings.database.mysql.database");
        String username = config.getString("settings.database.mysql.username");
        String password = config.getString("settings.database.mysql.password");
        String arguments = config.getString("settings.database.mysql.arguments", "");

        String url = "jdbc:mysql://" + hostname + ":" + port + "/" + database + arguments;
        connection = DriverManager.getConnection(url, username, password);
        CosmeticsManager.getInstance().getLogger().info("Conectado ao banco MySQL.");
    }

    private void initSQLite() throws SQLException {
        File dbFile = new File(CosmeticsManager.getInstance().getDataFolder(), "data/database.db");
        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        connection = DriverManager.getConnection(url);
        CosmeticsManager.getInstance().getLogger().info("Conectado ao banco SQLite.");
    }

    private void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS " + tablePrefix + "player_cosmetics (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "tag VARCHAR(50)," +
                "chat_color VARCHAR(50)," +
                "badge VARCHAR(50)" +
                ");";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.execute();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    private boolean isMySQLEnabled() {
        return CosmeticsManager.getInstance().getConfig().getBoolean("settings.database.mysql.enabled", false);
    }

    public void savePlayerChatColor(String uuid, String chatColor) {
        String sql = isMySQLEnabled()
                ? "INSERT INTO " + tablePrefix + "player_cosmetics (uuid, chat_color) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE chat_color = VALUES(chat_color);"
                : "INSERT INTO " + tablePrefix + "player_cosmetics (uuid, chat_color) VALUES (?, ?) " +
                "ON CONFLICT(uuid) DO UPDATE SET chat_color = excluded.chat_color;";

        executeUpdate(uuid, chatColor, sql);
    }

    public String getPlayerChatColor(String uuid) {
        return querySingleString(uuid, "chat_color");
    }

    public void savePlayerTag(String uuid, String tag) {
        String sql = isMySQLEnabled()
                ? "INSERT INTO " + tablePrefix + "player_cosmetics (uuid, tag) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE tag = VALUES(tag);"
                : "INSERT INTO " + tablePrefix + "player_cosmetics (uuid, tag) VALUES (?, ?) " +
                "ON CONFLICT(uuid) DO UPDATE SET tag = excluded.tag;";

        executeUpdate(uuid, tag, sql);
    }

    public String getPlayerTag(String uuid) {
        return querySingleString(uuid, "tag");
    }

    public void savePlayerBadge(String uuid, String badge) {
        String sql = isMySQLEnabled()
                ? "INSERT INTO " + tablePrefix + "player_cosmetics (uuid, badge) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE badge = VALUES(badge);"
                : "INSERT INTO " + tablePrefix + "player_cosmetics (uuid, badge) VALUES (?, ?) " +
                "ON CONFLICT(uuid) DO UPDATE SET badge = excluded.badge;";

        executeUpdate(uuid, badge, sql);
    }

    public String getPlayerBadge(String uuid) {
        return querySingleString(uuid, "badge");
    }

    private void executeUpdate(String uuid, String value, String sql) {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            stmt.setString(2, value);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String querySingleString(String uuid, String column) {
        String sql = "SELECT " + column + " FROM " + tablePrefix + "player_cosmetics WHERE uuid = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString(column);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed())
                connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}