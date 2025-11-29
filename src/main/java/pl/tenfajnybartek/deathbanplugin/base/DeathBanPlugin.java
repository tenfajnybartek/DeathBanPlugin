package pl.tenfajnybartek.deathbanplugin;

import org.bukkit.plugin.java.JavaPlugin;

public final class DeathBanPlugin extends JavaPlugin {

    private Database database;
    private StorageManager storageManager;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        getLogger().info("Włączanie pluginu...");
        saveDefaultConfig();

        configManager = new ConfigManager(this);
        storageManager = new StorageManager();
        database = new Database(
                this,
                configManager.getMysqlHost(),
                configManager.getMysqlUser(),
                configManager.getMysqlPassword(),
                configManager.getMysqlDatabase()
        );

        // Listener i komenda
        getServer().getPluginManager().registerEvents(new PlayerBanListener(this), this);
        getCommand("deathban").setExecutor(new DeathBanCommand(this));
    }

    @Override
    public void onDisable() {
        getLogger().info("Wyłączanie pluginu...");
        if (database != null) database.close();
    }
    
    public Database getDatabase() { return database; }
    public StorageManager getStorageManager() { return storageManager; }
    public ConfigManager getConfigManager() { return configManager; }
}
