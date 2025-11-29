package pl.tenfajnybartek.deathbanplugin.database;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.tenfajnybartek.deathbanplugin.base.DeathBanPlugin;

public class Storage {
    private final String nick;
    private final long time;
    private final DeathBanPlugin plugin;

    public Storage(DeathBanPlugin plugin, String nick, long time) {
        this.plugin = plugin;
        this.nick = nick;
        this.time = time;
        plugin.getDatabase().saveBan(nick, time);
    }

    public String getNick() { return nick; }
    public long getTime() { return time; }
    public Player getPlayer() { return Bukkit.getPlayer(nick); }
    public void delete() {
        plugin.getDatabase().deleteBan(nick);
        plugin.getStorageManager().getPlayerList().remove(this);
    }
}