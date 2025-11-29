package pl.tenfajnybartek.deathbanplugin.listeners;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import pl.tenfajnybartek.deathbanplugin.base.DeathBanPlugin;
import pl.tenfajnybartek.deathbanplugin.database.Storage;
import pl.tenfajnybartek.deathbanplugin.events.PlayerPreBanEvent;
import pl.tenfajnybartek.deathbanplugin.utils.ChatUtils;
import pl.tenfajnybartek.deathbanplugin.utils.DateUtils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerBanListener implements Listener {
    private final DeathBanPlugin plugin;

    // UUID -> czas końca odliczania
    private final Map<UUID, Long> pendingBanTimeout = new ConcurrentHashMap<>();
    // UUID -> task odliczania
    private final Map<UUID, BukkitTask> pendingBanTasks = new ConcurrentHashMap<>();

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
                    "ban_kick", "&4&lBAN SMIERTELNY: &cNie mozesz wejsc na serwer do &e%0 &c!"
            ).replace("%0", DateUtils.formatDate(storage.getTime()));
            ChatUtils.disallowPreLoginWithMessage(event, kickMsg);
            return;
        }
        storage.delete();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID uuid = player.getUniqueId();

        int prebanSeconds = plugin.getConfig().getInt("settings.preban_seconds", 20);
        int deathbanSeconds = plugin.getConfigManager().getDeathbanSeconds();

        // Ban czas docelowy
        long banEndTime = System.currentTimeMillis() + (deathbanSeconds * 1000L);

        PlayerPreBanEvent preBanEvent = new PlayerPreBanEvent(player, banEndTime);
        Bukkit.getServer().getPluginManager().callEvent(preBanEvent);

        if (preBanEvent.isCancelled()) return;

        long preBanEnd = System.currentTimeMillis() + (prebanSeconds * 1000L);
        pendingBanTimeout.put(uuid, preBanEnd);

        // Pobierz dane z configu do odliczania
        String rawMessage = plugin.getConfigManager().getMessage("preban_message",
                "&cZostales smiertelnie ranny! Za &e%time% &csekund zostaniesz zbanowany po smierci!");
        String displayType = plugin.getConfig().getString("settings.preban_display", "chat");
        String tickSound = plugin.getConfig().getString("settings.preban_sound", "").toUpperCase(); // np. "BLOCK_NOTE_BLOCK_PLING"

        // Start cyklicznego tasku odliczania
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            int secondsLeft = (int) Math.max(0, (preBanEnd - now) / 1000);
            if (secondsLeft < 1) return;
            String msg = rawMessage.replace("%time%", String.valueOf(secondsLeft));
            sendBanMessage(player, msg, displayType);
            // Odtwarzaj dźwięk (jeśli ustawiłeś w configu)
            if (!tickSound.isEmpty()) {
                try {
                    player.playSound(
                            Sound.sound(Key.key(tickSound.toLowerCase()), Sound.Source.PLAYER, 1.0f, 1.0f)
                    );
                } catch (Exception ignored) {}
            }
        }, 0L, 20L);
        pendingBanTasks.put(uuid, task);

        // Finalny ban + dźwięk + osobna wiadomość wg configu
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (pendingBanTimeout.containsKey(uuid)) {
                pendingBanTimeout.remove(uuid);
                BukkitTask countdown = pendingBanTasks.remove(uuid);
                if (countdown != null) countdown.cancel();

                Storage storage = new Storage(plugin, uuid, player.getName(), preBanEvent.getTime());
                storage.save();
                plugin.getStorageManager().getPlayerList().add(storage);

                // Wiadomość finalna (np. "Zostałeś zbanowany za śmierć!") i typ wyświetlania
                String finalBanMsg = plugin.getConfigManager().getMessage(
                        "final_ban_message", "&cZostales zbanowany po smierci!"); // nowa wiadomość
                finalBanMsg = finalBanMsg.replace("%date%", DateUtils.formatDate(storage.getTime()));
                String deathbanDisplay = plugin.getConfig().getString("settings.deathban_display", "chat");
                sendBanMessage(player, finalBanMsg, deathbanDisplay);

                // Dźwięk finalny (możesz ustawić w configu np. ENTITY_ENDER_DRAGON_GROWL)
                String finalBanSound = plugin.getConfig().getString("settings.deathban_sound", "").toUpperCase();
                if (!finalBanSound.isEmpty()) {
                    try {
                        player.playSound(
                                Sound.sound(Key.key(finalBanSound.toLowerCase()), Sound.Source.PLAYER, 1.0f, 1.0f)
                        );
                    } catch (Exception ignored) {}
                }

                // Kick z klasyczną wiadomością deathban/kick (chat, bo kicks nie wspierają title!)
                String deathbanMsg = plugin.getConfigManager().getMessage(
                        "deathban", "&cZostales zbanowany do &e%0 &c!"
                ).replace("%0", DateUtils.formatDate(storage.getTime()));
                ChatUtils.kickWithMessage(player, deathbanMsg);
            }
        }, prebanSeconds * 20L);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        // Jeśli gracz wyjdzie zanim upłynie odliczanie
        if (pendingBanTimeout.containsKey(uuid)) {
            pendingBanTimeout.remove(uuid);
            BukkitTask countdown = pendingBanTasks.remove(uuid);
            if (countdown != null) countdown.cancel();

            int deathbanSeconds = plugin.getConfigManager().getDeathbanSeconds();
            long banEndTime = System.currentTimeMillis() + (deathbanSeconds * 1000L);

            Storage storage = new Storage(plugin, uuid, event.getPlayer().getName(), banEndTime);
            storage.save();
            plugin.getStorageManager().getPlayerList().add(storage);
        }
    }

    @EventHandler
    public void onPreBan(PlayerPreBanEvent event) {
        String exemptPerm = plugin.getConfigManager().getExemptPermission();
        if (event.getPlayer().hasPermission(exemptPerm)) {
            event.setCancelled(true);
        }
    }

    // Typ wyświetlania wiadomości: chat, title, actionbar
    private void sendBanMessage(Player player, String message, String displayType) {
        switch (displayType.toLowerCase()) {
            case "title" -> player.sendTitle(ChatUtils.colorToLegacy(message), "", 10, 40, 10);
            case "actionbar" -> player.sendActionBar(ChatUtils.color(message));
            default -> ChatUtils.sendMessage(player, message);
        }
    }
}