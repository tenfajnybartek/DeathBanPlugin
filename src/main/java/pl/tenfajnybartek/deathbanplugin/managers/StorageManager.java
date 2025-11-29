package pl.tenfajnybartek.deathbanplugin.managers;

import pl.tenfajnybartek.deathbanplugin.base.DeathBanPlugin;
import pl.tenfajnybartek.deathbanplugin.database.Database;
import pl.tenfajnybartek.deathbanplugin.database.Storage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;

public class StorageManager {
    private final List<Storage> playerList = new ArrayList<>();

    public void loadAllAsync(DeathBanPlugin plugin) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            playerList.clear();
            Database db = plugin.getDatabase();
            try (Connection conn = db.getConnection();
                 ResultSet rs = db.getAllBans(conn)) {
                while (rs != null && rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    String nick = rs.getString("nick");
                    long time = rs.getLong("time");
                    Storage storage = new Storage(plugin, uuid, nick, time);
                    playerList.add(storage);
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Błąd podczas ładowania banów: " + e.getMessage());
            }
        });
    }

    public List<Storage> getPlayerList() { return playerList; }

    public Storage getPlayerByUuid(UUID uuid) {
        for (Storage s : playerList) {
            if (s.getUuid().equals(uuid)) return s;
        }
        return null;
    }

    public Storage getPlayerByNick(String nick) {
        for (Storage s : playerList) {
            if (s.getNick().equalsIgnoreCase(nick)) return s;
        }
        return null;
    }
}