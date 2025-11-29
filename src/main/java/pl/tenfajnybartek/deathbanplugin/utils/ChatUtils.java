package pl.tenfajnybartek.deathbanplugin.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class ChatUtils {

    private static final LegacyComponentSerializer SERIALIZER =
            LegacyComponentSerializer.legacyAmpersand();

    public static Component color(String message) {
        return SERIALIZER.deserialize(message);
    }

    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(color(message));
    }

    public static void kickWithMessage(Player player, String message) {
        player.kick(color(message));
    }

    public static void disallowPreLoginWithMessage(AsyncPlayerPreLoginEvent event, String message) {
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, color(message));
    }

    // Zamiana & na §, tylko jeśli musisz legacy do starego API
    public static String colorToLegacy(String msg) {
        return msg.replace('&', '§');
    }
}