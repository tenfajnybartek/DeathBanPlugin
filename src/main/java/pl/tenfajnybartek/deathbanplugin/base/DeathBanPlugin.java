package pl.tenfajnybartek.deathbanplugin.base;

import org.bukkit.plugin.java.JavaPlugin;
import pl.tenfajnybartek.deathbanplugin.commands.DeathBanCommand;
import pl.tenfajnybartek.deathbanplugin.database.Database;
import pl.tenfajnybartek.deathbanplugin.listeners.PlayerBanListener;
import pl.tenfajnybartek.deathbanplugin.managers.ConfigManager;
import pl.tenfajnybartek.deathbanplugin.managers.StorageManager;
import pl.tenfajnybartek.deathbanplugin.utils.ChatUtils;

public final class DeathBanPlugin extends JavaPlugin {

    private Database database;
    private StorageManager storageManager;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        getLogger().info("Włączanie pluginu...");
        saveDefaultConfig();

        configManager = new ConfigManager(this);
        database = new Database(
                this,
                configManager.getMysqlHost(),
                configManager.getMysqlPort(),
                configManager.getMysqlUser(),
                configManager.getMysqlPassword(),
                configManager.getMysqlDatabase()
        );
        storageManager = new StorageManager();
        storageManager.loadAll(this);

        ChatUtils.init(this);

        getServer().getPluginManager().registerEvents(new PlayerBanListener(this), this);
        getCommand("deathban").setExecutor(new DeathBanCommand(this));
    }

    @Override
    public void onDisable() {
        getLogger().info("Wyłączanie pluginu...");
        ChatUtils.close();
        if (database != null) database.close();
    }

    public Database getDatabase() { return database; }
    public StorageManager getStorageManager() { return storageManager; }
    public ConfigManager getConfigManager() { return configManager; }
}