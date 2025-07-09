package me.sunrise.cosmeticsmanager.storage;

import me.sunrise.cosmeticsmanager.CosmeticsManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseManager {

    private Connection connection;
    private String tablePrefix;

    public void init() {
        FileConfiguration config = CosmeticsManager.getInstance().getConfig();

        // Lê o prefixo
        tablePrefix = config.getString("settings.database.table-prefix", "cosmeticsmanager_");

        boolean mysqlEnabled = config.getBoolean("settings.database.mysql.enabled", false);

        try {
            if (mysqlEnabled) {
                // Conexão MySQL
                String hostname = config.getString("settings.database.mysql.hostname");
                int port = config.getInt("settings.database.mysql.port");
                String database = config.getString("settings.database.mysql.database");
                String username = config.getString("settings.database.mysql.username");
                String password = config.getString("settings.database.mysql.password");
                String arguments = config.getString("settings.database.mysql.arguments", "");

                String url = "jdbc:mysql://" + hostname + ":" + port + "/" + database + arguments;

                connection = DriverManager.getConnection(url, username, password);
                CosmeticsManager.getInstance().getLogger().info("Conectado ao banco MySQL.");
            } else {
                // Conexão SQLite
                File dbFile = new File(CosmeticsManager.getInstance().getDataFolder(), "data/database.db");
                String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
                connection = DriverManager.getConnection(url);
                CosmeticsManager.getInstance().getLogger().info("Conectado ao banco SQLite.");
            }

            // Criar a tabela se não existir
            createTable();

        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    public void savePlayerChatColor(String uuid, String chatColor) {
        boolean mysqlEnabled = CosmeticsManager.getInstance().getConfig().getBoolean("settings.database.mysql.enabled", false);
        String sql;
        if (mysqlEnabled) {
            sql = "INSERT INTO " + tablePrefix + "player_cosmetics (uuid, chat_color) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE chat_color = VALUES(chat_color);";
        } else {
            sql = "INSERT INTO " + tablePrefix + "player_cosmetics (uuid, chat_color) VALUES (?, ?) " +
                    "ON CONFLICT(uuid) DO UPDATE SET chat_color = excluded.chat_color;";
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            stmt.setString(2, chatColor);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getPlayerChatColor(String uuid) {
        String sql = "SELECT chat_color FROM " + tablePrefix + "player_cosmetics WHERE uuid = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("chat_color");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void savePlayerTag(String uuid, String tag) {
        boolean mysqlEnabled = CosmeticsManager.getInstance().getConfig().getBoolean("settings.database.mysql.enabled", false);
        String sql;
        if (mysqlEnabled) {
            sql = "INSERT INTO " + tablePrefix + "player_cosmetics (uuid, tag) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE tag = VALUES(tag);";
        } else {
            sql = "INSERT INTO " + tablePrefix + "player_cosmetics (uuid, tag) VALUES (?, ?) " +
                    "ON CONFLICT(uuid) DO UPDATE SET tag = excluded.tag;";
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            stmt.setString(2, tag);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getPlayerTag(String uuid) {
        String sql = "SELECT tag FROM " + tablePrefix + "player_cosmetics WHERE uuid = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("tag");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void savePlayerBadge(String uuid, String badge) {
        boolean mysqlEnabled = CosmeticsManager.getInstance().getConfig().getBoolean("settings.database.mysql.enabled", false);
        String sql;
        if (mysqlEnabled) {
            sql = "INSERT INTO " + tablePrefix + "player_cosmetics (uuid, badge) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE badge = VALUES(badge);";
        } else {
            sql = "INSERT INTO " + tablePrefix + "player_cosmetics (uuid, badge) VALUES (?, ?) " +
                    "ON CONFLICT(uuid) DO UPDATE SET badge = excluded.badge;";
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            stmt.setString(2, badge);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getPlayerBadge(String uuid) {
        String sql = "SELECT badge FROM " + tablePrefix + "player_cosmetics WHERE uuid = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("badge");
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
