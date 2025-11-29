package pl.tenfajnybartek.deathbanplugin.base;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import pl.tenfajnybartek.deathbanplugin.commands.DeathBanCommand;
import pl.tenfajnybartek.deathbanplugin.database.Database;
import pl.tenfajnybartek.deathbanplugin.database.Storage;
import pl.tenfajnybartek.deathbanplugin.listeners.PlayerBanListener;
import pl.tenfajnybartek.deathbanplugin.managers.ConfigManager;
import pl.tenfajnybartek.deathbanplugin.managers.StorageManager;

import java.util.ArrayList;
import java.util.List;

public final class DeathBanPlugin extends JavaPlugin {

    private Database database;
    private int unbanTaskId = -1;
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
        storageManager.loadAllAsync(this);
        startUnbanTask();

        getServer().getPluginManager().registerEvents(new PlayerBanListener(this), this);
        getCommand("deathban").setExecutor(new DeathBanCommand(this));
    }


    private void startUnbanTask() {
        unbanTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            List<Storage> toRemove = new ArrayList<>();
            for (Storage s : storageManager.getPlayerList()) {
                if (System.currentTimeMillis() > s.getTime()) {
                    toRemove.add(s);
                }
            }
            for (Storage s : toRemove) {
                s.delete();
            }
        }, 20L * 60, 20L * 60).getTaskId();
    }

    @Override
    public void onDisable() {
        getLogger().info("Wyłączanie pluginu...");
        if (unbanTaskId != -1) {
            Bukkit.getScheduler().cancelTask(unbanTaskId);
            unbanTaskId = -1;
        }
        if (database != null) database.close();
    }

    public Database getDatabase() { return database; }
    public StorageManager getStorageManager() { return storageManager; }
    public ConfigManager getConfigManager() { return configManager; }
}