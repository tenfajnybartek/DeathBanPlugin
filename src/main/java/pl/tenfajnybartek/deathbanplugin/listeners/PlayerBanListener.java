package pl.tenfajnybartek.deathbanplugin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.tenfajnybartek.deathbanplugin.base.DeathBanPlugin;
import pl.tenfajnybartek.deathbanplugin.database.Storage;
import pl.tenfajnybartek.deathbanplugin.events.PlayerPreBanEvent;
import pl.tenfajnybartek.deathbanplugin.utils.ChatUtils;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

public class PlayerBanListener implements Listener {
    private final DeathBanPlugin plugin;
    private final Set<String> recentlyBanned = new HashSet<>();

    public PlayerBanListener(DeathBanPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(PlayerLoginEvent event) {
        Storage storage = plugin.getStorageManager().getPlayerByNick(event.getPlayer().getName());
        if (storage == null) return;

        long now = System.currentTimeMillis();
        if (storage.getTime() >= now) {
            String kickMsg = plugin.getConfigManager().getMessage("ban_kick", "&cNie możesz wejść na serwer do &e%0 &c!")
                    .replace("%0", formatDate(storage.getTime()));
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, kickMsg);
            return;
        }
        storage.delete();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(PlayerDeathEvent event) {
        int deathbanSeconds = plugin.getConfig().getInt("settings.deathban_seconds", 86400);
        long banTime = System.currentTimeMillis() + (deathbanSeconds * 1000L);

        PlayerPreBanEvent preBanEvent = new PlayerPreBanEvent(event.getEntity(), banTime);
        Bukkit.getServer().getPluginManager().callEvent(preBanEvent);

        if (preBanEvent.isCancelled()) return;

        Storage storage = new Storage(plugin, event.getEntity().getName(), preBanEvent.getTime());
        plugin.getStorageManager().getPlayerList().add(storage);
        recentlyBanned.add(storage.getNick());

        String deathbanMsg = plugin.getConfigManager().getMessage("deathban", "&cZostałeś zbanowany do &e%0 &c!")
                .replace("%0", formatDate(preBanEvent.getTime()));
        ChatUtils.sendMessage(event.getEntity(), deathbanMsg);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (recentlyBanned.contains(storage.getNick())) {
                recentlyBanned.remove(storage.getNick());
                String kickMsg = plugin.getConfigManager().getMessage("ban_kick", "&cNie możesz wejść na serwer do &e%0 &c!")
                        .replace("%0", formatDate(storage.getTime()));
                event.getEntity().kickPlayer(kickMsg);
            }
        }, 20L * 3);
    }

    @EventHandler
    public void onPreBan(PlayerPreBanEvent event) {
        String exemptPerm = plugin.getConfig().getString("settings.exempt_permission", "deathban.vip");
        if (event.getPlayer().hasPermission(exemptPerm)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        recentlyBanned.remove(event.getPlayer().getName());
    }

    private String formatDate(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        return sdf.format(time);
    }
}