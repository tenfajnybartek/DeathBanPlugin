package pl.tenfajnybartek.deathbanplugin.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class ChatUtils {

    public static Component color(String message) {
        // Pozostawiamy domyślny legacy ampersand z obsługą UTF-8 (nie koduje polskich znaków na phi/krzaczki!)
        return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
    }

    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(color(message));
    }

    public static void kickWithMessage(Player player, String message) {
        // Adventure obsługuje polskie znaki/UTF-8!
        player.kick(color(message));
    }

    public static void disallowPreLoginWithMessage(AsyncPlayerPreLoginEvent event, String message) {
        // Nowa wersja: wysyłaj wiadomość bez konwersji do legacy ASCII, działa w UTF-8!
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, message);
    }

    public static String colorToLegacy(String msg) {
        // Możesz zostawić, nie używać do kicka
        return msg.replace('&', '§');
    }
}