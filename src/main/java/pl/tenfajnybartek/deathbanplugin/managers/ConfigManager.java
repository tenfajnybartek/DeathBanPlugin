package pl.tenfajnybartek.deathbanplugin.managers;

import org.bukkit.configuration.file.FileConfiguration;
import pl.tenfajnybartek.deathbanplugin.base.DeathBanPlugin;

public class ConfigManager {
    private final DeathBanPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(DeathBanPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        this.config = plugin.getConfig();
    }

    public String getMysqlHost()     { return config.getString("mysql.host", "localhost"); }
    public int getMysqlPort() { return config.getInt("mysql.port", 3306); }
    public String getMysqlUser()     { return config.getString("mysql.user", "root"); }
    public String getMysqlPassword() { return config.getString("mysql.password", ""); }
    public String getMysqlDatabase() { return config.getString("mysql.database", "deathban"); }

    public String getMessage(String key, String def) {
        return config.getString("messages." + key, def);
    }

    public int getDeathbanSeconds() {
        return config.getInt("settings.deathban_seconds", 300);
    }
    public String getExemptPermission() {
        return config.getString("settings.exempt_permission", "deathban.vip");
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }
}