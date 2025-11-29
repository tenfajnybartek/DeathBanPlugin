package pl.tenfajnybartek.deathbanplugin.database;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.tenfajnybartek.deathbanplugin.base.DeathBanPlugin;

import java.util.UUID;

public class Storage {
    private final UUID uuid;
    private final String nick;
    private final long time;
    private final DeathBanPlugin plugin;

    public Storage(DeathBanPlugin plugin, UUID uuid, String nick, long time) {
        this.plugin = plugin;
        this.uuid = uuid;
        this.nick = nick;
        this.time = time;
    }

    public void save() {
        plugin.getDatabase().saveBan(uuid, nick, time);
    }

    public String getNick() { return nick; }
    public UUID getUuid() { return uuid; }
    public long getTime() { return time; }
    public Player getPlayer() { return Bukkit.getPlayer(uuid); }

    public void delete() {
        plugin.getDatabase().deleteBan(uuid);
        plugin.getStorageManager().getPlayerList().remove(this);
    }
}