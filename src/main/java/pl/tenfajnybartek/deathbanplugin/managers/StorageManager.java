package pl.tenfajnybartek.deathbanplugin.managers;

import pl.tenfajnybartek.deathbanplugin.base.DeathBanPlugin;
import pl.tenfajnybartek.deathbanplugin.database.Database;
import pl.tenfajnybartek.deathbanplugin.database.Storage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StorageManager {
    private final List<Storage> playerList = new ArrayList<>();

    public void loadAll(DeathBanPlugin plugin) {
        playerList.clear();
        Database db = plugin.getDatabase();
        try (Connection conn = db.getConnection();
             ResultSet rs = db.getAllBans(conn)) {
            while (rs != null && rs.next()) {
                Storage storage = new Storage(plugin, rs.getString("nick"), rs.getLong("time"));
                playerList.add(storage);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Błąd podczas ładowania banów: " + e.getMessage());
        }
    }

    public List<Storage> getPlayerList() { return playerList; }

    public Storage getPlayerByNick(String nick) {
        for (Storage s : playerList) {
            if (s.getNick().equalsIgnoreCase(nick)) return s;
        }
        return null;
    }
}