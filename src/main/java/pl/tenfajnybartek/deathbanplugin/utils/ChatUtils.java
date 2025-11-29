package pl.tenfajnybartek.deathbanplugin.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class ChatUtils {

    public static Component color(String message) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
    }

    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(color(message));
    }

    public static void sendMessage(CommandSender sender, Component component) {
        sender.sendMessage(component);
    }

    public static void kickWithMessage(Player player, String message) {
        player.kick(color(message));
    }

    public static void disallowPreLoginWithMessage(AsyncPlayerPreLoginEvent event, String message) {
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, colorToLegacy(message));
    }

    public static String colorToLegacy(String msg) {
        return msg.replace('&', '§');
    }
}