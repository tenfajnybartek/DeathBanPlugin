package pl.tenfajnybartek.deathbanplugin.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;

public class Database {
    private final JavaPlugin plugin;
    private final HikariDataSource dataSource;

    public Database(JavaPlugin plugin, String host, int port, String user, String password, String database) {
        this.plugin = plugin;
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true");
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(10000);
        config.setIdleTimeout(60000);
        config.setMaxLifetime(1800000);
        this.dataSource = new HikariDataSource(config);

        plugin.getLogger().info("Połączenie z bazą MySQL zostało utworzone.");
        createTableIfNotExists();
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        try {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close(); // Szybko i synchronnie!
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Błąd podczas rozłączania bazy danych: " + e.getMessage());
        }
    }

    private void createTableIfNotExists() {
        String sql =
                "CREATE TABLE IF NOT EXISTS deathban_players (" +
                        "  uuid VARCHAR(36) NOT NULL," +
                        "  nick VARCHAR(32) NOT NULL," +
                        "  time BIGINT NOT NULL," +
                        "  PRIMARY KEY (uuid)" +
                        ")";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Nie można utworzyć/przetworzyć tabeli deathban_players: " + e.getMessage());
        }
    }

    public void saveBan(UUID uuid, String nick, long time) {
        String sqlUpdate = "UPDATE deathban_players SET nick = ?, time = ? WHERE uuid = ?";
        String sqlInsert = "INSERT INTO deathban_players (uuid, nick, time) VALUES (?, ?, ?)";

        try (Connection conn = getConnection()) {
            int updated;
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                ps.setString(1, nick);
                ps.setLong(2, time);
                ps.setString(3, uuid.toString());
                updated = ps.executeUpdate();
            }
            if (updated == 0) {
                try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                    ps.setString(1, uuid.toString());
                    ps.setString(2, nick);
                    ps.setLong(3, time);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Błąd podczas zapisu bana: " + e.getMessage());
        }
    }

    public void deleteBan(UUID uuid) {
        String sql = "DELETE FROM deathban_players WHERE uuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Błąd podczas usuwania bana gracza " + uuid + ": " + e.getMessage());
        }
    }

    public Long getBanTime(UUID uuid) {
        String sql = "SELECT time FROM deathban_players WHERE uuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("time");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Błąd podczas pobierania czasu bana gracza " + uuid + ": " + e.getMessage());
        }
        return null;
    }

    public ResultSet getAllBans(Connection conn) throws SQLException {
        String sql = "SELECT uuid, nick, time FROM deathban_players";
        PreparedStatement ps = conn.prepareStatement(sql);
        return ps.executeQuery();
    }
}