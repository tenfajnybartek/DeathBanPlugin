package pl.tenfajnybartek.deathbanplugin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.tenfajnybartek.deathbanplugin.base.DeathBanPlugin;
import pl.tenfajnybartek.deathbanplugin.database.Storage;
import pl.tenfajnybartek.deathbanplugin.events.PlayerPreBanEvent;
import pl.tenfajnybartek.deathbanplugin.utils.ChatUtils;
import pl.tenfajnybartek.deathbanplugin.utils.DateUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerBanListener implements Listener {
    private final DeathBanPlugin plugin;
    private final Set<UUID> recentlyBanned = new HashSet<>();

    public PlayerBanListener(DeathBanPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();
        Storage storage = plugin.getStorageManager().getPlayerByUuid(uuid);
        if (storage == null) return;

        long now = System.currentTimeMillis();
        if (storage.getTime() >= now) {
            String kickMsg = plugin.getConfigManager().getMessage(
                    "ban_kick", "&cNie możesz wejść na serwer do &e%0 &c!"
            ).replace("%0", DateUtils.formatDate(storage.getTime()));
            ChatUtils.disallowPreLoginWithMessage(event, kickMsg);
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

        // VIP = brak bana i komunikatu!
        if (preBanEvent.isCancelled()) return;

        UUID uuid = event.getEntity().getUniqueId();
        String nick = event.getEntity().getName();
        Storage storage = new Storage(plugin, uuid, nick, preBanEvent.getTime());
        storage.save();
        plugin.getStorageManager().getPlayerList().add(storage);
        recentlyBanned.add(uuid);

        String deathbanMsg = plugin.getConfigManager().getMessage(
                "deathban", "&cZostałeś zbanowany do &e%0 &c!"
        ).replace("%0", DateUtils.formatDate(preBanEvent.getTime()));
        ChatUtils.sendMessage(event.getEntity(), deathbanMsg);

        // Kicking po 3 sek.
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (recentlyBanned.contains(uuid)) {
                recentlyBanned.remove(uuid);
                String kickMsg = plugin.getConfigManager().getMessage("ban_kick", "&cNie możesz wejść na serwer do &e%0 &c!")
                        .replace("%0", DateUtils.formatDate(storage.getTime()));
                ChatUtils.kickWithMessage(event.getEntity(), kickMsg);
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
        recentlyBanned.remove(event.getPlayer().getUniqueId());
    }
}